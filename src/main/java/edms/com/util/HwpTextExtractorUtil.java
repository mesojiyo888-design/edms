package edms.com.util;

import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.reader.HWPReader;
import kr.dogfoot.hwplib.tool.textextractor.TextExtractMethod;
import kr.dogfoot.hwplib.tool.textextractor.TextExtractor;

import kr.dogfoot.hwpxlib.object.HWPXFile;
import kr.dogfoot.hwpxlib.reader.HWPXReader;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * HWP / HWPX 문서에서 텍스트를 추출하는 유틸리티.
 * - hwplib-1.1.10, hwpxlib-1.0.9 기준 (Java 8)
 *
 * [클래스명 충돌 주의]
 * hwplib과 hwpxlib에 동일한 이름의 TextExtractor / TextExtractMethod 클래스가 각각 존재합니다.
 * 이 파일에서는 hwplib 쪽만 import하고, hwpxlib 쪽은 메서드 내부에서
 * FQCN(전체 패키지 경로)으로 직접 참조합니다.
 *
 * [스레드 안전성]
 * 이 클래스는 상태를 가지지 않는 정적 유틸리티이며, extractText() 호출마다
 * 새로운 HWPFile/HWPXFile 객체를 생성하므로 멀티스레드 환경에서 안전하게 호출할 수 있습니다.
 *
 * [호출부 책임]
 * - 업로드 파일명/경로에 대한 경로 검증(Path Traversal 방지)은 호출부에서 수행해야 합니다.
 * - MultipartFile 등 스트림으로 받은 경우 saveToTempFileAndExtract()를 사용하면
 *   임시파일 생성/삭제까지 자동으로 처리됩니다.
 */
public final class HwpTextExtractorUtil {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HwpTextExtractorUtil.class);

    /** 처리 허용 최대 파일 크기 (기본 50MB) - 필요에 맞게 조정 */
    private static final long MAX_FILE_SIZE_BYTES = 50L * 1024 * 1024;

    /** 추출 결과 텍스트 최대 길이 (기본 5,000,000자) - DB 컬럼/화면 출력 보호용 */
    private static final int MAX_EXTRACTED_TEXT_LENGTH = 5_000_000;

    /** 매직바이트 비교에 필요한 최소 바이트 수 */
    private static final int SIGNATURE_CHECK_LENGTH = 8;

    // OLE2 (HWP) 파일 시그니처: D0 CF 11 E0 A1 B1 1A E1
    private static final byte[] HWP_OLE_SIGNATURE = {
            (byte) 0xD0, (byte) 0xCF, (byte) 0x11, (byte) 0xE0,
            (byte) 0xA1, (byte) 0xB1, (byte) 0x1A, (byte) 0xE1
    };

    // ZIP 기반 (HWPX) 파일 시그니처: PK 03 04
    private static final byte[] ZIP_SIGNATURE = {
            (byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04
    };

    private HwpTextExtractorUtil() {
        // 유틸 클래스 - 인스턴스화 방지
    }

    /**
     * 파일을 읽어 텍스트를 추출한다.
     * 확장자와 실제 파일 시그니처를 모두 확인하여 hwp/hwpx 여부를 판단한다.
     *
     * @param file 추출 대상 파일 (null 불가, 존재해야 함)
     * @return 추출된 전체 텍스트 (최대 MAX_EXTRACTED_TEXT_LENGTH 자로 잘릴 수 있음)
     * @throws HwpExtractException 파일이 유효하지 않거나, 지원하지 않는 형식이거나,
     *                              파싱 중 오류가 발생한 경우
     */
    public static String extractText(File file) throws HwpExtractException {

        validateFile(file);

        DocType type = detectDocType(file);

        String text;
        switch (type) {
            case HWP:
                text = extractFromHwp(file);
                break;
            case HWPX:
                text = extractFromHwpx(file);
                break;
            default:
                throw new HwpExtractException("지원하지 않는 파일 형식입니다. (hwp/hwpx 아님)");
        }

        return truncateIfNeeded(text);
    }

    /**
     * InputStream(예: MultipartFile.getInputStream())으로 받은 데이터를
     * 임시파일에 저장한 뒤 텍스트를 추출하고, 처리 완료/실패와 무관하게
     * 임시파일을 삭제한다.
     *
     * @param inputStream 업로드된 파일의 입력 스트림 (호출부에서 닫을 필요 없음, 내부에서 처리)
     * @param originalFileName 원본 파일명 (확장자 판별용. 경로 구분자가 포함된 값은 거부됨)
     * @return 추출된 텍스트
     * @throws HwpExtractException 추출 실패, 파일명 유효성 실패, 임시파일 처리 실패 시
     */
    public static String saveToTempFileAndExtract(InputStream inputStream, String originalFileName) throws HwpExtractException {

        String suffix = getSuffix(inputStream, originalFileName);
        LOGGER.debug("HwpTextExtractorUtil.saveToTempFileAndExtract - suffix : {}", suffix);

        File tempFile = null;
        try {
            // prefix는 고정 문자열만 사용 (원본 파일명을 그대로 prefix로 쓰지 않음 - 예측 불가/안전한 임시명 생성)
            String currentTime = getCurrentTime();
            LOGGER.debug("HwpTextExtractorUtil.saveToTempFileAndExtract - currentTime : {}", currentTime);

            tempFile = Files.createTempFile(currentTime + "_tmp_hwp_extract_", suffix).toFile();

            // 스트림 -> 임시파일 복사 (크기 제한을 넘으면 중단)
            copyWithSizeLimit(inputStream, tempFile, MAX_FILE_SIZE_BYTES);

            return extractText(tempFile);

        } catch (HwpExtractException e) {
            throw e;
        } catch (IOException e) {
            throw new HwpExtractException("임시 파일 생성/쓰기 중 오류가 발생했습니다.", e);
        } finally {
            // 성공/실패 여부와 관계없이 임시파일 정리 (디스크 누적 방지)
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    // 삭제 실패 시 JVM 종료 시점에라도 삭제되도록 예약
                    tempFile.deleteOnExit();
                }
            }
        }
    }

    private static String getSuffix(InputStream inputStream, String originalFileName) throws HwpExtractException {
        LOGGER.debug("HwpTextExtractorUtil.getSuffix - originalFileName : {}", originalFileName);

        if (inputStream == null) {
            throw new HwpExtractException("입력 스트림이 null 입니다.");
        }
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new HwpExtractException("원본 파일명이 비어 있습니다.");
        }
        // 경로 traversal 방지: 파일명에 경로 구분자/상위 디렉터리 참조가 포함되면 거부
        if (originalFileName.contains("/") || originalFileName.contains("\\")
                || originalFileName.contains("..")) {
            throw new HwpExtractException("허용되지 않는 파일명입니다: " + originalFileName);
        }

        String lowerName = originalFileName.toLowerCase();
        LOGGER.debug("HwpTextExtractorUtil.getSuffix - lowerName : {}", lowerName);

        String suffix;
        if (lowerName.endsWith(".hwpx")) {
            suffix = ".hwpx";
        } else if (lowerName.endsWith(".hwp")) {
            suffix = ".hwp";
        } else {
            throw new HwpExtractException("지원하지 않는 확장자입니다: " + originalFileName);
        }
        return suffix;
    }

    // ------------------------------------------------------------------
    // 내부 구현
    // ------------------------------------------------------------------

    private static void validateFile(File file) throws HwpExtractException {
        if (file == null) {
            throw new HwpExtractException("파일이 null 입니다.");
        }
        if (!file.exists() || !file.isFile()) {
            throw new HwpExtractException("파일이 존재하지 않거나 일반 파일이 아닙니다: " + file.getName());
        }
        if (!file.canRead()) {
            throw new HwpExtractException("파일을 읽을 권한이 없습니다: " + file.getName());
        }
        long size = file.length();
        if (size == 0) {
            throw new HwpExtractException("파일 크기가 0입니다: " + file.getName());
        }
        if (size > MAX_FILE_SIZE_BYTES) {
            throw new HwpExtractException(
                    "파일이 허용된 최대 크기를 초과합니다. (최대 "
                            + (MAX_FILE_SIZE_BYTES / (1024 * 1024)) + "MB): " + file.getName());
        }
    }

    /**
     * 입력 스트림을 목적 파일로 복사하되, maxBytes를 초과하면 즉시 중단하고 예외를 던진다.
     * (메모리에 전체를 올리지 않고 스트리밍 방식으로 처리 - OOM 방지)
     */
    private static void copyWithSizeLimit(InputStream in, File dest, long maxBytes) throws IOException, HwpExtractException {

        byte[] buffer = new byte[8192];
        long total = 0;

        try (OutputStream out = Files.newOutputStream(dest.toPath())) {
            int read;
            while ((read = in.read(buffer)) != -1) {
                total += read;
                if (total > maxBytes) {
                    throw new HwpExtractException(
                            "업로드 파일이 허용된 최대 크기를 초과합니다. (최대 "
                                    + (maxBytes / (1024 * 1024)) + "MB)");
                }
                out.write(buffer, 0, read);
            }
        } catch (HwpExtractException e) {
            throw e;
        }

        if (total == 0) {
            throw new HwpExtractException("업로드된 파일 내용이 비어 있습니다.");
        }
    }

    /**
     * 확장자 + 매직바이트를 종합해서 문서 타입을 판별한다.
     * 확장자만으로 판단하지 않는 이유:
     * - 사용자가 파일 확장자를 임의로 변경해 업로드할 수 있기 때문 (보안상 위험)
     */
    private static DocType detectDocType(File file) throws HwpExtractException {
        String name = file.getName().toLowerCase();
        boolean extHwp = name.endsWith(".hwp");
        boolean extHwpx = name.endsWith(".hwpx");
        LOGGER.debug("HwpTextExtractorUtil.detectDocType - extHwp : {}", extHwp);
        LOGGER.debug("HwpTextExtractorUtil.detectDocType - extHwpx : {}", extHwpx);

        if (!extHwp && !extHwpx) {
            throw new HwpExtractException("지원하지 않는 확장자입니다: " + file.getName());
        }

        byte[] header = readHeaderBytes(file, SIGNATURE_CHECK_LENGTH);

        boolean isOle = startsWith(header, HWP_OLE_SIGNATURE);
        boolean isZip = startsWith(header, ZIP_SIGNATURE);

        if (extHwp && isOle) {
            return DocType.HWP;
        }
        if (extHwpx && isZip) {
            return DocType.HWPX;
        }

        // 확장자와 실제 시그니처가 불일치 -> 위변조 가능성으로 보고 차단
        throw new HwpExtractException(
                "파일 확장자와 실제 파일 형식이 일치하지 않습니다. (위변조 의심): " + file.getName());
    }

    private static byte[] readHeaderBytes(File file, int length) throws HwpExtractException {
        byte[] buffer = new byte[length];
        try (InputStream is = Files.newInputStream(file.toPath())) {
            int read = is.read(buffer);
            if (read < length) {
                throw new HwpExtractException("파일 헤더를 읽을 수 없습니다(파일이 너무 작음): " + file.getName());
            }
            return buffer;
        } catch (IOException e) {
            throw new HwpExtractException("파일 헤더 읽기 실패: " + file.getName(), e);
        }
    }

    private static boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * hwplib 1.1.10 기준 HWP 텍스트 추출.
     * TextExtractor.extract(HWPFile, TextExtractMethod) - throws UnsupportedEncodingException(checked)
     */
    private static String extractFromHwp(File file) throws HwpExtractException {
        try {
            HWPFile hwpFile = HWPReader.fromFile(file.getAbsolutePath());

            String text = TextExtractor.extract(
                    hwpFile,
                    TextExtractMethod.InsertControlTextBetweenParagraphText);

            LOGGER.debug("HwpTextExtractorUtil.extractFromHwp - text : {}", text);

            return text == null ? "" : text;

        } catch (HwpExtractException e) {
            throw e;
        } catch (Exception e) {
            // UnsupportedEncodingException 포함, 손상/암호화 문서에서 발생 가능한 RuntimeException까지 포괄
            throw new HwpExtractException(
                    "HWP 파일 파싱 중 오류가 발생했습니다. 암호화/손상 여부를 확인하세요: " + file.getName(), e);
        }
    }

    /**
     * hwpxlib 1.0.9 기준 HWPX 텍스트 추출.
     * kr.dogfoot.hwpxlib.tool.textextractor.TextExtractor.extract(
     *     HWPXFile, TextExtractMethod, boolean insertParaHead, TextMarks) throws Exception
     *
     * hwplib의 TextExtractor / TextExtractMethod와 클래스명이 동일하여
     * import 충돌이 발생하므로 FQCN(전체 패키지 경로)으로 직접 호출한다.
     */
    private static String extractFromHwpx(File file) throws HwpExtractException {
        try {
            HWPXFile hwpxFile = HWPXReader.fromFile(file);

            String text = kr.dogfoot.hwpxlib.tool.textextractor.TextExtractor.extract(
                    hwpxFile,
                    kr.dogfoot.hwpxlib.tool.textextractor.TextExtractMethod.AppendControlTextAfterParagraphText,
                    true, // insertParaHead: 문단 번호/머리표 포함
                    new kr.dogfoot.hwpxlib.tool.textextractor.TextMarks()); // 기본 구분자 사용

            LOGGER.debug("HwpTextExtractorUtil.extractFromHwpx - text : {}", text);

            return text == null ? "" : text;

        } catch (HwpExtractException e) {
            throw e;
        } catch (Exception e) {
            throw new HwpExtractException(
                    "HWPX 파일 파싱 중 오류가 발생했습니다. 손상 여부를 확인하세요: " + file.getName(), e);
        }
    }

    /**
     * 추출된 텍스트가 너무 길 경우 안전하게 잘라낸다.
     * (DB 컬럼 오버플로우, 화면 렌더링 지연 등 방지)
     */
    private static String truncateIfNeeded(String text) {
        if (text != null && text.length() > MAX_EXTRACTED_TEXT_LENGTH) {
            return text.substring(0, MAX_EXTRACTED_TEXT_LENGTH)
                    + "\n...(내용이 너무 길어 일부만 표시됩니다)";
        }
        return text;
    }

    private enum DocType {
        HWP, HWPX, UNKNOWN
    }

    /**
     * 현재 시간 밀리세컨드 단위로 String값 반환
     * @return
     */
    private static String getCurrentTime() {
        Instant instant = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_SSS").withZone(ZoneId.of("UTC+9"));
        String currentTime = formatter.format(instant);
        LOGGER.debug("HwpTextExtractorUtil.getCurrentTime - currentTime : {}", currentTime);

        return currentTime;
    }

    /**
     * HWP/HWPX 추출 관련 모든 예외를 감싸는 커스텀 예외.
     * 호출부(Controller/Service)에서 이 예외만 catch 하면
     * 라이브러리 내부 예외 타입에 의존하지 않고 일관되게 처리 가능.
     */
    public static class HwpExtractException extends Exception {
        public HwpExtractException(String message) {
            super(message);
        }

        public HwpExtractException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
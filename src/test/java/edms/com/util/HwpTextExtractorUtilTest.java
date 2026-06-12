package edms.com.util;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.Files;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HwpTextExtractorUtilTest {

    private final String HWP_TRUE = "src/main/resources/static/sample/SAMPLE_001.hwp";
    private final String HWP_FALSE = "src/main/resources/static/sample/SAMPLE_002.hwp";
    private final String HWPX_TRUE = "src/main/resources/static/sample/SAMPLE_001.hwpx";
    private final String HWPX_FALSE = "src/main/resources/static/sample/SAMPLE_002.hwpx";

    @Order(1)
    @Test
    @DisplayName("File방식 - 올바른 한글파일 HWP 테스트 통과")
    void extractTextHwpTrue() throws HwpTextExtractorUtil.HwpExtractException {
        File hwpfile = new File(HWP_TRUE);
        System.out.println("====================================================");
        System.out.println("[TEST] HwpTextExtractorUtil.extractTextHwpTrue");
        System.out.println(HwpTextExtractorUtil.extractText(hwpfile));
        System.out.println("====================================================");
    }

    @Order(2)
    @Test
    @DisplayName("File방식 - 올바르지 않은 한글파일 HWP 테스트 실패")
    void extractTextHwpFalse() throws HwpTextExtractorUtil.HwpExtractException {
        File hwpfile = new File(HWP_FALSE);
        System.out.println("====================================================");
        System.out.println("[TEST] HwpTextExtractorUtil.extractTextHwpFalse");
        System.out.println(HwpTextExtractorUtil.extractText(hwpfile));
        System.out.println("====================================================");
    }

    @Order(3)
    @Test
    @DisplayName("File방식 - 올바른 한글파일 HWPX 테스트 통과")
    void extractTextHwpxTrue() throws HwpTextExtractorUtil.HwpExtractException {
        File hwpfile = new File(HWPX_TRUE);
        System.out.println("====================================================");
        System.out.println("[TEST] HwpTextExtractorUtil.extractTextHwpxTrue");
        System.out.println(HwpTextExtractorUtil.extractText(hwpfile));
        System.out.println("====================================================");
    }

    @Order(4)
    @Test
    @DisplayName("File방식 - 올바르지 않은 한글파일 HWPX 테스트 실패")
    void extractTextHwpxFalse() throws HwpTextExtractorUtil.HwpExtractException {
        File hwpfile = new File(HWPX_FALSE);
        System.out.println("====================================================");
        System.out.println("[TEST] HwpTextExtractorUtil.extractTextHwpxFalse");
        System.out.println(HwpTextExtractorUtil.extractText(hwpfile));
        System.out.println("====================================================");
    }

    @Order(5)
    @Test
    @DisplayName("Stream 방식 - 올바른 한글파일 HWP 테스트 성공")
    void saveToTempFileAndExtractHwpTrue() throws IOException, HwpTextExtractorUtil.HwpExtractException {
        File hwpfile = new File(HWP_TRUE);

        InputStream is = Files.newInputStream(hwpfile.toPath());
        System.out.println("====================================================");
        System.out.println("[TEST] HwpTextExtractorUtil.saveToTempFileAndExtractHwpTrue");
        System.out.println(HwpTextExtractorUtil.saveToTempFileAndExtract(is, hwpfile.getName()));
        System.out.println("====================================================");
    }

    @Order(6)
    @Test
    @DisplayName("Stream 방식 - 올바르지 않은 한글파일 HWP 테스트 실패")
    void saveToTempFileAndExtractHwpFalse() throws IOException, HwpTextExtractorUtil.HwpExtractException {
        File hwpfile = new File(HWP_FALSE);

        InputStream is = Files.newInputStream(hwpfile.toPath());
        System.out.println("====================================================");
        System.out.println("[TEST] HwpTextExtractorUtil.saveToTempFileAndExtractHwpFalse");
        System.out.println(HwpTextExtractorUtil.saveToTempFileAndExtract(is, hwpfile.getName()));
        System.out.println("====================================================");
    }

    @Order(7)
    @Test
    @DisplayName("Stream 방식 - 올바른 한글파일 HWPX 테스트 성공")
    void saveToTempFileAndExtractHwpxTrue() throws IOException, HwpTextExtractorUtil.HwpExtractException {
        File hwpfile = new File(HWPX_TRUE);

        InputStream is = Files.newInputStream(hwpfile.toPath());
        System.out.println("====================================================");
        System.out.println("[TEST] HwpTextExtractorUtil.saveToTempFileAndExtractHwpxTrue");
        System.out.println(HwpTextExtractorUtil.saveToTempFileAndExtract(is, hwpfile.getName()));
        System.out.println("====================================================");
    }

    @Order(8)
    @Test
    @DisplayName("Stream 방식 - 올바르지 않은 한글파일 HWPX 테스트 실패")
    void saveToTempFileAndExtractHwpxalse() throws IOException, HwpTextExtractorUtil.HwpExtractException {
        File hwpfile = new File(HWPX_FALSE);

        InputStream is = Files.newInputStream(hwpfile.toPath());
        System.out.println("====================================================");
        System.out.println("[TEST] HwpTextExtractorUtil.saveToTempFileAndExtractHwpxFalse");
        System.out.println(HwpTextExtractorUtil.saveToTempFileAndExtract(is, hwpfile.getName()));
        System.out.println("====================================================");
    }
}
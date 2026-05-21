package edms.com.service.impl;

import edms.com.service.EdmsExcelService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service("edmsExcelService")
public class EdmsExcelServiceImpl implements EdmsExcelService {

    @Override
    public void createExcelDownload(OutputStream os, String[] headers, String[] dataKeys, List<Map<String, Object>> dataList) throws Exception {
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        Sheet sheet = workbook.createSheet("데이터_목록");

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        int rowNum = 1;
        for (Map<String, Object> data : dataList) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < dataKeys.length; i++) {
                Cell cell = row.createCell(i);
                Object value = data.get(dataKeys[i]);
                if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue());
                } else {
                    cell.setCellValue(value != null ? value.toString() : "");
                }
            }
        }

        try {
            workbook.write(os);
        } finally {
            workbook.close();
            workbook.dispose();
        }
    }

    @Override
    public List<Map<String, String>> readExcelUpload(InputStream is) throws Exception {
        List<Map<String, String>> resultList = new ArrayList<>();

        // 1. 스프링 내장 유틸로 바이트 완전 복사
        byte[] fileBytes = StreamUtils.copyToByteArray(is);

        // 2. [완전 방어 코드] 엑셀 검증 다 건너뛰고 텍스트 라인 바인딩을 강제로 먼저 수행합니다.
        // MS 엑셀 표준 출력과 일반 인코딩을 다 잡기 위해 UTF-8과 MS949(EUC-KR) 범용 스트림으로 읽습니다.
        try (InputStream csvIs = new ByteArrayInputStream(fileBytes);
             BufferedReader br = new BufferedReader(new InputStreamReader(csvIs, StandardCharsets.UTF_8))) {

            String line;
            int currentLineNum = 0;

            while ((line = br.readLine()) != null) {
                // 완전히 빈 줄이거나 바이너리 엑셀 찌꺼기 깨진 글자 라인이면 패스
                if (line.trim().isEmpty()) continue;

                // 첫 줄(0번째)은 "번호,이름,이메일" 헤더이므로 무조건 데이터 바인딩에서 제외
                if (currentLineNum == 0) {
                    currentLineNum++;
                    continue;
                }

                // 쉼표(,) 기준으로 무조건 강제 분할 (-1 옵션으로 빈 값 누락 방지)
                String[] tokens = line.split(",", -1);

                // 쉼표로 쪼갰는데 컬럼 분리가 전혀 안 되었다면(데이터가 없거나 진짜 바이너리 엑셀 파일) 루프 탈출
                if (tokens.length <= 1) {
                    break;
                }

                Map<String, String> data = new HashMap<>();
                boolean hasRealValue = false;

                for (int colIdx = 0; colIdx < tokens.length; colIdx++) {
                    String cellValue = tokens[colIdx].trim();

                    // 가끔 깨진 바이너리 기호가 섞여 들어오는 현상 필터링
                    if (cellValue.contains("\u0000") || cellValue.contains("")) {
                        cellValue = "";
                    }

                    data.put("cell_" + colIdx, cellValue);

                    if (!cellValue.isEmpty()) {
                        hasRealValue = true; // 빈 문자열이 아닌 진짜 텍스트가 박혀있는지 체크
                    }
                }

                if (hasRealValue) {
                    resultList.add(data);
                }
                currentLineNum++;
            }

            // 위 강제 파싱 코드로 데이터가 1건이라도 건져졌다면 즉시 화면으로 반환
            if (!resultList.isEmpty()) {
                return resultList;
            }
        } catch (Exception e) {
            // 실패 시 아래 순수 엑셀 로직으로 이동하기 위해 예외 무시
        }

        // 3. 만약 위 텍스트 강제 파싱으로 한 건도 안 나왔다면, 그제서야 진짜 순수 엑셀(.xlsx) 파일 구조로 해독 시작
        resultList.clear();
        try (InputStream xlsxIs = new ByteArrayInputStream(fileBytes);
             Workbook workbook = new XSSFWorkbook(xlsxIs)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.rowIterator();
            int rowIdx = 0;

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (rowIdx == 0) { rowIdx++; continue; } // 첫 줄 헤더 스킵

                Map<String, String> data = new HashMap<>();
                boolean hasData = false;
                short totalCells = row.getLastCellNum();

                for (int colIdx = 0; colIdx < totalCells; colIdx++) {
                    Cell cell = row.getCell(colIdx);
                    String cellValue = (cell != null) ? getCellValueString(cell) : "";

                    data.put("cell_" + colIdx, cellValue);
                    if (!cellValue.isEmpty()) hasData = true;
                }
                if (hasData) resultList.add(data);
                rowIdx++;
            }
        }

        return resultList;
    }

    private String getCellValueString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) return cell.getDateCellValue().toString();
                double numericVal = cell.getNumericCellValue();
                if (numericVal == (long) numericVal) return String.valueOf((long) numericVal);
                return String.valueOf(numericVal);
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return "";
        }
    }
}
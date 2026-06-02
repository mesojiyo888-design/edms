package edms.com.service.impl;

import edms.com.service.EdmsExcelService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
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

        byte[] fileBytes = StreamUtils.copyToByteArray(is);

        try (InputStream xlsxIs = new ByteArrayInputStream(fileBytes);
             Workbook workbook = new XSSFWorkbook(xlsxIs)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.rowIterator();
            int rowIdx = 0;

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (rowIdx == 0) {
                    rowIdx++; continue;
                } // 첫 줄 헤더 스킵

                Map<String, String> data = new HashMap<>();

                short totalCells = row.getLastCellNum();

                for (int colIdx = 0; colIdx < totalCells; colIdx++) {
                    Cell cell = row.getCell(colIdx);
                    String cellValue = (cell != null) ? getCellValueString(cell) : "";

                    data.put("cell_" + colIdx, cellValue);

                }
                resultList.add(data);
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
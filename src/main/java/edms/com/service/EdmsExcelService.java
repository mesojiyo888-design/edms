package edms.com.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface EdmsExcelService {

    /**
     * 공통 대용량 엑셀 다운로드 (SXSSF 방식)
     */
    void createExcelDownload(OutputStream os, String[] headers, String[] dataKeys, List<Map<String, Object>> dataList) throws Exception;

    /**
     * 공통 엑셀/CSV 업로드 통합 읽기 (열 순서대로 자동 파싱)
     */
    List<Map<String, String>> readExcelUpload(InputStream is) throws Exception;
}
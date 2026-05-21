package edms.com.web;

import edms.com.service.EdmsExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class EdmsExcelController {

    // 인터페이스 타입으로 주입 (전자정부 표준 스타일에 맞춰 @Resource 사용도 가능)
    @Autowired
    private EdmsExcelService edmsExcelService;

    @GetMapping("/sample/excel")
    public String excelPage() {
        return "test/sampleExcel";
    }

    @GetMapping("/sample/excel/download")
    public void downloadExcel(HttpServletResponse response) {
        try {
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (int i = 1; i <= 500; i++) {
                Map<String, Object> rowData = new HashMap<>();
                rowData.put("id", i);
                rowData.put("name", "홍길동" + i);
                rowData.put("email", "user" + i + "@example.com");
                dataList.add(rowData);
            }

            String[] headers = {"번호", "이름", "이메일"};
            String[] dataKeys = {"id", "name", "email"};

            String fileName = URLEncoder.encode("사용자정보_목록.xlsx", "UTF-8").replaceAll("\\+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            edmsExcelService.createExcelDownload(response.getOutputStream(), headers, dataKeys, dataList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/sample/excel/upload")
    @ResponseBody
    public List<Map<String, String>> uploadExcel(@RequestParam("file") MultipartFile file) {
        List<Map<String, String>> resultList = new ArrayList<>();

        if (file.isEmpty()) {
            return resultList;
        }

        try (InputStream is = file.getInputStream()) {
            resultList = edmsExcelService.readExcelUpload(is);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }
}
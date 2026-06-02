package edms.com.web;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import edms.com.service.EdmsExcelService;
import edms.com.service.MyXmlDataListVO;
import edms.com.service.MyXmlDataVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class EdmsExcelController {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    // 인터페이스 타입으로 주입 (전자정부 표준 스타일에 맞춰 @Resource 사용도 가능)
    @Autowired
    private EdmsExcelService edmsExcelService;

    @GetMapping("/sample/excel")
    public String excelPage() {
        return "test/sampleExcel";
    }

    @GetMapping("/sample/excel/download")
    public void downloadExcel(HttpServletResponse response) throws Exception {
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

    @GetMapping("/sample/excel/xmldownload")
    public void downloadXml(HttpServletResponse response) {
        try {
            List<MyXmlDataVO> dataList = new ArrayList<>();
            for (int i = 1; i <= 500; i++) {
                MyXmlDataVO rowData = new MyXmlDataVO();
                rowData.setId(i);
                rowData.setName("홍길동");
                rowData.setEmail("user" + i + "@example.com");
                dataList.add(rowData);
            }
            MyXmlDataListVO xmlList = new MyXmlDataListVO();
            xmlList.setItems(dataList);

            response.setContentType("application/xml; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"data.xml\"");

            // 마샬링 실행
            JAXBContext context = JAXBContext.newInstance(MyXmlDataListVO.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(xmlList, response.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/sample/excel/downloadCsv")
    public void downloadCsv(HttpServletResponse response) throws Exception {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"data.csv\"");

        // 엑셀에서 한글이 깨지지 않게 하기 위한 BOM(Byte Order Mark) 추가
        response.getOutputStream().write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
        writer.println("ID,이름,이메일"); // 헤더

        List<MyXmlDataVO> dataList = new ArrayList<>();
        for (int i = 1; i <= 500; i++) {
            MyXmlDataVO rowData = new MyXmlDataVO();
            rowData.setId(i);
            rowData.setName("홍길동");
            rowData.setEmail("user" + i + "@example.com");
            dataList.add(rowData);
        }

        for(MyXmlDataVO vo : dataList) {
            writer.printf("%d,%s,%s\n", vo.getId(), vo.getName(), vo.getEmail());
        }
        writer.flush();
        writer.close();
    }

    @PostMapping("/sample/excel/upload")
    @ResponseBody
    public List<Map<String, String>> uploadExcel(@RequestParam("file") MultipartFile file, @RequestParam(value = "brdContent", required = false) String brdContent) {
        List<Map<String, String>> resultList = new ArrayList<>();

        if (file.isEmpty()) {
            return resultList;
        }
        log.debug("업로드된 파일명: {}, 크기: {} bytes, brdContent: {}", file.getOriginalFilename(), file.getSize(), brdContent);
        try (InputStream is = file.getInputStream()) {
            resultList = edmsExcelService.readExcelUpload(is);

            log.debug("엑셀에서 읽은 데이터 수: {}", resultList.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }
}
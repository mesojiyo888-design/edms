package edms.test.web;

import edms.com.search.service.SearchVO;
import edms.sample.service.SampleVO;
import egovframework.exception.EdmsException;
import egovframework.security.EgovUserDetails;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class EdmsTestController {

	@GetMapping("/test/list")
	public String list(@ModelAttribute SampleVO sampleVO, Model model) throws Exception {

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            Map<String, Object> rowData = new HashMap<>();
            rowData.put("name", "홍길동" + i);
            rowData.put("email", "user" + i + "@example.com");
            rowData.put("codeId", i%2 == 0 ? "C" : "W");
            rowData.put("regDate", new SimpleDateFormat("yyyyMMdd").format(new Date()));
            rowData.put("useYn", "Y");
            rowData.put("detail", i);
            rowData.put("status", i%2 == 0 ? "Y" : "N");
            dataList.add(rowData);
        }
        model.addAttribute("dataList", dataList);
		return "test/testList";
	}


    // 1. 처음 JSP 페이지를 열어주는 매핑
    @GetMapping("/test/vue")
    public String vueDemo(Model model) {
        // 기존 방식대로 JSP에 보낼 데이터 탑재
        model.addAttribute("serverMessage", "이것은 Spring Model에서 보낸 문자열입니다.");
        return "test/testVue"; // /WEB-INF/views/vue-demo.jsp를 찾아감
    }

    // 2. Vue 3 화면에서 '서버 데이터 가져오기' 버튼을 눌렀을 때 호출될 비동기 API
    @GetMapping("/api/axois/data")
    @ResponseBody
    public Map<String, Object> getApiData() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "SUCCESS");
        result.put("version", "Java 1.8 / Boot 2.7");
        result.put("description", "Vue 3에서 Axios를 통해 백엔드 데이터를 동적으로 가져왔습니다.");
        return result;
    }

    @GetMapping("/test/gridlist")
    public String gridlist(@ModelAttribute SearchVO searchVO, Model model) throws Exception {

        return "test/testGridList";
    }

    @GetMapping("/board/list")
    @ResponseBody
    public Map<String, Object> getList(SearchVO searchVO) {
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(searchVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(searchVO.getPageSize());
        paginationInfo.setPageSize(10); // 페이지 버튼 개수

        List<Map<String, Object>> dataList = new ArrayList<>();

        int totalCount = 500; //service.selectListCount(searchVO);
        paginationInfo.setTotalRecordCount(totalCount);

        searchVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
        searchVO.setLastIndex(paginationInfo.getLastRecordIndex());

        for (int i = paginationInfo.getFirstRecordIndex(); i <= paginationInfo.getLastRecordIndex(); i++) {
            Map<String, Object> rowData = new HashMap<>();
            rowData.put("name", "홍길동" + i);
            rowData.put("email", "user" + i + "@example.com");
            rowData.put("codeId", i%2 == 0 ? "C" : "W");
            rowData.put("regDate", new SimpleDateFormat("yyyyMMdd").format(new Date()));
            rowData.put("useYn", "Y");
            rowData.put("detail", i);
            rowData.put("status", i%2 == 0 ? "Y" : "N");
            dataList.add(rowData);
        }

        List<?> list = dataList; //service.selectList(searchVO);

        Map<String, Object> map = new HashMap<>();
        map.put("dataList", list);      // 그리드 데이터
        map.put("totalCount", totalCount);
        map.put("paginationInfo", paginationInfo);
        return map;
    }

    @GetMapping("/test/commonValidator")
    public String commonValidator(@ModelAttribute SearchVO searchVO, Model model) throws Exception {

        return "test/commonValidator";
    }

    @GetMapping("/test/msglist")
    public String msglist(@ModelAttribute SearchVO searchVO, Model model) throws Exception {

        return "test/testMsgList";
    }

    @GetMapping("/test/authlist")
    public String authlist(@ModelAttribute SearchVO searchVO, Model model) throws Exception {
        EgovUserDetails loginUser = EgovUserDetails.getCurrentUser();

        if(loginUser != null && loginUser.hasAuthList("A", "PERM_APPROVAL")){
            // A역할로 결재 가능
        }

        if(loginUser != null && !loginUser.isApprovalYn()){
            throw new EdmsException("결재 권한이 없습니다.", "ERR_404", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return "test/testAuthList";
    }
}

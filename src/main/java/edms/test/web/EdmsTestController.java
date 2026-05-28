package edms.test.web;

import egovframework.sample.service.EgovSampleService;
import egovframework.sample.service.SampleVO;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class EdmsTestController {

	@GetMapping("/test/list")
	public String list(@ModelAttribute SampleVO sampleVO, Model model) throws Exception {

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


}

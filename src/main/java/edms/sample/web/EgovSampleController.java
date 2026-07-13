package edms.sample.web;

import javax.validation.Valid;

import edms.com.service.CommonAlarmEventService;
import edms.com.service.CommonJobConfigService;
import edms.com.vo.CommonAlarmEventVo;
import edms.sample.service.impl.EgovSampleServiceImpl;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import edms.sample.service.EgovSampleService;
import edms.sample.service.SampleVO;
import lombok.RequiredArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class EgovSampleController {

    private static final Logger log = LoggerFactory.getLogger(EgovSampleController.class);

	/** EgovSampleService */
	private final EgovSampleService sampleService;

	/** EgovPropertyService */
	private final EgovPropertyService propertiesService;

    /** CommonJobConfigService */
    private final CommonJobConfigService commonJobConfigService;

    private final CommonAlarmEventService commonAlarmEventService;

	/*
	 * @GetMapping("/") public String search(@ModelAttribute SampleVO sampleVO,
	 * Model model) throws Exception { return this.list(sampleVO, model); }
	 */
    @RequestMapping(value = "/sample/list", method = {RequestMethod.GET, RequestMethod.POST})
	public String list(@ModelAttribute SampleVO sampleVO, Model model) throws Exception {
		sampleVO.setPageUnit(propertiesService.getInt("pageUnit"));
		sampleVO.setPageSize(propertiesService.getInt("pageSize"));

		// pagination setting
		PaginationInfo paginationInfo = new PaginationInfo();
		paginationInfo.setCurrentPageNo(sampleVO.getPageIndex());
		paginationInfo.setRecordCountPerPage(sampleVO.getPageUnit());
		paginationInfo.setPageSize(sampleVO.getPageSize());

		sampleVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
		sampleVO.setLastIndex(paginationInfo.getLastRecordIndex());
		sampleVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

		// List
		model.addAttribute("resultList", sampleService.selectSampleList(sampleVO));
		// Count
		paginationInfo.setTotalRecordCount(sampleService.selectSampleListTotCnt(sampleVO));
		// Pagination
		model.addAttribute("paginationInfo", paginationInfo);

		return "egovSampleList";
	}

	@PostMapping("/sample/detail")
	public String detail(@ModelAttribute SampleVO sampleVO, @RequestParam String id, Model model) throws Exception {
		sampleVO.setId(id);
		SampleVO detail = this.sampleService.selectSample(sampleVO);
		model.addAttribute("sampleVO", detail);
		return "egovSampleRegister";
	}

	@GetMapping("/sample/add")
	public String form(@ModelAttribute SampleVO sampleVO) {
		return "egovSampleRegister";
	}

	@PostMapping("/sample/add")
	public String add(@Valid @ModelAttribute SampleVO sampleVO, BindingResult bindingResult) throws Exception {
		if (bindingResult.hasErrors()) {
			return "egovSampleRegister";
		}
		this.sampleService.insertSample(sampleVO);
		return "redirect:/";
	}

	@PostMapping("/sample/update")
	public String update(@Valid @ModelAttribute SampleVO sampleVO, BindingResult bindingResult) throws Exception {
		if (bindingResult.hasErrors()) {
			return "egovSampleRegister";
		}
		this.sampleService.updateSample(sampleVO);
		return "redirect:/";
	}

	@PostMapping("/sample/delete")
	public String delete(@ModelAttribute SampleVO sampleVO) throws Exception {
		this.sampleService.deleteSample(sampleVO);
		return "redirect:/";
	}

    @RequestMapping(value = "/readme")
    public String readme(Model model) throws Exception {

        CommonAlarmEventVo vo1 = new CommonAlarmEventVo("0001", "ALARM", "1번 메시지");
        commonAlarmEventService.sendAlarm(vo1);

        CommonAlarmEventVo vo2 = new CommonAlarmEventVo("0002", "DOCUMENT", "2번 메시지");
        commonAlarmEventService.sendAlarm(vo2);

        List<EgovMap> jobConfig = commonJobConfigService.getJobConfig();

        for (EgovMap info : jobConfig) {
            log.debug("@@@ jobConfig:  {}", info);
        }

        return "readme";
    }
}

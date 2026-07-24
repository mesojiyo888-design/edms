package edms.batch.web;

import edms.batch.config.BatchServerProperties;
import edms.batch.service.BatchApiService;
import edms.batch.service.BatchRunHistVo;
import edms.batch.service.BatchScheduleVo;
import edms.batch.service.BatchToggleItem;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 배치 관리 화면 컨트롤러 + 배치서버(별도 프로젝트, 포트 8081) 프록시.
 *
 * - 화면(JSP) 라우팅: /batch/list, /batch/form, /batch/history
 * - AJAX 프록시: /batch/api/** → BatchApiService(RestTemplate)를 통해 배치서버로 위임
 *
 * 브라우저는 배치서버(8081)를 직접 호출하지 않고 반드시 이 컨트롤러를 경유한다
 * (CORS 회피 + 배치서버 완전 분리 대비).
 */
@Controller
@RequiredArgsConstructor
public class BatchController {

    private final BatchApiService batchApiService;
    private final BatchServerProperties batchServerProperties;

    // ─────────────────────────────────────────────
    // 화면 라우팅
    // ─────────────────────────────────────────────

    /** 배치관리 목록 화면 */
    @GetMapping("/batch/list")
    public String list() {
        return "batch/batchList";
    }

    /** 등록/수정 팝업 화면 (ComMsg.modalUrl로 로드). jobName 파라미터가 있으면 수정 모드 */
    @GetMapping("/batch/form")
    public String form(@RequestParam(required = false) String jobName, Model model) {
        model.addAttribute("jobName", jobName);
        return "batch/batchFormPopup";
    }

    /** 이력 조회 팝업 화면 (ComMsg.modalUrl로 로드). jobName 파라미터가 있으면 배치별 고정 조회 */
    @GetMapping("/batch/history")
    public String history(@RequestParam(required = false) String jobName, Model model) {
        model.addAttribute("jobName", jobName);
        return "batch/batchHistoryPopup";
    }

    // ─────────────────────────────────────────────
    // AJAX 프록시 API
    // ─────────────────────────────────────────────

    /**
     * 목록 그리드 데이터 (페이징). ToastGrid.search()가 GET으로 pageIndex/pageSize를 붙여 호출하고,
     * 응답은 /test/gridlist(board/list)와 동일하게 { dataList: [...], paginationInfo: {...} } 형태로 내려준다.
     * BatchScheduleVo가 SearchVO를 상속하므로 pageIndex/pageSize는 @ModelAttribute로 자동 바인딩된다.
     * perPage(페이지당 건수)는 화면(ToastGrid pageOptions.perPage)에서 결정해 pageSize로 넘어오므로,
     * 추후 사용자별 설정 화면에서 이 값을 동적으로 바꿔도 그대로 반영된다.
     */
    @GetMapping("/batch/api/schedules")
    @ResponseBody
    public Map<String, Object> getSchedules(@ModelAttribute BatchScheduleVo searchVO) {
        Map<String, Object> apiRes = batchApiService.selectAllSchedules(searchVO.getPageIndex(), searchVO.getPageSize());
        return toGridResult(apiRes, searchVO.getPageIndex(), searchVO.getPageSize());
    }

    /**
     * 수동 실행. 배치서버가 여러 대일 수 있어 요청 바디로 대상 서버 URL을 받는다.
     * body 예: { "serverUrl": "http://10.0.0.11:8081" } / 비우면 기본 서버(batch.api.base-url) 사용
     */
    @PostMapping("/batch/api/jobs/{jobName}/run")
    @ResponseBody
    public Map<String, Object> runJob(@PathVariable String jobName,
                                       @RequestBody(required = false) Map<String, String> body) {
        String serverUrl = body != null ? body.get("serverUrl") : null;
        return batchApiService.runJob(jobName, serverUrl);
    }

    /**
     * 화면(select2)에서 배치서버를 선택할 수 있도록 설정된 서버 목록 + 기본 서버 URL을 내려준다.
     * batch.api.servers가 비어있으면 servers는 빈 배열이 오고, 화면에서는 defaultUrl을 기본값으로 채운다.
     */
    @GetMapping("/batch/api/server-options")
    @ResponseBody
    public Map<String, Object> getServerOptions() {
        Map<String, Object> result = new HashMap<>();
        result.put("defaultUrl", batchServerProperties.getBaseUrl());
        result.put("servers", batchServerProperties.getServers());
        return result;
    }

    /** 신규 등록 */
    @PostMapping("/batch/api/schedules")
    @ResponseBody
    public Map<String, Object> createSchedule(@RequestBody BatchScheduleVo vo) {
        return batchApiService.createSchedule(vo);
    }

    /** 수정 */
    @PutMapping("/batch/api/schedules/{jobName}")
    @ResponseBody
    public Map<String, Object> updateSchedule(@PathVariable String jobName, @RequestBody BatchScheduleVo vo) {
        return batchApiService.updateSchedule(jobName, vo);
    }

    /**
     * 삭제 (NATIVE 포함 전부 삭제 가능). 존재하지 않는 jobName이면 배치서버가 400을 리턴 →
     * 그대로 success:false 전달.
     */
    @DeleteMapping("/batch/api/schedules/{jobName}")
    @ResponseBody
    public Map<String, Object> deleteSchedule(@PathVariable String jobName) {
        return batchApiService.deleteSchedule(jobName);
    }

    /** 단건 사용여부 즉시 토글 (instant 모드) */
    @PutMapping("/batch/api/schedules/{jobName}/toggle")
    @ResponseBody
    public Map<String, Object> toggleSchedule(@PathVariable String jobName) {
        return batchApiService.toggleSchedule(jobName);
    }

    /** 다건 사용여부 일괄 변경 (batch 모드 저장버튼) */
    @PutMapping("/batch/api/schedules/toggle-bulk")
    @ResponseBody
    public Map<String, Object> toggleBulk(@RequestBody List<BatchToggleItem> items) {
        return batchApiService.toggleBulk(items);
    }

    /**
     * 배치별 이력 조회 (기간/상태 검색 + 페이징). BatchRunHistVo가 SearchVO를 상속하므로
     * pageIndex/pageSize/startDt/endDt/status가 @ModelAttribute로 자동 바인딩된다.
     * (status는 BatchRunHistVo에 이미 있는 응답용 필드를 검색조건으로도 재사용)
     */
    @GetMapping("/batch/api/jobs/{jobName}/history")
    @ResponseBody
    public Map<String, Object> getHistory(@PathVariable String jobName, @ModelAttribute BatchRunHistVo searchVO) {
        Map<String, Object> apiRes = batchApiService.selectHistory(
                jobName, searchVO.getStartDt(), searchVO.getEndDt(), searchVO.getStatus(),
                searchVO.getPageIndex(), searchVO.getPageSize());
        return toGridResult(apiRes, searchVO.getPageIndex(), searchVO.getPageSize());
    }

    /**
     * 전체 이력 조회 (jobName/기간/상태 검색 + 페이징).
     * jobName/status 모두 BatchRunHistVo에 이미 있는 응답용 필드를 검색조건으로 재사용한다.
     */
    @GetMapping("/batch/api/history")
    @ResponseBody
    public Map<String, Object> getAllHistory(@ModelAttribute BatchRunHistVo searchVO) {
        Map<String, Object> apiRes = batchApiService.selectAllHistory(
                searchVO.getJobName(), searchVO.getStartDt(), searchVO.getEndDt(), searchVO.getStatus(),
                searchVO.getPageIndex(), searchVO.getPageSize());
        return toGridResult(apiRes, searchVO.getPageIndex(), searchVO.getPageSize());
    }

    /**
     * 배치서버 응답({ dataList, totalCount })을 ToastGrid가 기대하는 형태로 변환한다.
     * /test/gridlist(board/list) 예제와 동일하게 eGovFrame PaginationInfo를 사용해
     * 화면단 페이징 버튼(toastGrid.js의 renderPagination)을 그대로 재사용할 수 있게 한다.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> toGridResult(Map<String, Object> apiRes, int pageIndex, int pageSize) {
        List<Object> dataList = apiRes.get("dataList") != null
                ? (List<Object>) apiRes.get("dataList")
                : java.util.Collections.emptyList();
        int totalCount = apiRes.get("totalCount") != null
                ? ((Number) apiRes.get("totalCount")).intValue()
                : 0;

        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(pageIndex);
        paginationInfo.setRecordCountPerPage(pageSize);
        paginationInfo.setPageSize(10); // 페이지 버튼(1,2,3...) 개수
        paginationInfo.setTotalRecordCount(totalCount);

        Map<String, Object> result = new HashMap<>();
        result.put("dataList", dataList);
        result.put("paginationInfo", paginationInfo);
        return result;
    }
}

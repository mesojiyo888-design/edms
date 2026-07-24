package edms.batch.service.impl;

import edms.batch.service.BatchApiService;
import edms.batch.service.BatchScheduleVo;
import edms.batch.service.BatchToggleItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 배치서버(별도 프로젝트, 포트 8081)를 RestTemplate으로 호출하는 구현체.
 * 브라우저는 이 서비스를 직접 호출하지 않고, edms.batch.web.BatchController(프록시)를 경유한다.
 * base-url은 application.yml의 batch.api.base-url 프로퍼티로 관리한다.
 */
@Service
public class BatchApiServiceImpl implements BatchApiService {

    private static final Logger log = LoggerFactory.getLogger(BatchApiServiceImpl.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final RestTemplate restTemplate;

    @Value("${batch.api.base-url}")
    private String baseUrl;

    public BatchApiServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Map<String, Object> runJob(String jobName, String serverUrl) {
        String targetBase = (serverUrl != null && !serverUrl.trim().isEmpty()) ? serverUrl.trim() : baseUrl;
        String url = targetBase + "/api/batch/jobs/{jobName}/run";
        return exchangeForMap(url, HttpMethod.POST, null, jobName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> selectAllSchedules(int pageIndex, int pageSize) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/batch/schedules/all")
                .queryParam("pageIndex", pageIndex)
                .queryParam("pageSize", pageSize)
                .toUriString();
        ResponseEntity<Map> res = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> body = res.getBody();
        return body != null ? body : emptyPageResult();
    }

    @Override
    public Map<String, Object> createSchedule(BatchScheduleVo vo) {
        String url = baseUrl + "/api/batch/schedules";
        return exchangeForMap(url, HttpMethod.POST, vo);
    }

    @Override
    public Map<String, Object> updateSchedule(String jobName, BatchScheduleVo vo) {
        String url = baseUrl + "/api/batch/schedules/{jobName}";
        return exchangeForMap(url, HttpMethod.PUT, vo, jobName);
    }

    @Override
    public Map<String, Object> deleteSchedule(String jobName) {
        String url = baseUrl + "/api/batch/schedules/{jobName}";
        return exchangeForMap(url, HttpMethod.DELETE, null, jobName);
    }

    @Override
    public Map<String, Object> toggleSchedule(String jobName) {
        String url = baseUrl + "/api/batch/schedules/{jobName}/toggle";
        return exchangeForMap(url, HttpMethod.PUT, null, jobName);
    }

    @Override
    public Map<String, Object> toggleBulk(List<BatchToggleItem> items) {
        String url = baseUrl + "/api/batch/schedules/toggle-bulk";
        return exchangeForMap(url, HttpMethod.PUT, items);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> selectHistory(String jobName, String startDt, String endDt, String status, int pageIndex, int pageSize) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/api/batch/jobs/{jobName}/history")
                .queryParam("pageIndex", pageIndex)
                .queryParam("pageSize", pageSize);
        if (startDt != null && !startDt.trim().isEmpty()) builder.queryParam("startDt", startDt);
        if (endDt != null && !endDt.trim().isEmpty()) builder.queryParam("endDt", endDt);
        if (status != null && !status.trim().isEmpty()) builder.queryParam("status", status);
        String url = builder.buildAndExpand(jobName).toUriString();

        ResponseEntity<Map> res = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> body = res.getBody();
        return body != null ? body : emptyPageResult();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> selectAllHistory(String jobName, String startDt, String endDt, String status, int pageIndex, int pageSize) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/batch/history")
                .queryParam("pageIndex", pageIndex)
                .queryParam("pageSize", pageSize);
        if (jobName != null && !jobName.trim().isEmpty()) builder.queryParam("jobName", jobName);
        if (startDt != null && !startDt.trim().isEmpty()) builder.queryParam("startDt", startDt);
        if (endDt != null && !endDt.trim().isEmpty()) builder.queryParam("endDt", endDt);
        if (status != null && !status.trim().isEmpty()) builder.queryParam("status", status);
        String url = builder.toUriString();

        ResponseEntity<Map> res = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> body = res.getBody();
        return body != null ? body : emptyPageResult();
    }

    /** 배치서버 응답이 비정상적으로 비어있을 때(null body) 대비한 빈 페이지 결과 */
    private Map<String, Object> emptyPageResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("dataList", java.util.Collections.emptyList());
        result.put("totalCount", 0);
        return result;
    }

    /**
     * 배치서버 호출 공통 처리. 배치서버가 400 Bad Request(Map body)를 리턴하는 경우가 있어
     * (예: 이미 등록된 jobName으로 신규 등록 시도, 존재하지 않는 jobName 수정/삭제/토글 시도)
     * 예외 바디를 그대로 파싱해서 success:false 형태로 돌려준다.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> exchangeForMap(String url, HttpMethod method, Object body, Object... uriVars) {
        try {
            // body가 있을 때는 Content-Type을 명시적으로 application/json으로 지정한다.
            // (RestTemplate이 자동으로 붙여줄 것으로 기대했다가 415가 나는 경우가 있어 방어적으로 처리)
            HttpEntity<Object> entity;
            if (body != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                entity = new HttpEntity<>(body, headers);
            } else {
                entity = new HttpEntity<>(null);
            }
            ResponseEntity<Map> res = restTemplate.exchange(url, method, entity, Map.class, uriVars);
            return res.getBody();
        } catch (HttpClientErrorException e) {
            log.warn("배치서버 호출 실패: {} {} (uriVars={}) -> {} / body={}",
                    method, url, uriVars, e.getStatusCode(), e.getResponseBodyAsString());
            Map<String, Object> err;
            try {
                String responseBody = e.getResponseBodyAsString();
                err = (responseBody == null || responseBody.trim().isEmpty())
                        ? null
                        : OBJECT_MAPPER.readValue(responseBody, Map.class);
            } catch (Exception parseEx) {
                err = null;
            }
            if (err == null) {
                err = new HashMap<>();
                err.put("success", false);
                err.put("message", "배치서버 호출 중 오류가 발생했습니다: " + e.getStatusCode());
            }
            return err;
        }
    }
}

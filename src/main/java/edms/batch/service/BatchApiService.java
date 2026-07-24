package edms.batch.service;

import java.util.List;
import java.util.Map;

/**
 * 배치서버(별도 프로젝트, 포트 8081) REST API 호출 서비스.
 * nibpm은 이 서비스를 통해서만 배치서버와 통신하며, DB에는 직접 접근하지 않는다.
 */
public interface BatchApiService {

    /**
     * 수동 실행 - POST /api/batch/jobs/{jobName}/run
     * @param serverUrl 화면에서 선택/직접입력한 대상 배치서버 URL. null/공백이면 batch.api.base-url(기본 서버) 사용
     */
    Map<String, Object> runJob(String jobName, String serverUrl);

    /**
     * 스케줄 전체 목록(활성+비활성, 페이징) - GET /api/batch/schedules/all?pageIndex=&pageSize=
     * 배치서버 원본 응답을 그대로 반환한다: { dataList: [...], totalCount: N }
     */
    Map<String, Object> selectAllSchedules(int pageIndex, int pageSize);

    /** 스케줄 등록 - POST /api/batch/schedules */
    Map<String, Object> createSchedule(BatchScheduleVo vo);

    /** 스케줄 수정 - PUT /api/batch/schedules/{jobName} */
    Map<String, Object> updateSchedule(String jobName, BatchScheduleVo vo);

    /** 스케줄 삭제 - DELETE /api/batch/schedules/{jobName} (NATIVE는 배치서버에서 400) */
    Map<String, Object> deleteSchedule(String jobName);

    /** 단건 사용여부 즉시 토글 - PUT /api/batch/schedules/{jobName}/toggle */
    Map<String, Object> toggleSchedule(String jobName);

    /** 다건 사용여부 일괄 변경 - PUT /api/batch/schedules/toggle-bulk */
    Map<String, Object> toggleBulk(List<BatchToggleItem> items);

    /**
     * 배치별 이력(기간/상태 검색 + 페이징) - GET /api/batch/jobs/{jobName}/history?startDt=&endDt=&status=&pageIndex=&pageSize=
     * 배치서버 원본 응답을 그대로 반환한다: { dataList: [...], totalCount: N }
     */
    Map<String, Object> selectHistory(String jobName, String startDt, String endDt, String status, int pageIndex, int pageSize);

    /**
     * 전체 이력(jobName/기간/상태 검색 + 페이징) - GET /api/batch/history?jobName=&startDt=&endDt=&status=&pageIndex=&pageSize=
     * 배치서버 원본 응답을 그대로 반환한다: { dataList: [...], totalCount: N }
     */
    Map<String, Object> selectAllHistory(String jobName, String startDt, String endDt, String status, int pageIndex, int pageSize);
}

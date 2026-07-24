package edms.batch.service;

import edms.com.search.service.SearchVO;
import lombok.Getter;
import lombok.Setter;

/**
 * 배치서버 BatchRunHistVo와 필드를 맞춘 이력 조회용 DTO.
 *
 * SearchVO를 상속받아 목록 화면(이력 조회) 요청 파라미터 바인딩에도 함께 사용한다.
 * - pageIndex/pageSize: SearchVO 상속 필드 (페이징)
 * - startDt/endDt: 이력 조회 기간 검색 조건 (yyyy-MM-dd HH:mm:ss)
 * (startTime/endTime은 응답으로 내려오는 실행 시각이라 검색조건 startDt/endDt와는 별개 필드)
 */
@Getter
@Setter
public class BatchRunHistVo extends SearchVO {

    private Long runId;
    private String jobName;
    /** null이면 Job 전체 실행 */
    private String stepName;
    private String triggeredBy;
    /** AUTO | MANUAL */
    private String triggerType;
    /** 배치서버가 Date(timestamp 또는 ISO 문자열)로 내려줄 수 있어 Object로 느슨하게 받음 */
    private Object startTime;
    private Object endTime;
    private String status;
    private String errorMsg;
    private Long executionId;
    private String instanceId;

    /** 이력 조회 검색조건 - 시작일 (yyyy-MM-dd HH:mm:ss) */
    private String startDt;
    /** 이력 조회 검색조건 - 종료일 (yyyy-MM-dd HH:mm:ss) */
    private String endDt;
}

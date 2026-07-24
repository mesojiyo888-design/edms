package edms.batch.service;

import edms.com.search.service.SearchVO;
import lombok.Getter;
import lombok.Setter;

/**
 * 배치서버(별도 프로젝트, 포트 8081)의 BatchScheduleVo와 필드를 맞춘 DTO.
 * nibpm은 배치서버 DB에 직접 접근하지 않고, 이 VO로 REST 요청/응답을 주고받기만 한다.
 */
@Getter
@Setter
public class BatchScheduleVo extends SearchVO {

    /** Job 이름 (PK). 신규 등록시에만 입력 가능, 수정시 readonly */
    private String jobName;

    /** cron 표현식 (필수) */
    private String cronExpr;

    /** 사용여부 Y/N */
    private String useYn;

    private String description;

    /** NATIVE(하드코딩) | SQL | PROCEDURE | API */
    private String taskletType;

    /** SQL문 / 프로시저 호출 스펙 / API URL */
    private String taskletConfig;

    /** 배치서버가 Date(timestamp 또는 ISO 문자열)로 내려줄 수 있어 Object로 느슨하게 받음 */
    private Object regDt;
    private Object updDt;

    private String modifiedBy;
}

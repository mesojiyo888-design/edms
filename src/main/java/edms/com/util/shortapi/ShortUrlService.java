package edms.com.util.shortapi;

import edms.com.util.shortapi.dto.KuttLinkListResponseDto;
import edms.com.util.shortapi.dto.KuttLinkRequestDto;
import edms.com.util.shortapi.dto.KuttLinkResponseDto;
// [DB 연동 시 주석 해제] import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
// [DB 연동 시 주석 해제] import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 단축 URL 서비스
 *
 * 역할:
 * 1. Kutt API 호출 (KuttApiClient 위임)
 * 2. 자체 DB 저장/조회/수정/삭제 (MyBatis)
 * 3. 프로그램(화면) ↔ 단축 URL 매핑 관리
 *
 * [DB 테이블: SHORT_URL_MAPPING]
 * ┌─────────────────────────────────────────────────────────────┐
 * │ 컬럼명          │ 타입         │ 설명                        │
 * ├─────────────────────────────────────────────────────────────┤
 * │ SEQ            │ NUMBER(PK)   │ 자동 증가 PK                │
 * │ PROGRAM_ID     │ VARCHAR2(100)│ 프로그램 식별자              │
 * │ PROGRAM_URL    │ VARCHAR2(500)│ 프로그램 원본 URL            │
 * │ KUTT_ID        │ VARCHAR2(100)│ Kutt 내부 UUID              │
 * │ SHORT_URL      │ VARCHAR2(200)│ 단축 URL (link)             │
 * │ KUTT_ADDRESS   │ VARCHAR2(100)│ 단축 주소 (address)         │
 * │ DESCRIPTION    │ VARCHAR2(500)│ 설명                        │
 * │ EXPIRE_IN      │ VARCHAR2(100)│ 만료 기간 ("1 days" 형식)   │
 * │ USE_YN         │ CHAR(1)      │ 사용 여부 (Y/N)             │
 * │ REG_DT         │ DATE         │ 등록 일시                   │
 * │ MOD_DT         │ DATE         │ 수정 일시                   │
 * └─────────────────────────────────────────────────────────────┘
 *
 * =====================================================================
 * ★ DB 미연동 상태 안내 ★
 *
 * 현재 Oracle DB는 연동되어 있지 않습니다.
 * Kutt API 호출은 실제로 이루어지며, 생성/조회(Kutt 직접)는 정상 동작합니다.
 *
 * DB 없이 동작하는 기능:
 *   ✅ 생성 (createShortUrl)      → Kutt API 실호출, DB insert만 skip
 *   ✅ Kutt 링크 목록 조회 (getKuttLinks) → Kutt API 실호출
 *   ⚠️ 단건 조회 (getShortUrlByProgramId) → DB 없으므로 항상 null (등록 없음)
 *   ⚠️ 목록 조회 (getShortUrlList)        → DB 없으므로 빈 목록
 *   ❌ 수정 (updateShortUrl)       → kuttId를 DB에서 가져와야 하므로 오류 반환
 *   ❌ 삭제 (deleteShortUrl)       → kuttId를 DB에서 가져와야 하므로 오류 반환
 *
 * DB 연동 복구 방법 (3단계):
 *   1. 각 메서드 내 [DB 없이 skip] 블록을 주석 처리
 *   2. [DB 연동 시 주석 해제] 블록의 주석 제거
 *   3. 클래스 상단 @Autowired SqlSession, @Transactional 주석 해제
 * =====================================================================
 */
@Service
public class ShortUrlService {

    private static final Logger log = LoggerFactory.getLogger(ShortUrlService.class);

    private static final String MAPPER_NS = "shortapi.ShortUrlMapper";

    @Autowired
    private KuttApiClient kuttApiClient;

    /* [DB 연동 시 주석 해제] ================================================
     * Oracle DB와 MyBatis 연결이 준비된 후 아래 주석을 제거하세요.
     *
     * @Autowired
     * private SqlSession sqlSession;
     * ================================================================== */

    // =========================================================
    // 1. 단축 URL 생성
    // =========================================================

    /**
     * 단축 URL 생성 및 DB 저장
     *
     * @param programId   프로그램 ID
     * @param programUrl  프로그램 원본 URL
     * @param description 설명 (선택)
     * @param expireIn    만료 기간 (선택, 예: "7 days") — null이면 만료 없음
     * @return Map(shortUrl, kuttId, programId, expireIn)
     */
    // [DB 연동 시 주석 해제] @Transactional
    public Map<String, Object> createShortUrl(String programId,
                                               String programUrl,
                                               String description,
                                               String expireIn) {
        log.info("[ShortUrlService] createShortUrl - programId: {}, url: {}", programId, programUrl);

        /* [DB 없이 skip] =====================================================
         * DB 연동 시: 중복 등록 방지를 위해 기존 단축 URL 여부를 먼저 확인합니다.
         * DB 준비 후 이 블록을 제거하고 아래 [DB 연동 시 주석 해제] 블록을 활성화하세요.
         *
         * [DB 연동 시 주석 해제]
         * Map<String, Object> existing = getShortUrlByProgramId(programId);
         * if (existing != null) {
         *     log.info("[ShortUrlService] 이미 등록된 단축 URL 존재 - programId: {}", programId);
         *     return existing;
         * }
         * ==================================================================== */

        // ── Kutt API 실호출 (DB 연동 여부와 무관하게 항상 실행) ──────────────
        KuttLinkRequestDto requestDto = new KuttLinkRequestDto();
        requestDto.setTarget(programUrl);
        requestDto.setDescription(description);
        if (expireIn != null && !expireIn.trim().isEmpty()) {
            requestDto.setExpire_in(expireIn);
        }

        KuttLinkResponseDto apiResponse = kuttApiClient.createLink(requestDto);
        log.info("[ShortUrlService] Kutt 생성 완료 - kuttId: {}, link: {}",
                apiResponse.getId(), apiResponse.getLink());

        /* [DB 없이 skip] =====================================================
         * DB 연동 시: Kutt 응답을 DB에 저장합니다.
         * DB 준비 후 이 블록을 제거하고 아래 [DB 연동 시 주석 해제] 블록을 활성화하세요.
         *
         * [DB 연동 시 주석 해제]
         * Map<String, Object> param = new HashMap<>();
         * param.put("programId",   programId);
         * param.put("programUrl",  programUrl);
         * param.put("kuttId",      apiResponse.getId());
         * param.put("shortUrl",    apiResponse.getLink());
         * param.put("kuttAddress", apiResponse.getAddress());
         * param.put("description", description);
         * param.put("expireIn",    expireIn);
         * sqlSession.insert(MAPPER_NS + ".insertShortUrl", param);
         * ==================================================================== */

        Map<String, Object> result = new HashMap<>();
        result.put("shortUrl",  apiResponse.getLink());
        result.put("kuttId",    apiResponse.getId());
        result.put("programId", programId);
        result.put("expireIn",  expireIn);
        return result;
    }

    // =========================================================
    // 2. 단축 URL 조회
    // =========================================================

    /**
     * 프로그램 ID로 단축 URL 매핑 정보 조회
     *
     * @param programId 프로그램 ID
     * @return DB 매핑 정보 Map, 없으면 null
     */
    public Map<String, Object> getShortUrlByProgramId(String programId) {
        log.info("[ShortUrlService] getShortUrlByProgramId - programId: {}", programId);

        /* [DB 없이 skip] =====================================================
         * DB가 없으므로 null을 반환합니다 (등록된 URL 없음 처리).
         * 모달의 "기존 조회" 버튼 클릭 시 "등록된 단축 URL이 없습니다" 메시지가 표시됩니다.
         * DB 준비 후 이 블록을 제거하고 아래 [DB 연동 시 주석 해제] 블록을 활성화하세요.
         * ==================================================================== */
        log.info("[ShortUrlService][DB미연동] DB 조회 skip - null 반환");
        return null;

        /* [DB 연동 시 주석 해제] ================================================
         * return sqlSession.selectOne(MAPPER_NS + ".selectShortUrlByProgramId", programId);
         * ================================================================== */
    }

    /**
     * 단축 URL 목록 조회 (전체 또는 조건별)
     *
     * @param condition 조건 Map (programId, useYn 등)
     * @return 목록
     */
    public List<Map<String, Object>> getShortUrlList(Map<String, Object> condition) {
        log.info("[ShortUrlService] getShortUrlList - condition: {}", condition);

        /* [DB 없이 skip] =====================================================
         * DB가 없으므로 빈 목록을 반환합니다.
         * DB 준비 후 이 블록을 제거하고 아래 [DB 연동 시 주석 해제] 블록을 활성화하세요.
         * ==================================================================== */
        log.info("[ShortUrlService][DB미연동] DB 조회 skip - 빈 목록 반환");
        return new ArrayList<>();

        /* [DB 연동 시 주석 해제] ================================================
         * return sqlSession.selectList(MAPPER_NS + ".selectShortUrlList", condition);
         * ================================================================== */
    }

    /**
     * Kutt 서버에서 링크 목록 조회 (Kutt API 직접 호출 — DB 불필요)
     *
     * @param limit 페이지 당 수
     * @param skip  오프셋
     * @return Kutt API 응답
     */
    public KuttLinkListResponseDto getKuttLinks(int limit, int skip) {
        // Kutt API 직접 호출이므로 DB 없이도 동작합니다.
        return kuttApiClient.getLinks(limit, skip);
    }

    // =========================================================
    // 3. 단축 URL 수정
    // =========================================================

    /**
     * 단축 URL 수정
     *
     * [keepTarget 정책]
     * - true  : 원본 URL 유지 (다른 속성만 수정)
     * - false : newTarget으로 대상 URL 변경
     *
     * ⚠️ DB 미연동 상태에서는 kuttId를 조회할 수 없어 수정이 불가합니다.
     *    DB 연동 후 사용하세요.
     *
     * @param programId   프로그램 ID
     * @param newTarget   변경할 대상 URL (keepTarget=false 시 적용)
     * @param description 변경할 설명
     * @param expireIn    변경할 만료 기간 (null이면 만료 제거)
     * @param keepTarget  true면 원본 URL 유지
     * @return 수정 결과 Map
     */
    // [DB 연동 시 주석 해제] @Transactional
    public Map<String, Object> updateShortUrl(String programId,
                                               String newTarget,
                                               String description,
                                               String expireIn,
                                               boolean keepTarget) {
        log.info("[ShortUrlService] updateShortUrl - programId: {}, keepTarget: {}", programId, keepTarget);

        /* [DB 없이 skip] =====================================================
         * 수정은 DB에 저장된 kuttId가 있어야 Kutt API를 호출할 수 있습니다.
         * DB 없이는 수정이 불가하므로 명확한 오류를 반환합니다.
         * DB 준비 후 이 블록을 제거하고 아래 [DB 연동 시 주석 해제] 블록을 활성화하세요.
         * ==================================================================== */
        log.warn("[ShortUrlService][DB미연동] 수정 불가 - kuttId 조회를 위해 DB 연동이 필요합니다.");
        throw new IllegalStateException(
            "DB 미연동 상태에서는 수정이 불가합니다. " +
            "Oracle DB 연동 후 사용하세요. (programId: " + programId + ")"
        );

        /* [DB 연동 시 주석 해제] ================================================
         * // 1) DB에서 매핑 정보 조회 (kuttId 필요)
         * Map<String, Object> mapping = getShortUrlByProgramId(programId);
         * if (mapping == null) {
         *     throw new IllegalArgumentException("단축 URL 매핑 정보 없음. programId: " + programId);
         * }
         *
         * String kuttId = (String) mapping.get("kuttId");
         * String originalTarget = (String) mapping.get("programUrl");
         *
         * // 2) 수정 요청 DTO 구성
         * KuttLinkRequestDto requestDto = new KuttLinkRequestDto();
         * requestDto.setDescription(description);
         * requestDto.setExpire_in(expireIn);
         *
         * if (!keepTarget && newTarget != null && !newTarget.trim().isEmpty()) {
         *     requestDto.setTarget(newTarget);
         * } else {
         *     requestDto.setTarget(originalTarget); // keepTarget=true: 원본 URL 유지
         * }
         *
         * // 3) Kutt API 호출 (PATCH)
         * KuttLinkResponseDto apiResponse = kuttApiClient.updateLink(kuttId, requestDto, keepTarget);
         * log.info("[ShortUrlService] Kutt 수정 완료 - kuttId: {}", kuttId);
         *
         * // 4) DB 업데이트
         * Map<String, Object> param = new HashMap<>();
         * param.put("programId",   programId);
         * param.put("description", description);
         * param.put("expireIn",    expireIn);
         * if (!keepTarget && newTarget != null && !newTarget.trim().isEmpty()) {
         *     param.put("programUrl", newTarget);
         * }
         * sqlSession.update(MAPPER_NS + ".updateShortUrl", param);
         *
         * Map<String, Object> result = new HashMap<>();
         * result.put("shortUrl",   apiResponse.getLink());
         * result.put("kuttId",     kuttId);
         * result.put("programId",  programId);
         * result.put("expireIn",   expireIn);
         * result.put("keepTarget", keepTarget);
         * return result;
         * ================================================================== */
    }

    // =========================================================
    // 4. 단축 URL 삭제
    // =========================================================

    /**
     * 단축 URL 삭제 (Kutt 서버 + DB 모두 삭제)
     *
     * 삭제 후 해당 단축 URL로 접근하면 404 반환됩니다.
     *
     * ⚠️ DB 미연동 상태에서는 kuttId를 조회할 수 없어 삭제가 불가합니다.
     *    DB 연동 후 사용하세요.
     *
     * @param programId 프로그램 ID
     * @return 삭제 성공 여부
     */
    // [DB 연동 시 주석 해제] @Transactional
    public boolean deleteShortUrl(String programId) {
        log.info("[ShortUrlService] deleteShortUrl - programId: {}", programId);

        /* [DB 없이 skip] =====================================================
         * 삭제는 DB에 저장된 kuttId가 있어야 Kutt API를 호출할 수 있습니다.
         * DB 없이는 삭제가 불가하므로 명확한 오류를 반환합니다.
         * DB 준비 후 이 블록을 제거하고 아래 [DB 연동 시 주석 해제] 블록을 활성화하세요.
         * ==================================================================== */
        log.warn("[ShortUrlService][DB미연동] 삭제 불가 - kuttId 조회를 위해 DB 연동이 필요합니다.");
        throw new IllegalStateException(
            "DB 미연동 상태에서는 삭제가 불가합니다. " +
            "Oracle DB 연동 후 사용하세요. (programId: " + programId + ")"
        );

        /* [DB 연동 시 주석 해제] ================================================
         * // 1) DB에서 매핑 정보 조회 (kuttId 필요)
         * Map<String, Object> mapping = getShortUrlByProgramId(programId);
         * if (mapping == null) {
         *     log.warn("[ShortUrlService] 삭제 대상 없음 - programId: {}", programId);
         *     return false;
         * }
         *
         * String kuttId = (String) mapping.get("kuttId");
         *
         * // 2) Kutt 서버에서 삭제
         * boolean kuttDeleted = kuttApiClient.deleteLink(kuttId);
         * if (!kuttDeleted) {
         *     log.warn("[ShortUrlService] Kutt 서버 삭제 실패 - kuttId: {}. DB는 계속 진행.", kuttId);
         * }
         *
         * // 3) DB 논리 삭제 (Kutt 실패해도 DB는 삭제 — 고아 데이터 방지)
         * sqlSession.update(MAPPER_NS + ".deleteShortUrl", programId);
         * log.info("[ShortUrlService] DB 삭제 완료 - programId: {}", programId);
         *
         * return true;
         * ================================================================== */
    }

    /**
     * 프로그램 삭제 연동: 해당 프로그램의 단축 URL 일괄 삭제
     * (프로그램 삭제 시 연계 호출용)
     *
     * @param programId 프로그램 ID
     */
    // [DB 연동 시 주석 해제] @Transactional
    public void deleteShortUrlByProgramDelete(String programId) {
        log.info("[ShortUrlService] 프로그램 삭제 연동 - programId: {}", programId);
        deleteShortUrl(programId);
    }

    // =========================================================
    // ★ KUTT 직접 테스트 전용 메서드 (DB 불필요)
    //   - DB에 저장된 kuttId 없이 Kutt API를 직접 호출합니다.
    //   - 샘플 페이지 테스트 및 Kutt 서버 연동 확인용으로 사용합니다.
    //   - DB 연동 후에는 위쪽 메서드(getShortUrlByProgramId 등)를 사용하세요.
    // =========================================================

    /**
     * [KUTT 직접] kuttId로 단건 링크 조회 (DB 불필요)
     *
     * Kutt API는 단건 GET을 지원하지 않으므로 목록(최대 100건)에서 id 필터링합니다.
     *
     * @param kuttId Kutt 내부 UUID (생성 시 응답의 id 필드)
     * @return Kutt 응답 Map, 없으면 null
     */
    public Map<String, Object> getLinkFromKuttByKuttId(String kuttId) {
        log.info("[ShortUrlService][KUTT직접] getLinkFromKuttByKuttId - kuttId: {}", kuttId);

        KuttLinkResponseDto dto = kuttApiClient.getLinkById(kuttId);
        if (dto == null) {
            log.info("[ShortUrlService][KUTT직접] Kutt에서 해당 링크를 찾지 못했습니다. kuttId: {}", kuttId);
            return null;
        }
        return kuttLinkResponseToMap(dto);
    }

    /**
     * [KUTT 직접] Kutt 링크 목록 조회 (DB 불필요)
     *
     * @param limit 페이지 당 조회 수
     * @param skip  오프셋
     * @return Kutt API 응답 Map (total, limit, skip, data 포함)
     */
    public Map<String, Object> getLinksFromKutt(int limit, int skip) {
        log.info("[ShortUrlService][KUTT직접] getLinksFromKutt - limit: {}, skip: {}", limit, skip);

        KuttLinkListResponseDto dto = kuttApiClient.getLinks(limit, skip);

        Map<String, Object> result = new HashMap<>();
        result.put("total", dto.getTotal());
        result.put("limit", dto.getLimit());
        result.put("skip",  dto.getSkip());

        java.util.List<Map<String, Object>> dataList = new ArrayList<>();
        if (dto.getData() != null) {
            for (KuttLinkResponseDto link : dto.getData()) {
                dataList.add(kuttLinkResponseToMap(link));
            }
        }
        result.put("data", dataList);
        return result;
    }

    /**
     * [KUTT 직접] kuttId로 링크 수정 (DB 불필요)
     *
     * @param kuttId      Kutt 내부 UUID
     * @param newTarget   변경할 대상 URL (keepTarget=false 시 적용)
     * @param description 변경할 설명
     * @param expireIn    변경할 만료 기간 (null이면 만료 제거)
     * @param keepTarget  true면 원본 URL 유지
     * @return 수정된 링크 정보 Map
     */
    public Map<String, Object> updateShortUrlKuttOnly(String kuttId,
                                                       String newTarget,
                                                       String description,
                                                       String expireIn,
                                                       boolean keepTarget) {
        log.info("[ShortUrlService][KUTT직접] updateShortUrlKuttOnly - kuttId: {}, keepTarget: {}", kuttId, keepTarget);

        KuttLinkRequestDto requestDto = new KuttLinkRequestDto();
        requestDto.setDescription(description);
        requestDto.setExpire_in(expireIn);

        // keepTarget=false이고 newTarget이 있을 경우 URL 변경
        // keepTarget=true이면 KuttApiClient.updateLink() 내부에서 기존 target을 조회해 자동 유지
        if (!keepTarget && newTarget != null && !newTarget.trim().isEmpty()) {
            requestDto.setTarget(newTarget);
        }

        KuttLinkResponseDto apiResponse = kuttApiClient.updateLink(kuttId, requestDto, keepTarget);
        log.info("[ShortUrlService][KUTT직접] Kutt 수정 완료 - kuttId: {}, link: {}", kuttId, apiResponse.getLink());

        return kuttLinkResponseToMap(apiResponse);
    }

    /**
     * [KUTT 직접] kuttId로 링크 삭제 (DB 불필요)
     *
     * @param kuttId Kutt 내부 UUID
     * @return 삭제 성공 여부
     */
    public boolean deleteShortUrlKuttOnly(String kuttId) {
        log.info("[ShortUrlService][KUTT직접] deleteShortUrlKuttOnly - kuttId: {}", kuttId);

        boolean deleted = kuttApiClient.deleteLink(kuttId);
        log.info("[ShortUrlService][KUTT직접] Kutt 삭제 결과 - kuttId: {}, deleted: {}", kuttId, deleted);
        return deleted;
    }

    // =========================================================
    // 내부 유틸
    // =========================================================

    /** KuttLinkResponseDto → Map 변환 (JSON 직렬화 편의용) */
    private Map<String, Object> kuttLinkResponseToMap(KuttLinkResponseDto dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("kuttId",      dto.getId());
        map.put("shortUrl",    dto.getLink());
        map.put("address",     dto.getAddress());
        map.put("target",      dto.getTarget());
        map.put("description", dto.getDescription());
        map.put("expireIn",    dto.getExpire_in());
        map.put("visitCount",  dto.getVisit_count());
        map.put("createdAt",   dto.getCreated_at());
        map.put("updatedAt",   dto.getUpdated_at());
        map.put("banned",      dto.isBanned());
        return map;
    }
}

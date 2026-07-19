package edms.com.util.shortapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 단축 URL 테스트 전용 컨트롤러
 *
 * 운영용 진입점({@link ShortUrlController}, /api/short-url/**)과 분리하여,
 * 샘플 페이지 및 DB 매핑 없이 kuttId로 Kutt API를 직접 호출하는 테스트 엔드포인트만 제공합니다.
 *
 * BASE: /api/short-url/test
 *
 * [엔드포인트 목록]
 * GET  /api/short-url/test/sample              → 샘플 테스트 페이지
 * GET  /api/short-url/test/kutt/link           → kuttId로 단건 링크 조회 (Kutt 직접)
 * GET  /api/short-url/test/kutt/links          → Kutt 링크 목록 조회 (Kutt 직접)
 * PATCH/POST /api/short-url/test/kutt/{kuttId} → kuttId로 링크 수정 (Kutt 직접)
 * POST /api/short-url/test/kutt/{kuttId}/delete→ kuttId로 링크 삭제 (Kutt 직접)
 *
 * ※ 운영 배포 시 보안 설정에서 /api/short-url/test/** 경로만 차단하면
 *   테스트 엔드포인트를 손쉽게 비활성화할 수 있습니다.
 */
@Controller
@RequestMapping("/api/short-url/test")
public class ShortUrlTestController {

    private static final Logger log = LoggerFactory.getLogger(ShortUrlTestController.class);

    @Autowired
    private ShortUrlService shortUrlService;

    // =========================================================
    // 0. 샘플 테스트 페이지
    // GET /api/short-url/test/sample
    // =========================================================

    /**
     * 단축 URL 모달 샘플 페이지
     * - 브라우저에서 직접 접속해 모달 동작 및 REST API를 테스트할 수 있습니다.
     * - URL: /api/short-url/test/sample
     */
    @GetMapping("/sample")
    public String samplePage() {
        return "shortapi/shortUrlSample";
    }

    // =========================================================
    // ★ KUTT 직접 테스트 전용 엔드포인트 (DB 불필요)
    //   DB에 저장된 programId 매핑 없이 kuttId로 직접 Kutt API를 호출합니다.
    //   샘플 페이지 테스트 및 Kutt 서버 연동 확인 용도입니다.
    // =========================================================

    /**
     * [KUTT 직접] kuttId로 단건 링크 조회
     * GET /api/short-url/test/kutt/link?kuttId={kuttId}
     */
    @GetMapping(value = "/kutt/link", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> getKuttLink(@RequestParam String kuttId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = shortUrlService.getLinkFromKuttByKuttId(kuttId);
            if (data == null) {
                result.put("success", false);
                result.put("message", "Kutt에서 해당 링크를 찾지 못했습니다. kuttId: " + kuttId);
                return result;
            }
            result.put("success", true);
            result.putAll(data);
        } catch (Exception e) {
            log.error("[ShortUrlTestController] getKuttLink 오류", e);
            return fail(e.getMessage());
        }
        return result;
    }

    /**
     * [KUTT 직접] Kutt 링크 목록 조회
     * GET /api/short-url/test/kutt/links?limit=10&skip=0
     */
    @GetMapping(value = "/kutt/links", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> getKuttLinks(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0")  int skip) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = shortUrlService.getLinksFromKutt(limit, skip);
            result.put("success", true);
            result.putAll(data);
        } catch (Exception e) {
            log.error("[ShortUrlTestController] getKuttLinks 오류", e);
            return fail(e.getMessage());
        }
        return result;
    }

    /**
     * [KUTT 직접] kuttId로 링크 수정
     * POST /api/short-url/test/kutt/{kuttId}  (X-HTTP-Method-Override: PATCH)
     *
     * [요청 Body - JSON]
     * {
     *   "newTarget"  : "https://변경할-url.com",
     *   "description": "수정된 설명",
     *   "expireIn"   : "7 days",
     *   "keepTarget" : true
     * }
     */
    @RequestMapping(value = "/kutt/{kuttId}",
            method = {RequestMethod.PATCH, RequestMethod.POST},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> updateKuttLink(
            @PathVariable String kuttId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-HTTP-Method-Override", required = false) String methodOverride) {
        Map<String, Object> result = new HashMap<>();
        try {
            String  newTarget   = getString(body, "newTarget");
            String  description = getString(body, "description");
            String  expireIn    = getString(body, "expireIn");
            boolean keepTarget  = body.get("keepTarget") == null
                    || Boolean.parseBoolean(body.get("keepTarget").toString());

            Map<String, Object> data =
                    shortUrlService.updateShortUrlKuttOnly(kuttId, newTarget, description, expireIn, keepTarget);

            result.put("success", true);
            result.putAll(data);
        } catch (Exception e) {
            log.error("[ShortUrlTestController] updateKuttLink 오류", e);
            return fail(e.getMessage());
        }
        return result;
    }

    /**
     * [KUTT 직접] kuttId로 링크 삭제
     * POST /api/short-url/test/kutt/{kuttId}/delete
     */
    @PostMapping(value = "/kutt/{kuttId}/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> deleteKuttLink(@PathVariable String kuttId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean deleted = shortUrlService.deleteShortUrlKuttOnly(kuttId);
            result.put("success", deleted);
            result.put("kuttId",  kuttId);
            result.put("message", deleted ? "Kutt에서 삭제 완료" : "Kutt 삭제 실패");
        } catch (Exception e) {
            log.error("[ShortUrlTestController] deleteKuttLink 오류", e);
            return fail(e.getMessage());
        }
        return result;
    }

    // =========================================================
    // 공통 유틸
    // =========================================================

    private Map<String, Object> fail(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message != null ? message : "처리 중 오류가 발생했습니다.");
        return result;
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString().trim() : null;
    }
}

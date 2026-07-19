package edms.com.util.shortapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 단축 URL 운영 컨트롤러
 *
 * 어떤 화면(JSP/JS)에서든 AJAX로 호출 가능한 운영용 REST 엔드포인트 제공.
 * (샘플 페이지 및 kuttId 직접 호출 테스트 엔드포인트는 {@link ShortUrlTestController},
 *  /api/short-url/test/** 로 분리되어 있습니다.)
 *
 * [엔드포인트 목록]
 * POST   /api/short-url/create           → 단축 URL 생성
 * GET    /api/short-url/{programId}      → 단축 URL 조회 (by programId)
 * GET    /api/short-url/list             → 단축 URL 목록 조회
 * PATCH  /api/short-url/{programId}      → 단축 URL 수정
 * DELETE /api/short-url/{programId}      → 단축 URL 삭제
 * POST   /api/short-url/{programId}/delete → 삭제 (DELETE 미지원 환경용)
 *
 * [사용 예 - JSP JavaScript]
 * $.ajax({
 *     url: '/api/short-url/create',
 *     method: 'POST',
 *     contentType: 'application/json',
 *     data: JSON.stringify({
 *         programId: 'MENU_001',
 *         programUrl: 'http://내부서비스/program/detail?id=1',
 *         description: '상세 페이지',
 *         expireIn: '30 days'   // 생략 시 만료 없음
 *     }),
 *     success: function(res) { console.log(res.shortUrl); }
 * });
 */
@Controller
@RequestMapping("/api/short-url")
public class ShortUrlController {

    private static final Logger log = LoggerFactory.getLogger(ShortUrlController.class);

    @Autowired
    private ShortUrlService shortUrlService;

    // =========================================================
    // 1. 단축 URL 생성
    // POST /api/short-url/create
    // =========================================================

    /**
     * 단축 URL 생성
     *
     * [요청 Body - JSON]
     * {
     *   "programId"  : "MENU_001",
     *   "programUrl" : "http://내부서비스/program/detail?id=1",
     *   "description": "프로그램 상세 페이지",
     *   "expireIn"   : "30 days"
     * }
     */
    @PostMapping(value = "/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> createShortUrl(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            String programId   = getString(body, "programId");
            String programUrl  = getString(body, "programUrl");
            String description = getString(body, "description");
            String expireIn    = getString(body, "expireIn");

            if (programId == null || programId.isEmpty()) {
                return fail("programId는 필수입니다.");
            }
            if (programUrl == null || programUrl.isEmpty()) {
                return fail("programUrl은 필수입니다.");
            }

            Map<String, Object> data =
                    shortUrlService.createShortUrl(programId, programUrl, description, expireIn);

            result.put("success", true);
            result.putAll(data);
        } catch (Exception e) {
            log.error("[ShortUrlController] createShortUrl 오류", e);
            return fail(e.getMessage());
        }
        return result;
    }

    // =========================================================
    // 2. 단축 URL 단건 조회
    // GET /api/short-url/{programId}
    // =========================================================

    @GetMapping(value = "/{programId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> getShortUrl(@PathVariable String programId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = shortUrlService.getShortUrlByProgramId(programId);
            if (data == null) {
                result.put("success", false);
                result.put("message", "단축 URL 없음");
                return result;
            }
            result.put("success", true);
            result.putAll(data);
        } catch (Exception e) {
            log.error("[ShortUrlController] getShortUrl 오류", e);
            return fail(e.getMessage());
        }
        return result;
    }

    // =========================================================
    // 3. 단축 URL 목록 조회
    // GET /api/short-url/list?programId=&useYn=Y
    // =========================================================

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> getShortUrlList(
            @RequestParam(required = false) String programId,
            @RequestParam(required = false, defaultValue = "Y") String useYn) {

        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> condition = new HashMap<>();
            if (programId != null && !programId.isEmpty()) {
                condition.put("programId", programId);
            }
            condition.put("useYn", useYn);

            List<Map<String, Object>> list = shortUrlService.getShortUrlList(condition);
            result.put("success", true);
            result.put("list", list);
            result.put("total", list.size());
        } catch (Exception e) {
            log.error("[ShortUrlController] getShortUrlList 오류", e);
            return fail(e.getMessage());
        }
        return result;
    }

    // =========================================================
    // 4. 단축 URL 수정
    // PATCH /api/short-url/{programId}
    // =========================================================

    /**
     * [요청 Body - JSON]
     * {
     *   "newTarget"  : "http://내부서비스/program/new-path",
     *   "description": "수정된 설명",
     *   "expireIn"   : "7 days",
     *   "keepTarget" : true   // true=URL유지 / false=URL변경
     * }
     */
    @RequestMapping(value = "/{programId}",
            method = {RequestMethod.PATCH, RequestMethod.POST},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> updateShortUrl(
            @PathVariable String programId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-HTTP-Method-Override", required = false) String methodOverride) {

        Map<String, Object> result = new HashMap<>();
        try {
            String newTarget   = getString(body, "newTarget");
            String description = getString(body, "description");
            String expireIn    = getString(body, "expireIn");
            boolean keepTarget = body.get("keepTarget") == null
                    || Boolean.parseBoolean(body.get("keepTarget").toString());

            Map<String, Object> data =
                    shortUrlService.updateShortUrl(programId, newTarget, description, expireIn, keepTarget);

            result.put("success", true);
            result.putAll(data);
        } catch (Exception e) {
            log.error("[ShortUrlController] updateShortUrl 오류", e);
            return fail(e.getMessage());
        }
        return result;
    }

    // =========================================================
    // 5. 단축 URL 삭제
    // DELETE /api/short-url/{programId}
    // =========================================================

    @RequestMapping(value = "/{programId}",
            method = {RequestMethod.DELETE},
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> deleteShortUrl(@PathVariable String programId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean deleted = shortUrlService.deleteShortUrl(programId);
            result.put("success", deleted);
            result.put("programId", programId);
            result.put("message", deleted ? "삭제 완료" : "삭제 대상 없음");
        } catch (Exception e) {
            log.error("[ShortUrlController] deleteShortUrl 오류", e);
            return fail(e.getMessage());
        }
        return result;
    }

    /** DELETE 미지원 환경용 POST 방식 삭제 */
    @PostMapping(value = "/{programId}/delete",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> deleteShortUrlPost(@PathVariable String programId) {
        return deleteShortUrl(programId);
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

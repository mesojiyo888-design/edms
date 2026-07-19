# Kutt 단축 URL 유틸 패키지 (`util/shortapi`)

> Spring MVC + JDK 1.8 + MyBatis 기반 / 내부망(폐쇄망) 호환  
> Kutt 자체 호스팅 서버와 연동하여 단축 URL CRUD 처리

---

## 0. 변경 이력

| 일자 | 내용 |
|------|------|
| 2026-07-10 | **HTTP 클라이언트 전환**: `KuttApiClient` 를 `HttpURLConnection`(+PATCH 리플렉션) → Spring `WebClient`(reactor-netty) 기반으로 재작성. PATCH 네이티브 지원으로 리플렉션 제거, 신규 외부 의존성 없음(기존 spring-webflux 재사용). 공개 메서드 시그니처 유지로 호출부 무변경. |
| 2026-07-10 | **컨트롤러 경로 분리**: 운영 `ShortUrlController`(`/api/short-url/**`) ↔ 테스트 `ShortUrlTestController`(`/api/short-url/test/**`, 샘플 페이지 + `/kutt/**`). 운영 배포 시 `/api/short-url/test/**` 만 차단하면 테스트 엔드포인트 비활성화 가능. |
| 2026-07-10 | **기존 구현 백업**: `HttpURLConnection` 버전을 `backup/shortapi-httpurlconnection-20260710/`(src 밖, 빌드 제외)에 보관. 검증 후 폴더째 삭제 가능. |

---

## 1. 전체 디렉토리 구조

```
src/main/
├── java/edms/com/
│   └── util/
│       └── shortapi/
│           ├── dto/
│           │   ├── KuttLinkRequestDto.java       ← POST/PATCH 요청 DTO
│           │   ├── KuttLinkResponseDto.java      ← Kutt 단건 응답 DTO  (@JsonIgnoreProperties 적용)
│           │   └── KuttLinkListResponseDto.java  ← Kutt 목록 응답 DTO  (@JsonIgnoreProperties 적용)
│           ├── KuttApiClient.java                ← Kutt REST 호출 클라이언트 (WebClient/reactor-netty)
│           ├── ShortUrlService.java              ← 비즈니스 로직 + MyBatis DB 연동 + Kutt 직접 메서드
│           ├── ShortUrlController.java           ← [운영] AJAX REST 엔드포인트 (/api/short-url/**)
│           └── ShortUrlTestController.java       ← [테스트] 샘플 페이지 + Kutt 직접 (/api/short-url/test/**)
│
├── resources/
│   └── mapper/shortapi/
│       └── ShortUrlMapper.xml                   ← MyBatis SQL (CRUD)
│
└── webapp/WEB-INF/jsp/shortapi/
    ├── shortUrlModal.jsp                        ← 공통 모달 UI + JS
    └── shortUrlSample.jsp                       ← 테스트용 샘플 페이지 (/api/short-url/test/sample)
```

---

## 2. 설계 핵심 포인트

### REST 엔드포인트

#### DB 연동 (실제 비즈니스 로직)

| Method | URL | 설명 |
|--------|-----|------|
| `GET`  | `/api/short-url/test/sample` | 샘플 테스트 페이지 |
| `POST` | `/api/short-url/create` | 단축 URL 생성 (Kutt 호출 + DB 저장) |
| `GET`  | `/api/short-url/{programId}` | 단건 조회 (DB) |
| `GET`  | `/api/short-url/list` | 목록 조회 (DB) |
| `PATCH`| `/api/short-url/{programId}` | 수정 (DB에서 kuttId 조회 후 Kutt 호출) |
| `POST` | `/api/short-url/{programId}/delete` | 삭제 (DELETE 미지원 환경용) |

#### Kutt 직접 테스트 (DB 불필요 — kuttId로 직접 호출)

| Method | URL | 설명 |
|--------|-----|------|
| `GET`  | `/api/short-url/test/kutt/link?kuttId=` | kuttId로 단건 조회 |
| `GET`  | `/api/short-url/test/kutt/links?limit=&skip=` | Kutt 전체 목록 조회 |
| `PATCH`| `/api/short-url/test/kutt/{kuttId}` | kuttId로 직접 수정 |
| `POST` | `/api/short-url/test/kutt/{kuttId}/delete` | kuttId로 직접 삭제 |

#### Kutt API 엔드포인트 (KuttApiClient 내부 호출)

| Method | Kutt API Path | 설명 |
|--------|--------------|------|
| `POST`   | `/api/v2/links`         | 단축 URL 생성 |
| `GET`    | `/api/v2/links`         | 목록 조회 (단건 GET 없으므로 목록 필터링) |
| `PATCH`  | `/api/v2/links/{id}`    | 수정 |
| `DELETE` | `/api/v2/links/{id}`    | 삭제 |

---

### keepTarget 수정 정책

- `keepTarget=true` → 기존 원본 URL 조회 후 자동 유지. `description`, `expire_in`만 변경
- `keepTarget=false` → 전달받은 `newTarget`으로 대상 URL 변경

### expire_in 형식

```
"30 minutes"  →  30분 후 만료
"2 hours"     →  2시간 후 만료
"7 days"      →  7일 후 만료
"2 weeks"     →  2주 후 만료
"3 months"    →  3개월 후 만료
null          →  만료 없음 (기본값)
```

### 삭제 정책

- 논리 삭제 (`USE_YN = 'N'`) 기본 적용
- Kutt 서버 삭제 실패해도 DB는 논리 삭제 진행 (고아 데이터 방지)
- 물리 삭제 필요 시 `ShortUrlMapper.xml` 주석 처리된 `DELETE` 문으로 교체

### 내부망 호환 — PATCH 처리 방식

`HttpURLConnection`은 PATCH 메서드를 공식 지원하지 않음.  
`X-HTTP-Method-Override` 헤더 방식은 Kutt 서버가 인식하지 못해 404 발생.  
→ **리플렉션으로 `method` 필드를 강제 설정** (`PATCH`를 직접 주입).  
`HttpsURLConnection` 등 서브클래스도 대응하기 위해 클래스 계층을 거슬러 탐색.  
리플렉션 실패 시만 `X-HTTP-Method-Override`로 폴백.

### DTO 역직렬화 — 미지원 필드 처리

Kutt 서버 응답에 `domain` 등 DTO에 없는 필드가 포함될 수 있음.  
→ `KuttLinkResponseDto`, `KuttLinkListResponseDto` 모두 `@JsonIgnoreProperties(ignoreUnknown = true)` 적용.

### DB 미연동 상태 (테스트 단계)

현재 Oracle DB 테이블 생성 전 테스트 단계. `ShortUrlService`의 처리 상태:

| 기능 | 상태 | 비고 |
|------|------|------|
| 생성 | ✅ Kutt 실호출 | DB insert만 주석 처리 |
| 기존 조회 | ⚠️ 항상 null 반환 | DB 없으므로 "등록 없음" 처리 |
| 목록 조회 | ⚠️ 빈 목록 반환 | DB 없으므로 |
| 수정 | ❌ 오류 반환 | kuttId가 DB에 있어야 함 → `/kutt/{kuttId}` 엔드포인트 사용 |
| 삭제 | ❌ 오류 반환 | kuttId가 DB에 있어야 함 → `/kutt/{kuttId}/delete` 엔드포인트 사용 |

**DB 연동 복구 방법 (3단계):**
1. 각 메서드 내 `[DB 없이 skip]` 블록 주석 처리
2. `[DB 연동 시 주석 해제]` 블록 주석 제거
3. 클래스 상단 `@Autowired SqlSession`, `@Transactional` 주석 해제

---

## 3. DB 테이블 DDL

```sql
CREATE TABLE SHORT_URL_MAPPING (
    SEQ          NUMBER         NOT NULL,
    PROGRAM_ID   VARCHAR2(100)  NOT NULL,
    PROGRAM_URL  VARCHAR2(500)  NOT NULL,
    KUTT_ID      VARCHAR2(100)  NOT NULL,
    SHORT_URL    VARCHAR2(200)  NOT NULL,
    KUTT_ADDRESS VARCHAR2(100),
    DESCRIPTION  VARCHAR2(500),
    EXPIRE_IN    VARCHAR2(100),
    USE_YN       CHAR(1)  DEFAULT 'Y' NOT NULL,
    REG_DT       DATE     DEFAULT SYSDATE,
    MOD_DT       DATE,
    CONSTRAINT PK_SHORT_URL PRIMARY KEY (SEQ),
    CONSTRAINT UQ_SHORT_URL_PROGRAM UNIQUE (PROGRAM_ID)
);
CREATE SEQUENCE SEQ_SHORT_URL START WITH 1 INCREMENT BY 1;

COMMENT ON TABLE SHORT_URL_MAPPING IS '단축 URL 매핑';

COMMENT ON COLUMN SHORT_URL_MAPPING.SEQ          IS '일련번호 (PK)';
COMMENT ON COLUMN SHORT_URL_MAPPING.PROGRAM_ID   IS '프로그램 식별자 (화면/메뉴 고유 ID)';
COMMENT ON COLUMN SHORT_URL_MAPPING.PROGRAM_URL  IS '원본 URL (단축 전 실제 주소)';
COMMENT ON COLUMN SHORT_URL_MAPPING.KUTT_ID      IS 'Kutt 서버 내부 고유 ID (UUID, 수정·삭제 시 사용)';
COMMENT ON COLUMN SHORT_URL_MAPPING.SHORT_URL    IS '단축 URL (Kutt 서버에서 발급된 완성 주소)';
COMMENT ON COLUMN SHORT_URL_MAPPING.KUTT_ADDRESS IS '단축 주소 키값 (단축 URL의 path 부분, 예: NFJ)';
COMMENT ON COLUMN SHORT_URL_MAPPING.DESCRIPTION  IS '단축 URL 설명';
COMMENT ON COLUMN SHORT_URL_MAPPING.EXPIRE_IN    IS '만료 기간 (예: 7 days, 24 hours — NULL이면 만료 없음)';
COMMENT ON COLUMN SHORT_URL_MAPPING.USE_YN       IS '사용 여부 (Y: 사용, N: 삭제/미사용)';
COMMENT ON COLUMN SHORT_URL_MAPPING.REG_DT       IS '등록 일시';
COMMENT ON COLUMN SHORT_URL_MAPPING.MOD_DT       IS '최종 수정 일시';
```

---

## 4. application.yml 설정

```yaml
kutt:
  server:
    url: http://localhost:8085       # Kutt 서버 주소 (내부망 자체 호스팅)
  api:
    key: YOUR_KUTT_API_KEY_HERE      # Kutt 설정 > API Keys 에서 발급
    timeout: 5000                    # HTTP 타임아웃 (ms, 기본 5초)
```

---

## 5. JSP 화면 사용법

```jsp
<%-- 1) 모달 include (페이지당 1회) --%>
<%@ include file="/WEB-INF/jsp/shortapi/shortUrlModal.jsp" %>

<%-- 2) 버튼으로 호출 --%>
<button onclick="ShortUrl.open({
    programId  : 'MENU_001',
    programUrl : location.href,
    description: '페이지 설명',
    onSuccess  : function(shortUrl) { console.log(shortUrl); }
})">단축 URL</button>
```

> 샘플 테스트 페이지: `/api/short-url/test/sample`  
> 모달 동작 확인 및 DB/Kutt 각 API 직접 호출 테스트 가능

---

## 6. Java Service 직접 호출

```java
@Autowired
private ShortUrlService shortUrlService;

// ── DB 연동 메서드 (DB 연동 후 사용) ──────────────────────────

// 생성 (Kutt 호출 + DB 저장)
Map<String, Object> result = shortUrlService.createShortUrl(
    "PROGRAM_ID", "http://내부서비스/path", "설명", "30 days");

// 조회
Map<String, Object> info = shortUrlService.getShortUrlByProgramId("PROGRAM_ID");

// 수정 - URL 유지
shortUrlService.updateShortUrl("PROGRAM_ID", null, "새설명", "7 days", true);

// 수정 - URL 변경
shortUrlService.updateShortUrl("PROGRAM_ID", "http://새URL", "새설명", null, false);

// 삭제 (Kutt + DB)
shortUrlService.deleteShortUrl("PROGRAM_ID");

// 프로그램 삭제 연동
shortUrlService.deleteShortUrlByProgramDelete("PROGRAM_ID");

// ── Kutt 직접 메서드 (DB 불필요 — kuttId로 직접 호출) ──────────

// kuttId로 단건 조회
Map<String, Object> link = shortUrlService.getLinkFromKuttByKuttId("uuid-here");

// Kutt 전체 목록 조회
Map<String, Object> list = shortUrlService.getLinksFromKutt(10, 0);

// kuttId로 수정
shortUrlService.updateShortUrlKuttOnly("uuid-here", null, "새설명", "7 days", true);

// kuttId로 삭제
shortUrlService.deleteShortUrlKuttOnly("uuid-here");
```

---

## 7. 소스 코드

### 7-1. `dto/KuttLinkRequestDto.java`

```java
package edms.com.util.shortapi.dto;

/**
 * Kutt API 링크 생성/수정 요청 DTO
 *
 * [Kutt API 필드 설명]
 * - target      : (필수) 단축할 원본 URL
 * - description : (선택) 링크 설명
 * - expire_in   : (선택) 만료 기간. 예) "1 days", "24 hours", "3 months"
 *                  형식: "{숫자} {단위}" — minutes/hours/days/weeks/months
 * - address     : (선택) 커스텀 단축 주소 (예: "my-page" → kutt.to/my-page)
 * - password    : (선택) 접근 비밀번호
 * - reuse       : (선택) 동일 target이 존재하면 기존 URL 재사용 여부 (POST 전용)
 */
public class KuttLinkRequestDto {

    /** 단축할 원본 URL (필수) */
    private String target;

    /** 링크 설명 (선택) */
    private String description;

    /**
     * 만료 기간 (선택) — null이면 만료 없음
     * 형식: "1 days" / "24 hours" / "3 months" 등
     */
    private String expire_in;

    /** 커스텀 단축 주소 (선택) */
    private String address;

    /** 접근 비밀번호 (선택) */
    private String password;

    /**
     * 동일 target URL 재사용 여부 (POST 전용, 선택)
     * true: 동일 target이 이미 있으면 기존 단축 URL 반환
     */
    private Boolean reuse;

    // ===================== 생성자 =====================

    public KuttLinkRequestDto() {}

    public KuttLinkRequestDto(String target) {
        this.target = target;
    }

    public KuttLinkRequestDto(String target, String description, String expire_in) {
        this.target = target;
        this.description = description;
        this.expire_in = expire_in;
    }

    // ===================== Getter / Setter =====================

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getExpire_in() { return expire_in; }
    public void setExpire_in(String expire_in) { this.expire_in = expire_in; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Boolean getReuse() { return reuse; }
    public void setReuse(Boolean reuse) { this.reuse = reuse; }

    @Override
    public String toString() {
        return "KuttLinkRequestDto{target='" + target + "', description='" + description
                + "', expire_in='" + expire_in + "', address='" + address + "', reuse=" + reuse + '}';
    }
}
```

---

### 7-2. `dto/KuttLinkResponseDto.java`

> ⚠️ Kutt 서버 응답에 `domain` 등 미정의 필드가 포함될 수 있어  
> `@JsonIgnoreProperties(ignoreUnknown = true)` 추가 (미추가 시 역직렬화 오류 발생)

```java
package edms.com.util.shortapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Kutt API 링크 단건 응답 DTO
 *
 * [Kutt API 응답 예시]
 * {
 *   "id": "accefebc-f139-4f08-8b6a-e45b89ce0873",
 *   "address": "NFJ",
 *   "description": null,
 *   "banned": false,
 *   "password": false,
 *   "expire_in": null,
 *   "target": "https://example.com",
 *   "visit_count": 0,
 *   "created_at": "2020-11-06T17:26:22.533Z",
 *   "updated_at": "2020-11-06T17:26:22.533Z",
 *   "link": "https://kutt.to/NFJ",
 *   "domain": null    ← DTO에 없는 필드 (ignoreUnknown=true로 무시)
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KuttLinkResponseDto {

    /** Kutt 내부 고유 ID (UUID) — 수정/삭제 시 사용 */
    private String id;

    /** 단축 주소 (path 부분만, 예: "NFJ") */
    private String address;

    /** 링크 설명 */
    private String description;

    /** 차단 여부 */
    private boolean banned;

    /** 비밀번호 설정 여부 */
    private boolean password;

    /** 만료 일시 (ISO 8601, null이면 만료 없음) */
    private String expire_in;

    /** 원본(대상) URL */
    private String target;

    /** 방문 횟수 */
    private int visit_count;

    /** 생성 일시 (ISO 8601) */
    private String created_at;

    /** 수정 일시 (ISO 8601) */
    private String updated_at;

    /** 완성된 단축 URL (예: http://localhost:8085/NFJ) */
    private String link;

    // ===================== Getter / Setter =====================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isBanned() { return banned; }
    public void setBanned(boolean banned) { this.banned = banned; }

    public boolean isPassword() { return password; }
    public void setPassword(boolean password) { this.password = password; }

    public String getExpire_in() { return expire_in; }
    public void setExpire_in(String expire_in) { this.expire_in = expire_in; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public int getVisit_count() { return visit_count; }
    public void setVisit_count(int visit_count) { this.visit_count = visit_count; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }

    public String getUpdated_at() { return updated_at; }
    public void setUpdated_at(String updated_at) { this.updated_at = updated_at; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    @Override
    public String toString() {
        return "KuttLinkResponseDto{id='" + id + "', address='" + address
                + "', target='" + target + "', link='" + link
                + "', expire_in='" + expire_in + "', visit_count=" + visit_count + '}';
    }
}
```

---

### 7-3. `dto/KuttLinkListResponseDto.java`

```java
package edms.com.util.shortapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Kutt API 링크 목록 조회 응답 DTO
 *
 * GET /api/v2/links 응답 형식:
 * {
 *   "total": 1,
 *   "limit": 10,
 *   "skip": 0,
 *   "data": [ { ...KuttLinkResponseDto... } ]
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KuttLinkListResponseDto {

    /** 전체 링크 수 */
    private int total;

    /** 요청 시 limit 값 */
    private int limit;

    /** 요청 시 skip(offset) 값 */
    private int skip;

    /** 링크 목록 */
    private List<KuttLinkResponseDto> data;

    // ===================== Getter / Setter =====================

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }

    public int getSkip() { return skip; }
    public void setSkip(int skip) { this.skip = skip; }

    public List<KuttLinkResponseDto> getData() { return data; }
    public void setData(List<KuttLinkResponseDto> data) { this.data = data; }
}
```

---

### 7-4. `KuttApiClient.java`

> **PATCH 처리 방식 변경**  
> `X-HTTP-Method-Override` 헤더 방식 → **리플렉션으로 `method` 필드 강제 설정**  
> (Kutt 서버가 Override 헤더를 인식하지 못해 `Cannot POST /api/v2/links/{id}` 404 오류 발생했던 문제 수정)

```java
package edms.com.util.shortapi;

import edms.com.util.shortapi.dto.KuttLinkListResponseDto;
import edms.com.util.shortapi.dto.KuttLinkRequestDto;
import edms.com.util.shortapi.dto.KuttLinkResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Kutt URL 단축 서비스 REST API 클라이언트
 *
 * JDK 1.8 / Spring MVC 환경 (Apache HttpClient 미사용, 순수 HttpURLConnection 사용)
 * ※ 내부망(폐쇄망) 환경에서도 동작 가능.
 *
 * [설정 - application.yml]
 *   kutt:
 *     server:
 *       url: http://localhost:8085
 *     api:
 *       key: your-kutt-api-key
 *       timeout: 5000
 *
 * [Kutt API Endpoints]
 *   POST   /api/v2/links         → 단축 URL 생성
 *   GET    /api/v2/links         → 단축 URL 목록 조회
 *   PATCH  /api/v2/links/{id}    → 단축 URL 수정
 *   DELETE /api/v2/links/{id}    → 단축 URL 삭제
 */
@Component
public class KuttApiClient {

    private static final Logger log = LoggerFactory.getLogger(KuttApiClient.class);

    private static final String API_BASE_PATH    = "/api/v2/links";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String API_KEY_HEADER   = "X-API-KEY";

    @Value("${kutt.server.url}")
    private String kuttServerUrl;

    @Value("${kutt.api.key}")
    private String kuttApiKey;

    @Value("${kutt.api.timeout:5000}")
    private int timeoutMs;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // =========================================================
    // 1. 단축 URL 생성 (POST /api/v2/links)
    // =========================================================

    public KuttLinkResponseDto createLink(KuttLinkRequestDto requestDto) {
        log.info("[KuttApiClient] createLink - target: {}", requestDto.getTarget());
        String response = execute("POST", kuttServerUrl + API_BASE_PATH, toJson(requestDto));
        return fromJson(response, KuttLinkResponseDto.class);
    }

    // =========================================================
    // 2. 단축 URL 목록 조회 (GET /api/v2/links)
    // =========================================================

    public KuttLinkListResponseDto getLinks(int limit, int skip) {
        log.info("[KuttApiClient] getLinks - limit: {}, skip: {}", limit, skip);
        String endpoint = kuttServerUrl + API_BASE_PATH + "?limit=" + limit + "&skip=" + skip;
        String response = execute("GET", endpoint, null);
        return fromJson(response, KuttLinkListResponseDto.class);
    }

    /**
     * kuttId로 단건 조회
     * Kutt v2 API는 단건 GET 없음 → 목록(최대 100건)에서 id 필터링
     */
    public KuttLinkResponseDto getLinkById(String kuttId) {
        log.info("[KuttApiClient] getLinkById - id: {}", kuttId);
        KuttLinkListResponseDto listDto = getLinks(100, 0);
        if (listDto != null && listDto.getData() != null) {
            for (KuttLinkResponseDto link : listDto.getData()) {
                if (kuttId.equals(link.getId())) return link;
            }
        }
        return null;
    }

    // =========================================================
    // 3. 단축 URL 수정 (PATCH /api/v2/links/{id})
    // =========================================================

    /**
     * keepTarget=true  : 기존 target을 목록에서 조회 후 자동 유지
     * keepTarget=false : requestDto.target으로 변경
     */
    public KuttLinkResponseDto updateLink(String kuttId,
                                          KuttLinkRequestDto requestDto,
                                          boolean keepTarget) {
        log.info("[KuttApiClient] updateLink - id: {}, keepTarget: {}", kuttId, keepTarget);
        if (keepTarget) {
            KuttLinkResponseDto existing = getLinkById(kuttId);
            if (existing != null) {
                log.info("[KuttApiClient] keepTarget=true, 기존 target 유지: {}", existing.getTarget());
                requestDto.setTarget(existing.getTarget());
            } else {
                log.warn("[KuttApiClient] keepTarget=true이나 기존 링크 조회 실패. id: {}", kuttId);
            }
        }
        String endpoint = kuttServerUrl + API_BASE_PATH + "/" + kuttId;
        String response = execute("PATCH", endpoint, toJson(requestDto));
        return fromJson(response, KuttLinkResponseDto.class);
    }

    // =========================================================
    // 4. 단축 URL 삭제 (DELETE /api/v2/links/{id})
    // =========================================================

    public boolean deleteLink(String kuttId) {
        log.info("[KuttApiClient] deleteLink - id: {}", kuttId);
        String endpoint = kuttServerUrl + API_BASE_PATH + "/" + kuttId;
        try {
            execute("DELETE", endpoint, null);
            return true;
        } catch (KuttApiException e) {
            log.error("[KuttApiClient] deleteLink 실패 - id: {}, error: {}", kuttId, e.getMessage());
            return false;
        }
    }

    // =========================================================
    // 공통 HTTP 실행 (HttpURLConnection, JDK 1.8)
    // =========================================================

    /**
     * PATCH 처리:
     * HttpURLConnection은 PATCH를 공식 지원하지 않음.
     * X-HTTP-Method-Override 헤더는 Kutt 서버가 인식 못해 404 발생.
     * → 리플렉션으로 method 필드를 "PATCH"로 강제 설정.
     *   HttpsURLConnection 등 서브클래스 대응을 위해 클래스 계층을 거슬러 탐색.
     *   리플렉션 실패 시에만 X-HTTP-Method-Override로 폴백.
     */
    private String execute(String method, String endpoint, String body) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();

            if ("PATCH".equals(method)) {
                conn.setRequestMethod("POST"); // 일부 JDK에서 먼저 필요
                try {
                    Class<?> clazz = conn.getClass();
                    Field methodField = null;
                    while (clazz != null && methodField == null) {
                        try {
                            methodField = clazz.getDeclaredField("method");
                        } catch (NoSuchFieldException e) {
                            clazz = clazz.getSuperclass();
                        }
                    }
                    if (methodField != null) {
                        methodField.setAccessible(true);
                        methodField.set(conn, "PATCH");
                        log.debug("[KuttApiClient] PATCH 메서드 리플렉션 설정 완료");
                    } else {
                        log.warn("[KuttApiClient] method 필드를 찾지 못해 X-HTTP-Method-Override로 대체");
                        conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
                    }
                } catch (Exception e) {
                    log.warn("[KuttApiClient] 리플렉션 실패, X-HTTP-Method-Override로 대체: {}", e.getMessage());
                    conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
                }
            } else {
                conn.setRequestMethod(method);
            }

            conn.setRequestProperty("Content-Type", CONTENT_TYPE_JSON);
            conn.setRequestProperty("Accept", CONTENT_TYPE_JSON);
            conn.setRequestProperty(API_KEY_HEADER, kuttApiKey);
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);

            if (body != null) {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }
            }

            int statusCode = conn.getResponseCode();
            log.debug("[KuttApiClient] {} {} → HTTP {}", method, endpoint, statusCode);

            InputStream is = (statusCode >= 200 && statusCode < 300)
                    ? conn.getInputStream() : conn.getErrorStream();
            String responseBody = readStream(is);

            if (statusCode < 200 || statusCode >= 300) {
                log.error("[KuttApiClient] API 오류 응답 - status: {}, body: {}", statusCode, responseBody);
                throw new KuttApiException("Kutt API 오류: HTTP " + statusCode + " / " + responseBody);
            }

            return responseBody;

        } catch (KuttApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("[KuttApiClient] HTTP 요청 실패 - method: {}, url: {}", method, endpoint, e);
            throw new KuttApiException("Kutt API 연결 실패: " + e.getMessage(), e);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    private String toJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); }
        catch (Exception e) { throw new KuttApiException("JSON 직렬화 오류: " + e.getMessage(), e); }
    }

    private <T> T fromJson(String json, Class<T> clazz) {
        try { return objectMapper.readValue(json, clazz); }
        catch (Exception e) {
            log.error("[KuttApiClient] JSON 역직렬화 오류 - json: {}", json);
            throw new KuttApiException("JSON 역직렬화 오류: " + e.getMessage(), e);
        }
    }

    // =========================================================
    // 커스텀 예외
    // =========================================================

    public static class KuttApiException extends RuntimeException {
        public KuttApiException(String message) { super(message); }
        public KuttApiException(String message, Throwable cause) { super(message, cause); }
    }
}
```

---

### 7-5. `ShortUrlService.java`

> DB 연동 전 테스트 상태.  
> Kutt API는 실호출. DB 관련 코드는 주석 처리되어 있으며, 복구 방법이 주석으로 표시되어 있음.  
> Kutt 직접 메서드(`getLinkFromKuttByKuttId` 등)는 DB와 무관하게 영구 사용.

```java
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

@Service
public class ShortUrlService {

    private static final Logger log = LoggerFactory.getLogger(ShortUrlService.class);
    private static final String MAPPER_NS = "shortapi.ShortUrlMapper";

    @Autowired
    private KuttApiClient kuttApiClient;

    /* [DB 연동 시 주석 해제]
     * @Autowired
     * private SqlSession sqlSession;
     */

    // =========================================================
    // 1. 단축 URL 생성
    // =========================================================

    // [DB 연동 시 주석 해제] @Transactional
    public Map<String, Object> createShortUrl(String programId, String programUrl,
                                               String description, String expireIn) {
        log.info("[ShortUrlService] createShortUrl - programId: {}, url: {}", programId, programUrl);

        /* [DB 없이 skip] - DB 연동 시 아래 블록 주석 해제, 이 블록 주석 처리
         * Map<String, Object> existing = getShortUrlByProgramId(programId);
         * if (existing != null) return existing;
         */

        // Kutt API 실호출 (DB 연동 여부와 무관)
        KuttLinkRequestDto requestDto = new KuttLinkRequestDto();
        requestDto.setTarget(programUrl);
        requestDto.setDescription(description);
        if (expireIn != null && !expireIn.trim().isEmpty()) requestDto.setExpire_in(expireIn);

        KuttLinkResponseDto apiResponse = kuttApiClient.createLink(requestDto);
        log.info("[ShortUrlService] Kutt 생성 완료 - kuttId: {}, link: {}",
                apiResponse.getId(), apiResponse.getLink());

        /* [DB 없이 skip] - DB 연동 시 아래 블록 주석 해제, 이 블록 주석 처리
         * Map<String, Object> param = new HashMap<>();
         * param.put("programId",   programId);
         * param.put("programUrl",  programUrl);
         * param.put("kuttId",      apiResponse.getId());
         * param.put("shortUrl",    apiResponse.getLink());
         * param.put("kuttAddress", apiResponse.getAddress());
         * param.put("description", description);
         * param.put("expireIn",    expireIn);
         * sqlSession.insert(MAPPER_NS + ".insertShortUrl", param);
         */

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

    public Map<String, Object> getShortUrlByProgramId(String programId) {
        // [DB 없이 skip] DB 연동 시: return sqlSession.selectOne(MAPPER_NS + ".selectShortUrlByProgramId", programId);
        log.info("[ShortUrlService][DB미연동] getShortUrlByProgramId skip - null 반환");
        return null;
    }

    public List<Map<String, Object>> getShortUrlList(Map<String, Object> condition) {
        // [DB 없이 skip] DB 연동 시: return sqlSession.selectList(MAPPER_NS + ".selectShortUrlList", condition);
        log.info("[ShortUrlService][DB미연동] getShortUrlList skip - 빈 목록 반환");
        return new ArrayList<>();
    }

    // =========================================================
    // 3. 단축 URL 수정 (DB 필요 — kuttId를 DB에서 가져옴)
    // =========================================================

    // [DB 연동 시 주석 해제] @Transactional
    public Map<String, Object> updateShortUrl(String programId, String newTarget,
                                               String description, String expireIn,
                                               boolean keepTarget) {
        // DB 없이는 kuttId 조회 불가 → 명확한 오류 반환
        // DB 연동 후: 아래 throw 제거하고 [DB 연동 시 주석 해제] 블록 활성화
        log.warn("[ShortUrlService][DB미연동] 수정 불가 - kuttId 조회를 위해 DB 연동 필요");
        throw new IllegalStateException(
            "DB 미연동 상태에서는 수정 불가. Oracle DB 연동 후 사용하세요. (programId: " + programId + ")" +
            " | 현재는 KUTT 직접 테스트 엔드포인트 사용: PATCH /api/short-url/test/kutt/{kuttId}");

        /* [DB 연동 시 주석 해제]
         * Map<String, Object> mapping = getShortUrlByProgramId(programId);
         * if (mapping == null) throw new IllegalArgumentException("단축 URL 매핑 정보 없음. programId: " + programId);
         * String kuttId = (String) mapping.get("kuttId");
         * String originalTarget = (String) mapping.get("programUrl");
         *
         * KuttLinkRequestDto requestDto = new KuttLinkRequestDto();
         * requestDto.setDescription(description);
         * requestDto.setExpire_in(expireIn);
         * if (!keepTarget && newTarget != null && !newTarget.trim().isEmpty()) requestDto.setTarget(newTarget);
         * else requestDto.setTarget(originalTarget);
         *
         * KuttLinkResponseDto apiResponse = kuttApiClient.updateLink(kuttId, requestDto, keepTarget);
         *
         * Map<String, Object> param = new HashMap<>();
         * param.put("programId", programId);
         * param.put("description", description);
         * param.put("expireIn", expireIn);
         * if (!keepTarget && newTarget != null && !newTarget.trim().isEmpty()) param.put("programUrl", newTarget);
         * sqlSession.update(MAPPER_NS + ".updateShortUrl", param);
         *
         * Map<String, Object> result = new HashMap<>();
         * result.put("shortUrl", apiResponse.getLink()); result.put("kuttId", kuttId);
         * result.put("programId", programId); result.put("expireIn", expireIn);
         * result.put("keepTarget", keepTarget);
         * return result;
         */
    }

    // =========================================================
    // 4. 단축 URL 삭제 (DB 필요 — kuttId를 DB에서 가져옴)
    // =========================================================

    // [DB 연동 시 주석 해제] @Transactional
    public boolean deleteShortUrl(String programId) {
        // DB 없이는 kuttId 조회 불가 → 명확한 오류 반환
        log.warn("[ShortUrlService][DB미연동] 삭제 불가 - kuttId 조회를 위해 DB 연동 필요");
        throw new IllegalStateException(
            "DB 미연동 상태에서는 삭제 불가. Oracle DB 연동 후 사용하세요. (programId: " + programId + ")" +
            " | 현재는 KUTT 직접 테스트 엔드포인트 사용: POST /api/short-url/test/kutt/{kuttId}/delete");

        /* [DB 연동 시 주석 해제]
         * Map<String, Object> mapping = getShortUrlByProgramId(programId);
         * if (mapping == null) { log.warn("삭제 대상 없음 - programId: {}", programId); return false; }
         * String kuttId = (String) mapping.get("kuttId");
         * boolean kuttDeleted = kuttApiClient.deleteLink(kuttId);
         * if (!kuttDeleted) log.warn("Kutt 서버 삭제 실패 - kuttId: {}. DB는 계속 진행.", kuttId);
         * sqlSession.update(MAPPER_NS + ".deleteShortUrl", programId);
         * return true;
         */
    }

    // [DB 연동 시 주석 해제] @Transactional
    public void deleteShortUrlByProgramDelete(String programId) {
        log.info("[ShortUrlService] 프로그램 삭제 연동 - programId: {}", programId);
        deleteShortUrl(programId);
    }

    // =========================================================
    // ★ Kutt 직접 메서드 (DB 불필요 — kuttId로 직접 호출)
    //   DB 연동 여부와 무관하게 영구 사용
    // =========================================================

    /** kuttId로 단건 조회 (Kutt 목록에서 필터링) */
    public Map<String, Object> getLinkFromKuttByKuttId(String kuttId) {
        log.info("[ShortUrlService][KUTT직접] getLinkFromKuttByKuttId - kuttId: {}", kuttId);
        KuttLinkResponseDto dto = kuttApiClient.getLinkById(kuttId);
        if (dto == null) return null;
        return kuttLinkResponseToMap(dto);
    }

    /** Kutt 전체 목록 조회 */
    public Map<String, Object> getLinksFromKutt(int limit, int skip) {
        log.info("[ShortUrlService][KUTT직접] getLinksFromKutt - limit: {}, skip: {}", limit, skip);
        KuttLinkListResponseDto dto = kuttApiClient.getLinks(limit, skip);
        Map<String, Object> result = new HashMap<>();
        result.put("total", dto.getTotal());
        result.put("limit", dto.getLimit());
        result.put("skip",  dto.getSkip());
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (dto.getData() != null) {
            for (KuttLinkResponseDto link : dto.getData()) dataList.add(kuttLinkResponseToMap(link));
        }
        result.put("data", dataList);
        return result;
    }

    /** kuttId로 직접 수정 */
    public Map<String, Object> updateShortUrlKuttOnly(String kuttId, String newTarget,
                                                       String description, String expireIn,
                                                       boolean keepTarget) {
        log.info("[ShortUrlService][KUTT직접] updateShortUrlKuttOnly - kuttId: {}", kuttId);
        KuttLinkRequestDto requestDto = new KuttLinkRequestDto();
        requestDto.setDescription(description);
        requestDto.setExpire_in(expireIn);
        if (!keepTarget && newTarget != null && !newTarget.trim().isEmpty()) {
            requestDto.setTarget(newTarget);
        }
        KuttLinkResponseDto apiResponse = kuttApiClient.updateLink(kuttId, requestDto, keepTarget);
        log.info("[ShortUrlService][KUTT직접] Kutt 수정 완료 - kuttId: {}, link: {}", kuttId, apiResponse.getLink());
        return kuttLinkResponseToMap(apiResponse);
    }

    /** kuttId로 직접 삭제 */
    public boolean deleteShortUrlKuttOnly(String kuttId) {
        log.info("[ShortUrlService][KUTT직접] deleteShortUrlKuttOnly - kuttId: {}", kuttId);
        boolean deleted = kuttApiClient.deleteLink(kuttId);
        log.info("[ShortUrlService][KUTT직접] Kutt 삭제 결과 - kuttId: {}, deleted: {}", kuttId, deleted);
        return deleted;
    }

    // =========================================================
    // 내부 유틸
    // =========================================================

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
```

---

### 7-6. `ShortUrlController.java`

```java
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

@Controller
@RequestMapping("/api/short-url")
public class ShortUrlController {

    private static final Logger log = LoggerFactory.getLogger(ShortUrlController.class);

    @Autowired
    private ShortUrlService shortUrlService;

    // ── 샘플 테스트 페이지 ───────────────────────────────────────
    // GET /api/short-url/test/sample
    @GetMapping("/sample")
    public String samplePage() {
        return "shortapi/shortUrlSample";
    }

    // ── DB 연동 엔드포인트 ───────────────────────────────────────

    // POST /api/short-url/create
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
            if (programId == null || programId.isEmpty())  return fail("programId는 필수입니다.");
            if (programUrl == null || programUrl.isEmpty()) return fail("programUrl은 필수입니다.");
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

    // GET /api/short-url/{programId}
    @GetMapping(value = "/{programId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> getShortUrl(@PathVariable String programId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = shortUrlService.getShortUrlByProgramId(programId);
            if (data == null) { result.put("success", false); result.put("message", "단축 URL 없음"); return result; }
            result.put("success", true);
            result.putAll(data);
        } catch (Exception e) {
            log.error("[ShortUrlController] getShortUrl 오류", e);
            return fail(e.getMessage());
        }
        return result;
    }

    // GET /api/short-url/list
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> getShortUrlList(
            @RequestParam(required = false) String programId,
            @RequestParam(required = false, defaultValue = "Y") String useYn) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> condition = new HashMap<>();
            if (programId != null && !programId.isEmpty()) condition.put("programId", programId);
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

    // PATCH /api/short-url/{programId}  (POST + X-HTTP-Method-Override: PATCH 겸용)
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
            String  newTarget   = getString(body, "newTarget");
            String  description = getString(body, "description");
            String  expireIn    = getString(body, "expireIn");
            boolean keepTarget  = body.get("keepTarget") == null
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

    // DELETE /api/short-url/{programId}
    @RequestMapping(value = "/{programId}", method = {RequestMethod.DELETE},
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

    // POST /api/short-url/{programId}/delete  (DELETE 미지원 환경용)
    @PostMapping(value = "/{programId}/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> deleteShortUrlPost(@PathVariable String programId) {
        return deleteShortUrl(programId);
    }

    // ── Kutt 직접 테스트 엔드포인트 (DB 불필요) ──────────────────

    // GET /api/short-url/test/kutt/link?kuttId=
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
            log.error("[ShortUrlController] getKuttLink 오류", e);
            return fail(e.getMessage());
        }
        return result;
    }

    // GET /api/short-url/test/kutt/links?limit=10&skip=0
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
            log.error("[ShortUrlController] getKuttLinks 오류", e);
            return fail(e.getMessage());
        }
        return result;
    }

    // PATCH /api/short-url/test/kutt/{kuttId}  (POST + X-HTTP-Method-Override: PATCH 겸용)
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
            log.error("[ShortUrlController] updateKuttLink 오류", e);
            return fail(e.getMessage());
        }
        return result;
    }

    // POST /api/short-url/test/kutt/{kuttId}/delete
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
            log.error("[ShortUrlController] deleteKuttLink 오류", e);
            return fail(e.getMessage());
        }
        return result;
    }

    // ── 공통 유틸 ────────────────────────────────────────────────

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
```

---

### 7-7. `mapper/shortapi/ShortUrlMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="shortapi.ShortUrlMapper">

    <resultMap id="shortUrlResultMap" type="java.util.HashMap">
        <result column="SEQ"          property="seq"         />
        <result column="PROGRAM_ID"   property="programId"   />
        <result column="PROGRAM_URL"  property="programUrl"  />
        <result column="KUTT_ID"      property="kuttId"      />
        <result column="SHORT_URL"    property="shortUrl"    />
        <result column="KUTT_ADDRESS" property="kuttAddress" />
        <result column="DESCRIPTION"  property="description" />
        <result column="EXPIRE_IN"    property="expireIn"    />
        <result column="USE_YN"       property="useYn"       />
        <result column="REG_DT"       property="regDt"       />
        <result column="MOD_DT"       property="modDt"       />
    </resultMap>

    <!-- 1. 단건 조회 -->
    <select id="selectShortUrlByProgramId" parameterType="String" resultMap="shortUrlResultMap">
        SELECT SEQ, PROGRAM_ID, PROGRAM_URL, KUTT_ID, SHORT_URL, KUTT_ADDRESS,
               DESCRIPTION, EXPIRE_IN, USE_YN,
               TO_CHAR(REG_DT, 'YYYY-MM-DD HH24:MI:SS') AS REG_DT,
               TO_CHAR(MOD_DT, 'YYYY-MM-DD HH24:MI:SS') AS MOD_DT
          FROM SHORT_URL_MAPPING
         WHERE PROGRAM_ID = #{programId}
           AND USE_YN = 'Y'
    </select>

    <!-- 2. 목록 조회 -->
    <select id="selectShortUrlList" parameterType="java.util.Map" resultMap="shortUrlResultMap">
        SELECT SEQ, PROGRAM_ID, PROGRAM_URL, KUTT_ID, SHORT_URL, KUTT_ADDRESS,
               DESCRIPTION, EXPIRE_IN, USE_YN,
               TO_CHAR(REG_DT, 'YYYY-MM-DD HH24:MI:SS') AS REG_DT,
               TO_CHAR(MOD_DT, 'YYYY-MM-DD HH24:MI:SS') AS MOD_DT
          FROM SHORT_URL_MAPPING
         <where>
            <if test="programId != null and programId != ''">AND PROGRAM_ID = #{programId}</if>
            <if test="useYn != null and useYn != ''">AND USE_YN = #{useYn}</if>
         </where>
         ORDER BY REG_DT DESC
    </select>

    <!-- 3. 등록 -->
    <insert id="insertShortUrl" parameterType="java.util.Map">
        INSERT INTO SHORT_URL_MAPPING (
            SEQ, PROGRAM_ID, PROGRAM_URL, KUTT_ID, SHORT_URL,
            KUTT_ADDRESS, DESCRIPTION, EXPIRE_IN, USE_YN, REG_DT
        ) VALUES (
            SEQ_SHORT_URL.NEXTVAL, #{programId}, #{programUrl}, #{kuttId}, #{shortUrl},
            #{kuttAddress}, #{description}, #{expireIn}, 'Y', SYSDATE
        )
    </insert>

    <!-- 4. 수정 -->
    <update id="updateShortUrl" parameterType="java.util.Map">
        UPDATE SHORT_URL_MAPPING
        <set>
            MOD_DT = SYSDATE,
            <if test="programUrl != null and programUrl != ''">PROGRAM_URL = #{programUrl},</if>
            <if test="description != null">DESCRIPTION = #{description},</if>
            <if test="expireIn != null">EXPIRE_IN = #{expireIn},</if>
        </set>
         WHERE PROGRAM_ID = #{programId}
    </update>

    <!-- 5. 논리 삭제 (USE_YN = 'N') -->
    <update id="deleteShortUrl" parameterType="String">
        UPDATE SHORT_URL_MAPPING
           SET USE_YN = 'N', MOD_DT = SYSDATE
         WHERE PROGRAM_ID = #{programId}
    </update>

    <!--
    물리 삭제 필요 시 위 update 대신 아래 delete 사용:
    <delete id="deleteShortUrl" parameterType="String">
        DELETE FROM SHORT_URL_MAPPING WHERE PROGRAM_ID = #{programId}
    </delete>
    -->

</mapper>
```

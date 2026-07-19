# 단축 URL 보안 설계안 (target/returnUrl 검증)

> 작성: 2026-07-15
> 상태: **설계안 (코드 미반영)** — 검토 후 반영 예정
> 대상: `edms.com.util.shortapi` (Kutt 단축 URL)

---

## 1. 전제 / 컨텍스트

- 인증: **세션(쿠키) 기반 + SSO 로그인**. 엔드포인트는 `/api/short-url/**` (인증 필수, `/api/**`는 CSRF 예외).
- 단축 대상 URL 포맷(예정):
  ```
  http://{sso_login_url}/@{user_id}&returnUrl={실제_보여주고자_하는_URL}
  ```
  → 사용자가 단축 URL 접속 시 **SSO 로그인 후 returnUrl로 이동**하는 구조.
- "전체 URL을 암호화"하여 Kutt에 저장 예정 (user_id·returnUrl 포함 문자열을 암호화).

---

## 2. 위험 요소

| # | 위험 | 설명 |
|---|------|------|
| R1 | **오픈 리다이렉트** | `returnUrl`이 외부 주소면, SSO 인증된 사용자를 임의 외부 사이트로 유도 가능. 단축 URL이 신뢰 도메인이라 사용자·필터가 방심. |
| R2 | 검증 시점 오류 | 암호화 후에는 returnUrl 도메인을 알 수 없음 → **암호화 이전 평문에서 검증**해야 함. |
| R3 | CSRF (세션+`/api/**` off) | 세션 쿠키 인증인데 `/api/**`는 CSRF 예외 → create/update/delete 상태변경이 이론상 위조요청 노출(내부망·SSO 뒤라 리스크는 낮음). |
| R4 | 우회 입력 | 스킴 생략(`//evil.com`), 백슬래시(`https:\\evil.com`), `@` 트릭(`https://internal@evil.com`), 대소문자·인코딩, IP·포트 우회 등. |

---

## 3. 검증 원칙

1. **검증 위치**: `ShortUrlService.createShortUrl()` / `updateShortUrl()` — Kutt 호출(setTarget) 및 암호화 **이전**.
2. **검증 대상**: target 전체가 아니라 **returnUrl 파라미터 값**(내부 도메인 여부). SSO 호스트는 고정이므로 별도.
3. **화이트리스트 방식**: 허용 도메인은 `application.yml` 프로퍼티로 외부화(코드 수정 없이 도메인 추가 가능).
4. **기본 거부(deny by default)**: 화이트리스트에 없으면 거부.

---

## 4. 설정안 (application.yml)

```yaml
shorturl:
  security:
    # returnUrl 로 허용할 내부 도메인 (스킴 무관, 호스트/서브도메인 기준)
    allowed-return-domains:
      - intra.example.go.kr
      - edms.example.go.kr
    # SSO 로그인 호스트 (target 래핑에 사용, 고정)
    sso-login-host: sso.example.go.kr
```

---

## 5. 검증 로직 설계 (의사코드 — 참고용, 미반영)

```
validateReturnUrl(rawTarget):
    # 1) target 에서 returnUrl 파라미터 추출 (평문 상태)
    returnUrl = extractQueryParam(rawTarget, "returnUrl")
    if returnUrl == null or blank:
        reject("returnUrl 누락")

    # 2) URL 파싱 (파싱 실패 자체를 거부)
    uri = parseStrict(returnUrl)          # java.net.URI, 실패 시 예외→거부

    # 3) 스킴 제한: http/https 만 허용 (javascript:, data:, file: 차단)
    if uri.scheme not in [http, https]:
        reject("허용되지 않은 스킴")

    # 4) 호스트 정규화 후 화이트리스트 대조
    host = lowercase(uri.host)            # host 가 null(상대경로/‘//evil’) 이면 거부
    if host == null:
        reject("호스트 없음")
    if not matchesWhitelist(host, allowedReturnDomains):   # 정확일치 또는 .서브도메인 접미사
        reject("허용되지 않은 도메인: " + host)

    # 5) 우회 방어
    #  - userinfo(@) 포함 거부: https://internal@evil.com
    #  - 백슬래시/이중 인코딩 정규화 후 재검사
    #  - 필요 시 포트 화이트리스트

matchesWhitelist(host, allowed):
    for d in allowed:
        if host == d or host.endsWith("." + d):
            return true
    return false
```

호출 지점:
```
createShortUrl(programId, programUrl, ...):
    validateReturnUrl(programUrl)   # ← 실패 시 EdmsException/검증오류 반환
    encrypted = crypto.encrypt(programUrl)   # 검증 통과 후 암호화
    requestDto.setTarget(encrypted)
    kuttApiClient.createLink(requestDto)
```

---

## 6. 반영 시 변경 예상 지점

- `application.yml` : `shorturl.security.*` 추가
- `ShortUrlService` : `validateReturnUrl(...)` 추가 및 create/update 진입부 호출 (암호화 전)
- (선택) 검증 실패 응답 코드: `egovframework.exception.ResponseCode` 에 전용 코드 추가 or 기존 코드 재사용
- (선택) R3 대응: 세션 기반이면 `/api/short-url/**` 상태변경에 Origin/Referer 체크 or 커스텀 헤더 요구 검토

---

## 7. 미결정 사항 (검토 필요)

- allowed-return-domains 실제 목록 확정
- 포트/경로까지 제한할지 (호스트만으로 충분한지)
- 검증 실패 시 UX (차단 메시지 / 로깅 / 관리자 알림)
- 암호화 모듈(공통 CryptoUtil, 현재 롤백됨)과의 연계 시점

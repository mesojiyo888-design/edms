package edms.com.util.shortapi;

import edms.com.util.shortapi.dto.KuttLinkListResponseDto;
import edms.com.util.shortapi.dto.KuttLinkRequestDto;
import edms.com.util.shortapi.dto.KuttLinkResponseDto;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Kutt URL 단축 서비스 REST API 클라이언트
 *
 * Spring WebClient(reactor-netty) 기반. (기존 HttpURLConnection + PATCH 리플렉션 방식은
 * backup/shortapi-httpurlconnection-20260710/ 에 백업됨)
 *
 * - PATCH 를 네이티브 지원 → 리플렉션으로 method 필드를 강제 설정할 필요 없음.
 * - spring-webflux + reactor-netty 는 이미 프로젝트에 존재 → 신규 외부 의존성 없음.
 * - 동기 서비스 계약 유지를 위해 각 호출은 내부에서 block() 처리.
 *
 * [설정 - application.yml]
 *   kutt:
 *     server:
 *       url: http://내부KUTT서버주소
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

    private static final String API_BASE_PATH = "/api/v2/links";
    private static final String API_KEY_HEADER = "X-API-KEY";

    /** Kutt 서버 주소 (예: http://kutt.내부도메인.co.kr) */
    @Value("${kutt.server.url}")
    private String kuttServerUrl;

    /** Kutt API Key (Kutt 설정 페이지에서 발급) */
    @Value("${kutt.api.key}")
    private String kuttApiKey;

    /** HTTP 연결/읽기 타임아웃 (ms, 기본 5초) */
    @Value("${kutt.api.timeout:5000}")
    private int timeoutMs;

    private WebClient webClient;

    @PostConstruct
    private void init() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMs)
                .responseTimeout(Duration.ofMillis(timeoutMs))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS)));

        this.webClient = WebClient.builder()
                .baseUrl(kuttServerUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(API_KEY_HEADER, kuttApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        log.info("[KuttApiClient] WebClient 초기화 완료 - baseUrl: {}, timeout: {}ms", kuttServerUrl, timeoutMs);
    }

    // =========================================================
    // 1. 단축 URL 생성 (POST /api/v2/links)
    // =========================================================

    /**
     * 단축 URL을 생성합니다.
     *
     * @param requestDto 생성 요청 DTO (target 필수, expire_in 선택)
     * @return Kutt API 응답 DTO (id, link 포함)
     * @throws KuttApiException API 호출 실패 시
     */
    public KuttLinkResponseDto createLink(KuttLinkRequestDto requestDto) {
        log.info("[KuttApiClient] createLink - target: {}", requestDto.getTarget());

        return block(webClient.post()
                .uri(API_BASE_PATH)
                .bodyValue(requestDto)
                .retrieve()
                .onStatus(HttpStatus::isError, this::toError)
                .bodyToMono(KuttLinkResponseDto.class));
    }

    // =========================================================
    // 2. 단축 URL 목록 조회 (GET /api/v2/links)
    // =========================================================

    /**
     * 단축 URL 목록을 조회합니다.
     *
     * @param limit  페이지 당 조회 수 (기본 10)
     * @param skip   건너뛸 수 (offset, 기본 0)
     * @return 목록 응답 DTO
     */
    public KuttLinkListResponseDto getLinks(int limit, int skip) {
        log.info("[KuttApiClient] getLinks - limit: {}, skip: {}", limit, skip);

        return block(webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(API_BASE_PATH)
                        .queryParam("limit", limit)
                        .queryParam("skip", skip)
                        .build())
                .retrieve()
                .onStatus(HttpStatus::isError, this::toError)
                .bodyToMono(KuttLinkListResponseDto.class));
    }

    /**
     * 특정 ID의 단축 URL 정보를 조회합니다.
     * (Kutt v2 API는 단건 GET이 없으므로 목록에서 필터링)
     *
     * @param kuttId Kutt 내부 ID (UUID)
     * @return 해당 링크 DTO, 없으면 null
     */
    public KuttLinkResponseDto getLinkById(String kuttId) {
        log.info("[KuttApiClient] getLinkById - id: {}", kuttId);

        // 전체 목록(최대 100)에서 id 매칭으로 단건 조회
        KuttLinkListResponseDto listDto = getLinks(100, 0);
        if (listDto != null && listDto.getData() != null) {
            for (KuttLinkResponseDto link : listDto.getData()) {
                if (kuttId.equals(link.getId())) {
                    return link;
                }
            }
        }
        return null;
    }

    // =========================================================
    // 3. 단축 URL 수정 (PATCH /api/v2/links/{id})
    // =========================================================

    /**
     * 단축 URL을 수정합니다.
     *
     * [수정 정책]
     * - keepTarget=true  : target을 변경하지 않음 → 기존 target을 조회 후 자동 유지
     * - keepTarget=false : 전달받은 requestDto.target 으로 변경
     *
     * @param kuttId     Kutt 내부 ID (UUID)
     * @param requestDto 수정 요청 DTO
     * @param keepTarget true면 기존 target URL 유지
     * @return 수정된 링크 DTO
     */
    public KuttLinkResponseDto updateLink(String kuttId,
                                          KuttLinkRequestDto requestDto,
                                          boolean keepTarget) {
        log.info("[KuttApiClient] updateLink - id: {}, keepTarget: {}", kuttId, keepTarget);

        // keepTarget=true: 기존 target 조회 후 원복
        if (keepTarget) {
            KuttLinkResponseDto existing = getLinkById(kuttId);
            if (existing != null) {
                log.info("[KuttApiClient] keepTarget=true, 기존 target 유지: {}", existing.getTarget());
                requestDto.setTarget(existing.getTarget());
            } else {
                log.warn("[KuttApiClient] keepTarget=true이나 기존 링크 조회 실패. id: {}", kuttId);
            }
        }

        // WebClient는 PATCH를 네이티브 지원 (리플렉션 불필요)
        return block(webClient.patch()
                .uri(API_BASE_PATH + "/" + kuttId)
                .bodyValue(requestDto)
                .retrieve()
                .onStatus(HttpStatus::isError, this::toError)
                .bodyToMono(KuttLinkResponseDto.class));
    }

    // =========================================================
    // 4. 단축 URL 삭제 (DELETE /api/v2/links/{id})
    // =========================================================

    /**
     * 단축 URL을 삭제합니다.
     * 삭제 후 해당 단축 URL로 접근하면 404 반환됩니다.
     *
     * @param kuttId Kutt 내부 ID (UUID)
     * @return 삭제 성공 여부
     */
    public boolean deleteLink(String kuttId) {
        log.info("[KuttApiClient] deleteLink - id: {}", kuttId);

        try {
            block(webClient.delete()
                    .uri(API_BASE_PATH + "/" + kuttId)
                    .retrieve()
                    .onStatus(HttpStatus::isError, this::toError)
                    .toBodilessEntity());
            return true;
        } catch (KuttApiException e) {
            log.error("[KuttApiClient] deleteLink 실패 - id: {}, error: {}", kuttId, e.getMessage());
            return false;
        }
    }

    // =========================================================
    // 공통 처리
    // =========================================================

    /**
     * Mono를 동기적으로 실행(block)하고, 오류를 KuttApiException으로 정규화합니다.
     * (연결 실패/타임아웃 등도 KuttApiException으로 래핑)
     */
    private <T> T block(Mono<T> mono) {
        try {
            return mono.block();
        } catch (KuttApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("[KuttApiClient] Kutt API 연결 실패", e);
            throw new KuttApiException("Kutt API 연결 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 4xx/5xx 응답을 본문과 함께 KuttApiException으로 변환합니다.
     */
    private Mono<? extends Throwable> toError(ClientResponse response) {
        int status = response.rawStatusCode();
        return response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .map(bodyStr -> {
                    log.error("[KuttApiClient] API 오류 응답 - status: {}, body: {}", status, bodyStr);
                    return new KuttApiException("Kutt API 오류: HTTP " + status + " / " + bodyStr);
                });
    }

    // =========================================================
    // 커스텀 예외
    // =========================================================

    /** Kutt API 호출 관련 예외 */
    public static class KuttApiException extends RuntimeException {
        public KuttApiException(String message) { super(message); }
        public KuttApiException(String message, Throwable cause) { super(message, cause); }
    }
}

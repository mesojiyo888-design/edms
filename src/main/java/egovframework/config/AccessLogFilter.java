package egovframework.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

@Component("accessLogFilter")
public class AccessLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AccessLogFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        //System.out.println(">>> 요청 감지: " + request.getMethod() + " " + request.getRequestURI());
        // 💡 [핵심] 일회용 리퀘스트를 여러 번 읽을 수 있는 캐싱 리퀘스트로 래핑합니다.
        ContentCachingRequestWrapper wrappingRequest = new ContentCachingRequestWrapper(request);

        try {
            // 우선 다음 필터 체인(컨트롤러까지) 진행시켜서 데이터를 캐시에 쌓이게 만듭니다.
            filterChain.doFilter(wrappingRequest, response);
        } finally {
            // 컨트롤러 로직이 끝나거나 진행되는 도중에 로그 수집을 시작합니다.
            logRequest(wrappingRequest);
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        String requestURI = request.getRequestURI();
        //System.out.println(">>> 로그 기록 시도: " + requestURI); // 이것이 찍히는지 확인!

        // 정적 리소스(CSS, JS, 이미지 등) 요청은 로그가 너무 많이 쌓이므로 수집 제외
        if (requestURI.contains("/resources/")
                || requestURI.endsWith(".js")
                || requestURI.endsWith(".css")
                || requestURI.endsWith(".png")
                || requestURI.contains("/.well-known/")
                || requestURI.endsWith("/favicon.ico")) {
            return;
        }

        String controllerName = "UNKNOWN_CONTROLLER";
        String methodName = "UNKNOWN_METHOD";

        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 클래스명 추출 (예: TestController)
            controllerName = handlerMethod.getBeanType().getSimpleName();
            // 메서드명 추출 (예: getJsonData)
            methodName = handlerMethod.getMethod().getName();
        }

        String method = request.getMethod();
        String ip = getClientIp(request);
        String userId = getCurrentUserId();

        // 1. 일반 파라미터 수집 (GET 쿼리스트링 및 일반 POST Form 데이터)
        Map<String, String[]> paramMap = request.getParameterMap();
        String queryString = paramMap.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
                .collect(Collectors.joining("&"));

        // 2. JSON 바디 데이터 수집 (Content-Type이 application/json일 때만 작동)
        String bodyData = "";
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            byte[] buf = request.getContentAsByteArray();
            if (buf.length > 0) {
                try {
                    // JSON 데이터를 문자열로 변환 (기본 UTF-8)
                    bodyData = new String(buf, 0, buf.length, request.getCharacterEncoding());
                    // 줄바꿈이나 공백이 있으면 로그가 더러워지므로 한 줄로 압축
                    bodyData = bodyData.replaceAll("\\s+", " ");
                } catch (UnsupportedEncodingException e) {
                    bodyData = "[데이터 인코딩 에러]";
                }
            }
        }

        // 3. 로그 통합 출력 및 DB 저장 포인트
        // 파라미터가 있으면 파라미터를, JSON 바디가 있으면 바디를 노출합니다.
        String finalPayload = !bodyData.isEmpty() ? "BODY: " + bodyData : "PARAM: [" + queryString + "]";
        log.info("#####################################ACCESS start ##############################################");
        log.info("[ACCESS LOG] 유저: {} | IP: {} | 호출: {}.{}() | 메서드: {} | 경로: {} | 데이터: {}",
                userId, ip, controllerName, methodName, method, requestURI, finalPayload);
        log.info("#####################################ACCESS end ##############################################");
        /*
         * 📝 [진짜 실무 저장 로직 배치 공간]
         * 여기에 MyBatis Mapper나 Service를 호출해서 디비에 insert 하시면 됩니다.
         * accessLogService.insertLog(userId, ip, method, requestURI, finalPayload);
         */
    }

    // 접속자 실제 IP 추출용 메서드
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    // 시큐리티 컨텍스트에서 로그인한 사용자 사번/ID 추출용 메서드
    private String getCurrentUserId() {
        if (SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        }
        return "ANONYMOUS"; // 로그인 전이거나 비인증 사용자
    }
}
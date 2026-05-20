package egovframework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@Order(1)
public class SsoIntegratedConfig {

    // ==========================================
    // [1] Spring Security 기본 환경 설정
    // ==========================================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf()
                //.disable()
                .ignoringAntMatchers("/sso/receive", "/dummy-login-process")
                .and()
                .headers().frameOptions().disable()
                .and()
                .authorizeRequests()
                // 로그인 화면, 로그인 처리, 그리고 에러/권한없음 페이지는 무조건 허용
                .antMatchers("/login", "/dummy-login-process", "/error", "/denied").permitAll()
                // 그 외 시스템의 모든 요청은 로그인(인증) 필수
                .anyRequest().authenticated()
                .and()
                // ⭐️ [추가] 인증 실패(비로그인) 시 예외 처리 핸들러 작동
                .exceptionHandling()
                // .authenticationEntryPoint(customAuthenticationEntryPoint()) // 방법 A: 403 에러코드 가기
                .authenticationEntryPoint(customRedirectEntryPoint())          // 방법 B: 커스텀 페이지 가기
                .and()
                .addFilterBefore(new SsoAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new AccessLogFilter(), SsoAuthenticationFilter.class);

        return http.build();
    }

    // ==========================================
    // [추가] 비로그인 사용자 차단 핸들러 정의
    // ==========================================

    // 방법 A: 깔끔하게 브라우저에 브라우저 표준 403 에러창을 던지고 싶을 때
    private AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied - No Session");
        };
    }

    // 방법 B: 우리가 만든 예쁜 "권한이 없습니다" HTML 페이지로 리다이렉트 시키고 싶을 때 (추천)
    private AuthenticationEntryPoint customRedirectEntryPoint() {
        return (request, response, authException) -> {
            response.sendRedirect("/denied");
        };
    }

    // ==========================================
    // [2] SSO 헤더 감지용 시큐리티 필터
    // ==========================================
    public static class SsoAuthenticationFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {

            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            String ssoUserId = request.getHeader("X-SSO-USER-ID");

            if (ssoUserId != null && !ssoUserId.trim().isEmpty()) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(ssoUserId, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        }
    }

    // ==========================================
    // [3] 개발용 컨트롤러 (권한없음 페이지 추가)
    // ==========================================
    @Controller
    public static class DummyLoginController {


        @GetMapping("/login")
        public String dummyLoginPage() {
            return "login/loginSso";
        }

        // 💡 [추가] 권한 없음 안내 화면 호출 컨트롤러
        @GetMapping("/denied")
        public String accessDeniedPage() {
            return "error/error403"; // src/main/resources/templates/error/denied.html 파일 생성 필요
        }

        @PostMapping("/dummy-login-process")
        public String dummyLoginProcess(@RequestParam("userId") String userId, HttpServletRequest request) {
            if (userId != null && !userId.trim().isEmpty()) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                HttpSession session = request.getSession(true);
                session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

                return "redirect:/main";
            }
            return "redirect:/login?error";
        }

        @GetMapping("/main")
        public String mainPage(Principal principal, Model model) {
            if (principal != null) {
                model.addAttribute("userId", principal.getName());
            } else {
                model.addAttribute("userId", "인증 실패 - 세션 없음");
            }
            return "main/main";
        }
    }
}
package egovframework.config;

import egovframework.security.EgovSecurityMetadataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@Order(1)
public class SsoIntegratedConfig {

    @Autowired
    private EgovSecurityMetadataSource egovSecurityMetadataSource;

    // ==========================================
    // [1] 동적 권한 판단 - AccessDecisionManager
    // ==========================================
    @Bean
    public AccessDecisionManager accessDecisionManager() {
        return new AffirmativeBased(
                Arrays.asList(new RoleVoter(), new AuthenticatedVoter())
        );
    }

    // ==========================================
    // [2] 동적 권한 인터셉터 - DB URL 매핑 적용
    // ==========================================
    @Bean
    public FilterSecurityInterceptor filterSecurityInterceptor() throws Exception {
        FilterSecurityInterceptor interceptor = new FilterSecurityInterceptor();
        interceptor.setSecurityMetadataSource(egovSecurityMetadataSource);
        interceptor.setAccessDecisionManager(accessDecisionManager());
        interceptor.setRejectPublicInvocations(false);
        return interceptor;
    }

    @Bean
    public SsoAuthenticationFilter ssoAuthenticationFilter() {
        return new SsoAuthenticationFilter();
    }

    // ==========================================
    // [3] Spring Security 기본 환경 설정
    // ==========================================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf()
                .ignoringRequestMatchers(
                        new AntPathRequestMatcher("/logout"),
                        new AntPathRequestMatcher("/dummy-login-process"),
                        new AntPathRequestMatcher("/api/**"),
                        new AntPathRequestMatcher("/sso/callback")
                )
                .and()
                .headers().frameOptions().disable()
                .and()
                .authorizeHttpRequests(auth -> auth
                        // 공개 URL은 Security에서 고정 허용
                        .requestMatchers(
                                "/", "/login", "/dummy-login-process",
                                "/error", "/denied", "/logout",
                                // springdoc
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()
                        //.requestMatchers("/admin/**").hasRole("ADMIN")
                        // 나머지는 인증 필수 — 세부 Role 판단은 FilterSecurityInterceptor가 담당
                        .anyRequest().authenticated()
                )
                .exceptionHandling()
                .authenticationEntryPoint(customRedirectEntryPoint())
                .and()
                .addFilterBefore(ssoAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                // DB 기반 동적 권한 인터셉터 등록
                .addFilterBefore(filterSecurityInterceptor(), FilterSecurityInterceptor.class)
                ;

        return http.build();
    }

    // ==========================================
    // [4] 비로그인 차단 핸들러
    // ==========================================
    private AuthenticationEntryPoint customRedirectEntryPoint() {
        return (request, response, authException) -> response.sendRedirect("/denied");
    }

    // ==========================================
    // [5] SSO 헤더 감지 필터
    // ==========================================
    public static class SsoAuthenticationFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {

            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            String ssoUserId = request.getHeader("X-SSO-USER-ID");
            if (ssoUserId != null && !ssoUserId.trim().isEmpty()) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                ssoUserId, null, Collections.emptyList()
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        }
    }

    // ==========================================
    // [6] 개발용 컨트롤러
    // ==========================================
    @Controller
    public static class DummyLoginController {
        private static final Logger log = LoggerFactory.getLogger(DummyLoginController.class);

        @Autowired
        private EgovSecurityMetadataSource egovSecurityMetadataSource;

        @GetMapping({"/", "/login"})
        public String dummyLoginPage() {
            return "login/loginSso";
        }

        @GetMapping("/denied")
        public String accessDeniedPage() {
            return "error/error403";
        }

        @PostMapping("/dummy-login-process")
        public String dummyLoginProcess(@RequestParam("userId") String userId,
                                        HttpServletRequest request) {
            if (userId != null && !userId.trim().isEmpty()) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId, null, Collections.emptyList()
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                HttpSession session = request.getSession(true);
                session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
                return "redirect:/main";
            }
            return "redirect:/login?error";
        }

        @GetMapping("/main")
        public String mainPage(Principal principal, Model model) {
            model.addAttribute("userId",
                    principal != null ? principal.getName() : "인증 실패 - 세션 없음");
            return "main/main";
        }

        @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
        public String logout(HttpServletRequest request, HttpServletResponse response) {
            HttpSession session = request.getSession(false);
            if (session != null) session.invalidate();

            Cookie cookie = new Cookie("JSESSIONID", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);

            return "redirect:/login";
        }

        // ⭐️ 관리자가 권한 변경 후 재로드 호출
        @PostMapping("/admin/role/reload")
        @ResponseBody
        public String reloadRoles() {
            egovSecurityMetadataSource.reload();
            return "권한 정보가 재로드되었습니다.";
        }
    }
}
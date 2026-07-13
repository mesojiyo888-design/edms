package egovframework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.exception.ApiResponse;
import egovframework.exception.ResponseCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class SsoAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        if(isAjax(request)) {
            writeJson(response, HttpStatus.UNAUTHORIZED, "인증이 필요합니다. 다시 로그인해주세요.", ResponseCode.FAIL_UNAUTHORIZED.getCode());
        } else {
            response.sendRedirect(request.getContextPath() + "/login"); // 기존 로그인 진입점으로 교체
        }
    }

    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    private void writeJson(HttpServletResponse response, HttpStatus status, String message, String code) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        try(PrintWriter writer = response.getWriter()) {
            writer.write(objectMapper.writeValueAsString(ApiResponse.fail(message, code)));
        }
    }
}
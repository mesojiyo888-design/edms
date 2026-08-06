package egovframework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.exception.ApiResponse;
import egovframework.exception.ResponseCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class SsoAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        if(isAjax(request)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            try(PrintWriter writer = response.getWriter()) {
                writer.write(objectMapper.writeValueAsString(ApiResponse.fail("접근 권한이 없습니다.", ResponseCode.FAIL_FORBIDDEN.getCode())));
            }
        } else {
            request.setAttribute("errorCode", HttpStatus.FORBIDDEN.value());
            request.setAttribute("errorMessage", "접근 권한이 없습니다.");
            response.setStatus(HttpStatus.FORBIDDEN.value());
            request.getRequestDispatcher("/WEB-INF/jsp/error/error.jsp").forward(request, response);
        }
    }

    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }
}
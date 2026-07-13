package egovframework.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.exception.ApiResponse;
import egovframework.exception.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class GlobalFilterExceptionHandler implements Filter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        try {
            chain.doFilter(req, res);
        } catch (Throwable t) {
            log.error("Filter chain / JSP rendering exception: ", t);

            if (response.isCommitted()) {
                log.warn("Response already committed. Cannot handle error response.");
                return;
            }

            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            String message = "시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";

            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setStatus(status.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                try (PrintWriter writer = response.getWriter()) {
                    writer.write(objectMapper.writeValueAsString(
                            ApiResponse.fail(message, ResponseCode.FAIL_SYSTEM.getCode())));
                }
            } else {
                try {
                    response.setStatus(status.value());
                    request.setAttribute("errorCode", status.value());
                    request.setAttribute("errorMessage", message);
                    request.getRequestDispatcher("/WEB-INF/jsp/error/error.jsp")
                            .forward(request, response);
                } catch (ServletException se) {
                    // errorPage.jsp 자체가 깨진 경우 최후의 방어선
                    log.error("Failed to forward to error page: ", se);
                    response.getWriter().write("System Error");
                }
            }
        }
    }
}
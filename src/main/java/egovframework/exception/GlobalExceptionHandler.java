package egovframework.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    // 1. 비즈니스 예외
    @ExceptionHandler(EdmsException.class)
    public ModelAndView handleBusinessException(EdmsException e, HandlerMethod handlerMethod,
                                                HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.error("EdmsException: {}", e.getMessage(), e);
        return respond(handlerMethod, request, response,
                HttpStatus.OK, ApiResponse.fail(e.getMessage(), e.getMessageCode()),
                "error/error", e.getMessage(), e.getMessageCode());
    }

    // 2. 시스템 예외 (알려진 타입)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleSystemException(Exception e, HandlerMethod handlerMethod,
                                              HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.error("System Unhandled Exception: ", e);

        HttpStatus status;
        ResponseCode code;

        if (e instanceof NoHandlerFoundException) {
            status = HttpStatus.NOT_FOUND;
            code = ResponseCode.FAIL_NOT_FOUND;
        } else if (e instanceof HttpRequestMethodNotSupportedException) {
            status = HttpStatus.METHOD_NOT_ALLOWED;
            code = ResponseCode.FAIL_METHOD_NOT_ALLOWED;
        } else if (e instanceof DataAccessException) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            code = ResponseCode.FAIL_DB;
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            code = ResponseCode.FAIL_SYSTEM;
        }

        return respond(handlerMethod, request, response,
                status, ApiResponse.fail(e.getMessage(), code.getCode()),
                "error/error", e.getMessage(), code.getCode());
    }

    // 3. 최후의 방어선
    @ExceptionHandler(Throwable.class)
    public ModelAndView handleAllExceptions(Throwable t, HandlerMethod handlerMethod,
                                            HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.error("Unhandled Global Exception: ", t);

        String message = "시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        String messageCode = ResponseCode.FAIL_SYSTEM.getCode();

        return respond(handlerMethod, request, response,
                HttpStatus.INTERNAL_SERVER_ERROR, ApiResponse.fail(message, messageCode),
                "error/error", message, messageCode);
    }

    /**
     * REST 컨트롤러(or @ResponseBody 메서드) / AJAX 요청 → JSON
     * 일반 컨트롤러 + 화면 요청 → ModelAndView
     */
    private ModelAndView respond(HandlerMethod handlerMethod, HttpServletRequest request, HttpServletResponse response,
                                 HttpStatus status, Object apiBody,
                                 String viewName, String errorMessage, String messageCode) throws Exception {

        if (isRestOrAjax(handlerMethod, request)) {
            response.setStatus(status.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter writer = response.getWriter()) {
                log.info("Responding with JSON: {}", objectMapper.writeValueAsString(apiBody));
                writer.write(objectMapper.writeValueAsString(apiBody));
            }
            return null; // JSON 이미 write 했으니 뷰 렌더링 안 함
        }

        ModelAndView mav = new ModelAndView(viewName);
        mav.addObject("errorCode", status.value());
        mav.addObject("errorMessage", errorMessage);
        mav.addObject("messageCode", messageCode);
        return mav;
    }

    private boolean isRestOrAjax(HandlerMethod handlerMethod, HttpServletRequest request) {
        // 1. 핸들러 메서드 자체가 @ResponseBody / @RestController면 무조건 JSON
        if (handlerMethod != null) {
            boolean hasResponseBody = handlerMethod.hasMethodAnnotation(org.springframework.web.bind.annotation.ResponseBody.class)
                    || handlerMethod.getBeanType().isAnnotationPresent(org.springframework.web.bind.annotation.RestController.class);
            if (hasResponseBody) {
                return true;
            }
        }

        // 2. X-Requested-With 헤더 (jQuery 등)
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return true;
        }

        // 3. Accept 헤더가 JSON을 명시적으로 요구하는 경우 (axios/fetch 대응)
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            return true;
        }

        return false;
    }


}
package egovframework.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.dao.DataAccessException;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 비즈니스 예외 처리
    @ExceptionHandler(EdmsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(EdmsException e) {
        log.error("EdmsException: {}", e.getMessage(), e);
        return ResponseEntity.ok(ApiResponse.fail(e.getMessage(), e.getMessageCode()));
    }

    // 2. 시스템 예외 처리
    @ExceptionHandler(Exception.class)
    public Object handleSystemException(Exception e, HttpServletRequest request) {
        log.error("System Unhandled Exception: ", e);

        HttpStatus status;
        ResponseCode code;

        // 예외 타입에 따른 상태 코드 및 ResponseCode 매핑
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

        // AJAX 요청 확인
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return ResponseEntity.status(status).body(ApiResponse.fail(e.getMessage(), code.getCode()));
        } else {
            // 일반 페이지 요청
            ModelAndView mav = new ModelAndView("error/error");
            mav.addObject("errorCode", status.value());
            mav.addObject("errorMessage", e.getMessage());
            mav.addObject("messageCode", code.getCode()); // JSP에서 필요시 사용
            return mav;
        }
    }

    @ExceptionHandler(Throwable.class)
    public Object handleAllExceptions(Throwable t, HttpServletRequest request) {
        log.error("Unhandled Global Exception: ", t);

        String message = "시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        String messageCode = ResponseCode.FAIL_SYSTEM.getCode();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // AJAX 요청이면 JSON 반환
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return ResponseEntity.status(status)
                    .body(ApiResponse.fail(message, messageCode));
        } else {
            // 일반 페이지면 에러 페이지로
            ModelAndView mav = new ModelAndView("error/errorPage");
            mav.addObject("errorCode", status.value());
            mav.addObject("errorMessage", message);
            return mav;
        }
    }
}
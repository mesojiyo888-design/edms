package edms.llm.exception;

import edms.llm.dto.LlmResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * LLM Service 예외 핸들러
 */
@RestControllerAdvice
public class LlmExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(LlmExceptionHandler.class);

    /**
     * Bean Validation 오류
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<LlmResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> messages = new ArrayList<String>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            messages.add(fe.getDefaultMessage());
        }
        String joined = StringUtils.join(messages);
        log.debug("[ERROR] - [LlmExceptionHandler.handleValidation] - [입력검증 실패] {}", joined);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(LlmResponse.error(joined, "n/a"));
    }

    /**
     * 비즈니스 입력 검증 오류
     */
    @ExceptionHandler(InputValidationException.class)
    public ResponseEntity<LlmResponse> handleInputValidation(InputValidationException ex) {
        log.debug("[ERROR] - [LlmExceptionHandler.handleInputValidation] - [입력검증 실패] {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(LlmResponse.error(ex.getMessage(), "n/a"));
    }

    /**
     * LLM 호출 오류
     */
    @ExceptionHandler(LlmException.class)
    public ResponseEntity<LlmResponse> handleLlm(LlmException ex) {
        log.error("[ERROR] - [LlmExceptionHandler.handleLlm] - [LLM오류] {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(LlmResponse.error("LLM 서버와 통신 중 오류가 발생했습니다.", "unknown"));
    }

    /**
     * 예상치 못한 오류
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<LlmResponse> handleGeneral(Exception ex) {
        log.error("[ERROR] - [LlmExceptionHandler.handleGeneral] - [내부 오류]", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(LlmResponse.error("서버 내부 오류가 발생했습니다.", "unknown"));
    }
}

package edms.llm.service;

import edms.llm.config.LlmProperties;
import edms.llm.exception.InputValidationException;
import edms.llm.exception.LlmException;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LLM 서비스 공통 추상 클래스.
 *
 * <p>역할:
 * <ul>
 *   <li>입력 검증 및 정제 (모든 구현체가 공유)</li>
 *   <li>기본 시스템 프롬프트 관리</li>
 *   <li>LLM 호출 예외를 {@link LlmException} 으로 래핑</li>
 *   <li>eGovFrame {@link EgovAbstractServiceImpl} 상속으로 표준 logger 제공</li>
 * </ul>
 *
 * <p>구현체({@code OllamaLlmServiceImpl}, {@code VllmLlmServiceImpl})는
 * 이 클래스를 상속하고 {@link LlmService} 를 구현합니다.
 * 구현체는 실제 LLM 호출({@link #doComplete}, {@link #doChat})만 담당합니다.
 *
 * <p>보안:
 * <ul>
 *   <li>maxInputLength 초과 입력 거부 (DoS/과도한 토큰 소비 방지)</li>
 *   <li>systemPrompt 와 userMessage 역할 분리 (프롬프트 인젝션 기초 방어)</li>
 *   <li>LLM 호출 예외 래핑으로 내부 오류 상세 미노출</li>
 * </ul>
 */
public abstract class AbstractLlmService extends EgovAbstractServiceImpl implements LlmService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLlmService.class);

    private static final String DEFAULT_SYSTEM_PROMPT = "You are a helpful assistant. Answer clearly and concisely in the same language as the user.";

    private static final int MAX_SYSTEM_PROMPT_LENGTH = 1000;

    /**
     * LlmConfig 의 @Bean 메서드에서 생성자로 주입.
     * final 선언으로 구현체에서 재할당 불가.
     */
    protected final LlmProperties llmProperties;

    protected AbstractLlmService(LlmProperties llmProperties) {
        this.llmProperties = llmProperties;
    }

    // ----------------------------------------------------------
    // LlmService 구현 (검증 → 훅 위임) - final 로 재정의 차단
    // ----------------------------------------------------------

    /**
     * {@inheritDoc}
     * <p>입력 검증·정제 후 {@link #doComplete} 에 위임합니다.
     */
    @Override
    public final String complete(String prompt) {
        String sanitized = validateAndSanitize(prompt, "prompt");
        logger.debug("[LLM] - [AbstractLlmService.complete] complete | service={} | len={}", getServiceName(), sanitized.length());

        try {
            return doComplete(sanitized);
        } catch (LlmException e) {
            // 이미 래핑된 예외는 그대로 전파
            throw e;
        } catch (Exception e) {
            logger.error("[LLM] - [AbstractLlmService.complete] complete 실패 | service={} | error={}", getServiceName(), e);
            throw new LlmException("[LLM] complete 요청 실패: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>입력 검증·정제 후 {@link #doChat} 에 위임합니다.
     */
    @Override
    public final String chat(String systemPrompt, String userMessage) {
        String sanitizedUser = validateAndSanitize(userMessage, "userMessage");
        String resolvedSystem = resolveSystemPrompt(systemPrompt);
        logger.debug("[LLM] - [AbstractLlmService.chat] chat | service={} | len={}", getServiceName(), sanitizedUser.length());

        try {
            return doChat(resolvedSystem, sanitizedUser);
        } catch (LlmException e) {
            throw e;
        } catch (Exception e) {
            logger.error("[LLM] - [AbstractLlmService.chat] chat 실패 | service={} | error={}", getServiceName(), e);
            throw new LlmException("[LLM] chat 요청 실패: " + e.getMessage(), e);
        }
    }

    // ----------------------------------------------------------
    // 구현체에서 반드시 구현할 훅 메서드
    // ----------------------------------------------------------

    /**
     * 실제 LLM complete 호출.
     * 호출 시점에 prompt 는 이미 검증·정제된 상태입니다.
     *
     * @param prompt 검증·정제된 사용자 프롬프트
     * @return 모델 응답 텍스트
     */
    protected abstract String doComplete(String prompt);

    /**
     * 실제 LLM chat 호출.
     * 호출 시점에 두 파라미터 모두 검증·정제된 상태입니다.
     *
     * @param systemPrompt 검증·정제된 시스템 프롬프트
     * @param userMessage  검증·정제된 사용자 메시지
     * @return 모델 응답 텍스트
     */
    protected abstract String doChat(String systemPrompt, String userMessage);

    // ----------------------------------------------------------
    // 공통 검증 헬퍼 (private - 구현체에서 직접 호출 불가)
    // ----------------------------------------------------------

    private String validateAndSanitize(String input, String fieldName) {
        if (input == null || input.trim().isEmpty()) {
            throw new InputValidationException(fieldName + " 은(는) 필수입니다.");
        }
        String trimmed = input.trim();
        if (trimmed.length() > llmProperties.getMaxInputLength()) {
            throw new InputValidationException(
                    String.format("%s 이(가) 최대 길이(%d자)를 초과합니다. 현재: %d자",
                            fieldName, llmProperties.getMaxInputLength(), trimmed.length()));
        }
        return trimmed;
    }

    private String resolveSystemPrompt(String systemPrompt) {
        if (systemPrompt == null || systemPrompt.trim().isEmpty()) {
            return DEFAULT_SYSTEM_PROMPT;
        }
        String trimmed = systemPrompt.trim();
        if (trimmed.length() > MAX_SYSTEM_PROMPT_LENGTH) {
            throw new InputValidationException(
                    String.format("systemPrompt 는 %d자 이하여야 합니다.", MAX_SYSTEM_PROMPT_LENGTH));
        }
        return trimmed;
    }
}

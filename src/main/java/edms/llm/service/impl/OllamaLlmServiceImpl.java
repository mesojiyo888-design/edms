package edms.llm.service.impl;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.output.Response;
import edms.llm.config.LlmConfig;
import edms.llm.config.LlmProperties;
import edms.llm.service.AbstractLlmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

/**
 * Ollama 백엔드 LLM 서비스 구현체.
 *
 * <p>패키지 구조:
 * <pre>
 *   service/
 *     LlmService.java          (interface)
 *     AbstractLlmService.java  (abstract class)
 *     impl/
 *       OllamaLlmServiceImpl.java  ← 이 클래스
 *       VllmLlmServiceImpl.java
 * </pre>
 *
 * <p>상속 구조:
 * <pre>
 *   EgovAbstractServiceImpl
 *     └── AbstractLlmService  (검증, 예외 래핑)
 *           └── OllamaLlmServiceImpl  (실제 Ollama 호출)
 * </pre>
 *
 * <p>{@code llm.provider=ollama} 일 때 {@link LlmConfig}
 * 의 {@code @Bean} 메서드에서 단일 인스턴스로 생성됩니다.
 *
 * <p>보안:
 * <ul>
 *   <li>logRequests/logResponses 기본 false (프롬프트 로그 노출 방지)</li>
 *   <li>timeout 설정으로 무한 대기 방지</li>
 *   <li>입력 검증은 부모 AbstractLlmService 에서 처리 (중복 구현 없음)</li>
 * </ul>
 */

public class OllamaLlmServiceImpl extends AbstractLlmService {

    private static final Logger logger = LoggerFactory.getLogger(OllamaLlmServiceImpl.class);

    private final OllamaChatModel chatModel;

    /**
     * {@link LlmConfig} 의 {@code @Bean} 에서 호출.
     *
     * @param llmProperties {@code @Value} 로 바인딩된 설정 객체
     */
    public OllamaLlmServiceImpl(LlmProperties llmProperties) {
        super(llmProperties);

        this.chatModel = OllamaChatModel.builder()
                .baseUrl(llmProperties.getOllamaBaseUrl())
                .modelName(llmProperties.getModel())
                .temperature(llmProperties.getTemperature())
                .timeout(Duration.ofSeconds(llmProperties.getTimeoutSeconds()))
                .logRequests(llmProperties.isOllamaLogReq())
                .logResponses(llmProperties.isOllamaLogRes())
                .build();

        logger.info("[LLM] - OllamaLlmServiceImpl 초기화 | baseUrl={}, model={}", llmProperties.getOllamaBaseUrl(), llmProperties.getModel());
    }

    /**
     * Ollama 단순 완성 호출.
     * 입력 검증은 {@link AbstractLlmService#complete} 에서 완료된 상태입니다.
     */
    @Override
    protected String doComplete(String prompt) {
        logger.debug("[LLM] - [Ollama] - OllamaLlmServiceImpl doComplete 실행");
        Response<AiMessage> response = chatModel.generate(
                Collections.singletonList(UserMessage.from(prompt))
        );
        return response.content().text();
    }

    /**
     * Ollama 채팅 완성 호출.
     * 입력 검증은 {@link AbstractLlmService#chat} 에서 완료된 상태입니다.
     */
    @Override
    protected String doChat(String systemPrompt, String userMessage) {
        logger.debug("[LLM] - [Ollama] - OllamaLlmServiceImpl doChat 실행");
        Response<AiMessage> response = chatModel.generate(
                Arrays.asList(
                        SystemMessage.from(systemPrompt),
                        UserMessage.from(userMessage)
                )
        );
        return response.content().text();
    }

    @Override
    public String getServiceName() {
        return "ollama";
    }
}

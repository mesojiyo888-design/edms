package edms.llm.service.impl;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import edms.llm.config.LlmConfig;
import edms.llm.config.LlmProperties;
import edms.llm.service.AbstractLlmService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

/**
 * vLLM 백엔드 LLM 서비스 구현체.
 *
 * <p>패키지 구조:
 * <pre>
 *   service/
 *     LlmService.java          (interface)
 *     AbstractLlmService.java  (abstract class)
 *     impl/
 *       OllamaLlmServiceImpl.java
 *       VllmLlmServiceImpl.java  ← 이 클래스
 * </pre>
 *
 * <p>상속 구조:
 * <pre>
 *   EgovAbstractServiceImpl
 *     └── AbstractLlmService  (검증, 예외 래핑)
 *           └── VllmLlmServiceImpl  (실제 vLLM 호출)
 * </pre>
 *
 * <p>vLLM 은 OpenAI 호환 REST API(/v1/chat/completions)를 제공하므로
 * LangChain4j 의 {@link OpenAiChatModel} 을 재사용합니다.
 *
 * <p>{@code llm.provider=vllm} 일 때 {@link LlmConfig}
 * 의 {@code @Bean} 메서드에서 단일 인스턴스로 생성됩니다.
 *
 * <p>보안:
 * <ul>
 *   <li>API Key 는 환경변수 LLM_VLLM_API_KEY 로만 주입 (properties 평문 금지)</li>
 *   <li>"none" 이면 더미 값 사용 (vLLM 인증 비활성 시)</li>
 *   <li>입력 검증은 부모 AbstractLlmService 에서 처리 (중복 구현 없음)</li>
 * </ul>
 */
public class VllmLlmServiceImpl extends AbstractLlmService {

    private static final Logger logger = LoggerFactory.getLogger(VllmLlmServiceImpl.class);

    private final OpenAiChatModel chatModel;

    /**
     * {@link LlmConfig} 의 {@code @Bean} 에서 호출.
     *
     * @param llmProperties {@code @Value} 로 바인딩된 설정 객체
     */
    public VllmLlmServiceImpl(LlmProperties llmProperties) {
        super(llmProperties);

        // "none" 또는 빈 값이면 더미 키 사용 (vLLM 인증 비활성 시)
        String apiKey = StringUtils.isBlank(llmProperties.getVllmApiKey())
                || "none".equalsIgnoreCase(llmProperties.getVllmApiKey())
                ? "no-auth-required"
                : llmProperties.getVllmApiKey();

        this.chatModel = OpenAiChatModel.builder()
                .baseUrl(llmProperties.getVllmBaseUrl())
                .apiKey(apiKey)
                .modelName(llmProperties.getModel())
                .temperature(llmProperties.getTemperature())
                .timeout(Duration.ofSeconds(llmProperties.getTimeoutSeconds()))
                .logRequests(llmProperties.isVllmLogReq())
                .logResponses(llmProperties.isVllmLogRes())
                .build();

        logger.info("[LLM] - [vLLM] - VllmLlmServiceImpl 초기화 | baseUrl={}, model={}", llmProperties.getVllmBaseUrl(), llmProperties.getModel());
    }

    /**
     * vLLM 단순 완성 호출.
     * 입력 검증은 {@link AbstractLlmService#complete} 에서 완료된 상태입니다.
     */
    @Override
    protected String doComplete(String prompt) {
        logger.debug("[LLM] - [vLLM] - VllmLlmServiceImpl doComplete 실행");
        Response<AiMessage> response = chatModel.generate(
                Collections.singletonList(UserMessage.from(prompt))
        );
        return response.content().text();
    }

    /**
     * vLLM 채팅 완성 호출.
     * 입력 검증은 {@link AbstractLlmService#chat} 에서 완료된 상태입니다.
     */
    @Override
    protected String doChat(String systemPrompt, String userMessage) {
        logger.debug("[LLM] - [vLLM] - VllmLlmServiceImpl doChat 실행");
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
        return "vllm";
    }

}

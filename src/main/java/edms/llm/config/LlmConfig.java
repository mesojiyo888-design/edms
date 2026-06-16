package edms.llm.config;

import edms.llm.service.LlmService;
import edms.llm.service.impl.OllamaLlmServiceImpl;
import edms.llm.service.impl.VllmLlmServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LLM 서비스 Java 설정 클래스.
 *
 * <p>{@link LlmProperties} 의 {@code llm.provider} 값을 읽어
 * {@link LlmService} 구현체를 하나만 Bean 으로 등록합니다.
 *
 * <p>프로바이더 전환 방법:
 * <pre>
 *   application.yml 에서 아래 한 줄만 변경 후 재배포
 *   llm.provider=ollama   →  OllamaLlmServiceImpl 활성화
 *   llm.provider=vllm     →  VllmLlmServiceImpl   활성화
 * </pre>
 *
 * <p>새 백엔드 추가 방법:
 * <pre>
 *   1. AbstractLlmService 를 상속하는 XxxLlmServiceImpl 작성
 *   2. 아래 llmService() 메서드에 else-if 분기 추가
 *   3. application.yml 에서 llm.provider=xxx 로 전환
 * </pre>
 */
@Configuration
public class LlmConfig {
    /**
     * {@code llm.provider} 값에 따라 LlmService 구현체를 단일 Bean 으로 등록.
     *
     * <p>두 구현체를 동시에 등록하지 않으므로 ambiguous bean 문제가 발생하지 않습니다.
     *
     * @param props {@code application.yml} 값이 매핑된 설정 객체
     * @return 활성화된 LlmService 구현체
     * @throws IllegalArgumentException 지원하지 않는 provider 값인 경우
     */

    @Bean
    public LlmService llmService(LlmProperties props) {
        String provider = props.getProvider();

        if ("ollama".equalsIgnoreCase(provider)) {
            return new OllamaLlmServiceImpl(props);
        }

        if ("vllm".equalsIgnoreCase(provider)) {
            return new VllmLlmServiceImpl(props);
        }

        // 잘못된 provider 값은 애플리케이션 기동 시점에 즉시 감지
        throw new IllegalArgumentException("지원하지 않는 llm.provider 값입니다: [" + provider + "]. " + "허용값: ollama, vllm");
    }
}

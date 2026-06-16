package edms.llm.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * LLM 설정 값 보유 컴포넌트.
 *
 * <p>{@code @Value} 로 {@code application.yml} 의 값을 필드에 매핑합니다.
 * {@link LlmConfig} 의 {@code @Bean} 메서드에서 이 객체를 주입받아
 * 구현체 생성에 사용합니다.
 *
 * <p>보안:
 * <ul>
 *   <li>vllmApiKey 는 환경변수(LLM_VLLM_API_KEY)로만 주입</li>
 *   <li>properties 파일에 API Key 평문 작성 금지</li>
 * </ul>
 */
@Getter
@Component
public class LlmProperties {

    // ----------------------------------------------------------
    // 공통
    // ----------------------------------------------------------

    /**
     * 활성 프로바이더: "ollama" | "vllm"
     */
    @Value("${llm.provider:ollama}")
    private String provider;

    /**
     * 사용 모델명 (예: gpt-oss:20b)
     */
    @Value("${llm.model:gpt-oss:20b}")
    private String model;

    /**
     * HTTP 요청 타임아웃 (초)
     */
    @Value("${llm.timeout-seconds:120}")
    private long timeoutSeconds;

    /**
     * 최대 입력 문자 수.
     * 보안: 과도한 토큰 소비 / DoS 방지용 상한선
     */
    @Value("${llm.max-input-length:4000}")
    private int maxInputLength;

    /**
     * 생성 온도 (0.0 ~ 1.0)
     */
    @Value("${llm.temperature:0.7}")
    private double temperature;

    // ----------------------------------------------------------
    // Ollama
    // ----------------------------------------------------------

    @Value("${llm.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    /**
     * 운영에서는 반드시 false - 프롬프트 로그 노출 방지
     */
    @Value("${llm.ollama.log-requests:false}")
    private boolean ollamaLogReq;

    @Value("${llm.ollama.log-responses:false}")
    private boolean ollamaLogRes;

    // ----------------------------------------------------------
    // vLLM
    // ----------------------------------------------------------

    @Value("${llm.vllm.base-url:http://localhost:8000/v1}")
    private String vllmBaseUrl;

    /**
     * vLLM API Key.
     * 환경변수 LLM_VLLM_API_KEY 로 주입. "none" 이면 인증 없이 연결.
     */
    @Value("${LLM_VLLM_API_KEY:none}")
    private String vllmApiKey;

    /**
     * 운영에서는 반드시 false - 프롬프트 로그 노출 방지
     */
    @Value("${llm.vllm.log-requests:false}")
    private boolean vllmLogReq;

    @Value("${llm.vllm.log-responses:false}")
    private boolean vllmLogRes;

}

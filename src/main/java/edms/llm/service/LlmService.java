package edms.llm.service;

/**
 * LLM 서비스 인터페이스.
 *
 * <p>백엔드(Ollama, vLLM 등)에 무관하게 동일한 계약을 정의합니다.
 * Controller 는 이 인터페이스에만 의존하므로
 * 구현체가 교체되어도 상위 레이어는 수정이 필요 없습니다.
 *
 * <p>새 백엔드 추가 방법:
 * <pre>
 *   1. AbstractLlmService 를 상속하는 구현 클래스 작성
 *   2. LlmConfig.java 에 Bean 수정
 *   4. application.yml 에서 llm.provider=새값 으로 전환
 * </pre>
 */
public interface LlmService {

    /**
     * 단순 텍스트 완성.
     *
     * @param prompt 사용자 프롬프트 (검증은 AbstractLlmService 에서 수행)
     * @return 모델 응답 텍스트
     */
    String complete(String prompt);

    /**
     * 시스템 프롬프트 + 사용자 메시지 채팅 완성.
     *
     * @param systemPrompt 시스템 지시사항 (null/빈값이면 기본값 사용)
     * @param userMessage  사용자 메시지
     * @return 모델 응답 텍스트
     */
    String chat(String systemPrompt, String userMessage);

    /**
     * 현재 구현체의 서비스 식별자 반환 (로깅/모니터링용).
     *
     * @return 예: "ollama", "vllm"
     */
    String getServiceName();
}
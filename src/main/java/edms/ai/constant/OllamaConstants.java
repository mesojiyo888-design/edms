package edms.ai.constant;

public final class OllamaConstants {

    private OllamaConstants() {}

    // 추론 레벨
    public static final String REASONING_LOW    = "Reasoning: low";
    public static final String REASONING_MEDIUM = "Reasoning: medium";
    public static final String REASONING_HIGH   = "Reasoning: high";

    // 역할
    public static final String ROLE_SYSTEM    = "system";
    public static final String ROLE_USER      = "user";
    public static final String ROLE_ASSISTANT = "assistant";

    // 시스템 프롬프트
    public static final String SYSTEM_DEFAULT   = "\n당신은 문서 관리 시스템의 AI 어시스턴트입니다. 한국어로 답변하세요.";
    public static final String SYSTEM_SUMMARIZE = "\n문서 분석 전문가입니다. 한국어로 답변하세요.";
    public static final String SYSTEM_RECOMMEND = "\n문서 관련성 분석 전문가입니다. 한국어로 답변하세요.";

    // SSE
    public static final String SSE_EVENT_DONE = "done";
    public static final String SSE_DATA_DONE  = "[DONE]";
}
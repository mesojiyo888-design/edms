package edms.llm.web;

import edms.llm.dto.ChatRequest;
import edms.llm.dto.CompleteRequest;
import edms.llm.dto.LlmResponse;
import edms.llm.service.LlmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * LLM REST API 컨트롤러.
 *
 * <pre>
 * POST /api/llm/complete  - 단순 텍스트 완성
 * POST /api/llm/chat      - 시스템 프롬프트 + 사용자 메시지 채팅
 * GET  /api/llm/service   - 현재 활성 서비스 구현체 확인
 * </pre>
 *
 * <p>llmService 는 {@link LlmService} 인터페이스 타입으로 주입받습니다.
 * application.yml의 provider 설정에 따라
 * OllamaLlmService 또는 VllmLlmService 가 바인딩됩니다.
 */
@RestController
public class LlmController {

    private static final Logger log = LoggerFactory.getLogger(LlmController.class);

    /** LlmService 인터페이스 타입으로 주입 - 구현체 변경 시 이 코드 수정 불필요 */
    @Resource(name = "llmService")
    private LlmService llmService;

    /**
     * 단순 완성.
     * <pre>
     * POST /api/llm/complete
     * { "prompt": "Spring 이란?" }
     * </pre>
     */
    @PostMapping("/api/llm/complete")
    public ResponseEntity<LlmResponse> complete(@Valid @RequestBody CompleteRequest request) {
        String result = llmService.complete(request.getPrompt());
        return ResponseEntity.ok(LlmResponse.success(result, llmService.getServiceName()));
    }

    /**
     * 채팅 완성.
     * <pre>
     * POST /api/llm/chat
     * { "systemPrompt": "Java 전문가입니다.", "userMessage": "스트림 API 설명해줘" }
     * </pre>
     */
    @PostMapping("/api/llm//chat")
    public ResponseEntity<LlmResponse> chat(@Valid @RequestBody ChatRequest request) {
        String result = llmService.chat(request.getSystemPrompt(), request.getUserMessage());
        return ResponseEntity.ok(LlmResponse.success(result, llmService.getServiceName()));
    }

    /** 현재 활성 서비스 구현체 확인 (운영/디버그용) */
    @GetMapping("/api/llm/service")
    public ResponseEntity<LlmResponse> getService() {
        return ResponseEntity.ok(LlmResponse.success(null, llmService.getServiceName()));
    }

}

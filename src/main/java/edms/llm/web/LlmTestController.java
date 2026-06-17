package edms.llm.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LlmTestController {

    /**
     * LLM 채팅 화면.
     * 브라우저에서 /llm/chat 으로 접근합니다.
     */
    @GetMapping("/llm/chat")
    public String chatPage() {
        return "llm/llmChat";
    }

}

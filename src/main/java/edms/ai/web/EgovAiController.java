package edms.ai.web;
import egovframework.sample.service.SampleVO;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class EgovAiController {


    @GetMapping("/api/v1/aiCon")
    public String list(@ModelAttribute SampleVO sampleVO, Model model) throws Exception {
       return "ai/aiCon";
    }

    // WebClient는 스프링 빈(Bean)으로 등록해서 쓰는 것이 좋으나,
    // 이해를 돕기 위해 인라인으로 구성했습니다.
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1") // 예시: 외부 AI 서비스 주소
            .defaultHeader("Authorization", "Bearer YOUR_AI_API_KEY")
            .build();

    /**
     * 1. 일반적인 비동기 단건 호출 (결과가 다 완성되면 한 번에 받기)
     */
    @PostMapping("/api/v1/ask")
    public Mono<String> askToAi(@RequestBody String prompt) {

        // AI API 규격에 맞게 보낼 JSON 데이터 바인딩 (Java 1.8 스타일)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o"); // 예시 모델
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);

        return webClient.post()
                .uri("/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class); // 결과를 비동기적으로 String으로 받아옴
    }

    /**
     * 2. 🔥 대망의 실시간 스트리밍 호출 (AI가 답변하는 대로 화면에 한 글자씩 전달)
     * produces에 "text/event-stream"을 설정하는 것이 핵심입니다.
     */
    @PostMapping(value = "/api/v1/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamFromAi(@RequestBody String prompt) {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("prompt", prompt);
        requestBody.put("stream", true); // AI 서버에 스트리밍으로 달라고 요청

        return webClient.post()
                .uri("/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class) // AI가 쪼개서 주는 데이터들을 Flux 스트림으로 받음
                // AI 서버가 주는 가공되지 않은 raw JSON 중에서 텍스트만 추출하고 싶다면
                // 아래처럼 .map()을 활용해 파싱 체인을 걸 수 있습니다.
                .map(rawJson -> {
                    // 실제 구현 시에는 Jackson 라이브러리 등으로 데이터만 파싱
                    return rawJson;
                })
                // 에러 발생 시 예외 처리 및 커넥션 끊김 방지
                .onErrorResume(e -> Flux.just(" [AI 통신 중 에러가 발생했습니다.] "));
    }
}
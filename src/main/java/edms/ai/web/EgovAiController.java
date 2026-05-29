package edms.ai.web;
import egovframework.sample.service.SampleVO;
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

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .defaultHeader("Authorization", "Bearer YOUR_AI_API_KEY")
            .build();

    @PostMapping("/api/v1/ask")
    public Mono<String> askToAi(@RequestBody String prompt) {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);

        return webClient.post()
                .uri("/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);
    }

    @PostMapping(value = "/api/v1/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamFromAi(@RequestBody String prompt) {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("prompt", prompt);
        requestBody.put("stream", true);

        return webClient.post()
                .uri("/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .map(rawJson -> {
                    return rawJson;
                })
                .onErrorResume(e -> Flux.just(" [AI ERROR] "));
    }
}
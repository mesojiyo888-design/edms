package edms.ai.web;

import edms.ai.constant.OllamaConstants;
import edms.ai.service.OllamaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class OllamaController {
    @Autowired
    public OllamaService ollamaService;

    @GetMapping("/ai/aiChat")
    public String chatPage() {
        return "ai/aiChat";
    }

    @GetMapping(value = "/ai/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<ServerSentEvent<String>> stream(
            @RequestParam String prompt,
            @RequestParam(defaultValue = "medium") String reasoning) {

        String reasoningLevel;
        switch (reasoning) {
            case "low":
                reasoningLevel = OllamaConstants.REASONING_LOW;
                break;
            case "high":
                reasoningLevel = OllamaConstants.REASONING_HIGH;
                break;
            default:
                reasoningLevel = OllamaConstants.REASONING_MEDIUM;
        }

        return ollamaService.streamChat(prompt, reasoningLevel)
                .map(token -> ServerSentEvent.<String>builder()
                        .data(token).build())
                .concatWith(Flux.just(
                        ServerSentEvent.<String>builder()
                                .event(OllamaConstants.SSE_EVENT_DONE)
                                .data(OllamaConstants.SSE_DATA_DONE)
                                .build()));
    }

    @PostMapping(value = "/ai/summarize", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<ServerSentEvent<String>> summarize(
            @RequestBody Map<String, String> body) {

        return ollamaService.streamSummarize(body.get("content"))
                .map(token -> ServerSentEvent.<String>builder()
                        .data(token).build())
                .concatWith(Flux.just(
                        ServerSentEvent.<String>builder()
                                .event(OllamaConstants.SSE_EVENT_DONE)
                                .data(OllamaConstants.SSE_DATA_DONE)
                                .build()));
    }

    @PostMapping(value = "/ai/recommend", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<ServerSentEvent<String>> recommend(@RequestBody Map<String, Object> body) {

        String currentDoc = (String) body.get("currentDoc");
        List<String> candidates = (List<String>) body.get("candidates");

        return ollamaService.streamRecommend(currentDoc, candidates)
                .map(token -> ServerSentEvent.<String>builder()
                        .data(token).build())
                .concatWith(Flux.just(
                        ServerSentEvent.<String>builder()
                                .event(OllamaConstants.SSE_EVENT_DONE)
                                .data(OllamaConstants.SSE_DATA_DONE)
                                .build()));
    }

}
package edms.ai.service.impl;
import edms.ai.constant.OllamaConstants;
import edms.ai.service.OllamaChatRequest;
import edms.ai.service.OllamaChatResponse;
import edms.ai.service.OllamaService;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("ollamaService")
@Profile("!local")
public class OllamaServiceImpl extends EgovAbstractServiceImpl implements OllamaService {

    private final WebClient ollamaWebClient;

    @Value("${ollama.model}")
    private String model;

    @Value("${ollama.api.chat}")
    private String apiChat;

    public OllamaServiceImpl(WebClient ollamaWebClient) {
        this.ollamaWebClient = ollamaWebClient;
        log.info("========= OllamaServiceImpl 로딩됨 =========");
    }

    @Override
    public Flux<String> streamChat(String userMessage, String reasoning) {
        String systemMsg = reasoning + OllamaConstants.SYSTEM_DEFAULT;

        List<OllamaChatRequest.Message> messages = new ArrayList<>();
        messages.add(new OllamaChatRequest.Message(OllamaConstants.ROLE_SYSTEM, systemMsg));
        messages.add(new OllamaChatRequest.Message(OllamaConstants.ROLE_USER, userMessage));

        return doStream(messages);
    }

    @Override
    public Flux<String> streamChatWithHistory(
            List<OllamaChatRequest.Message> history, String userMessage) {

        List<OllamaChatRequest.Message> messages = new ArrayList<>(history);
        messages.add(new OllamaChatRequest.Message(OllamaConstants.ROLE_USER, userMessage));

        return doStream(messages);
    }

    @Override
    public Flux<String> streamSummarize(String documentContent) {
        String userMsg = "다음 문서를 분석하여 아래 형식으로 요약해주세요.\n\n" +
                "## 핵심 내용\n" +
                "## 주요 결정사항\n" +
                "## 후속 조치\n" +
                "## 관련 키워드\n\n" +
                "문서:\n" + documentContent;

        List<OllamaChatRequest.Message> messages = new ArrayList<>();
        messages.add(new OllamaChatRequest.Message(OllamaConstants.ROLE_SYSTEM,
                OllamaConstants.REASONING_HIGH + OllamaConstants.SYSTEM_SUMMARIZE));
        messages.add(new OllamaChatRequest.Message(OllamaConstants.ROLE_USER, userMsg));

        return doStream(messages);
    }

    @Override
    public Flux<String> streamRecommend(String currentDoc, List<String> candidateTitles) {
        String candidates = String.join("\n", candidateTitles);
        String userMsg = "현재 문서: " + currentDoc + "\n\n" +
                "후보 문서 목록:\n" + candidates + "\n\n" +
                "현재 문서와 가장 관련성 높은 문서 3개를 추천하고 이유를 설명해주세요.";

        List<OllamaChatRequest.Message> messages = new ArrayList<>();
        messages.add(new OllamaChatRequest.Message(OllamaConstants.ROLE_SYSTEM,
                OllamaConstants.REASONING_MEDIUM + OllamaConstants.SYSTEM_RECOMMEND));
        messages.add(new OllamaChatRequest.Message(OllamaConstants.ROLE_USER, userMsg));

        return doStream(messages);
    }

    private Flux<String> doStream(List<OllamaChatRequest.Message> messages) {
        OllamaChatRequest request = new OllamaChatRequest();
        request.setModel(model);
        request.setMessages(messages);
        request.setStream(true);

        return ollamaWebClient.post()
                .uri(apiChat)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(OllamaChatResponse.class)
                .takeUntil(OllamaChatResponse::isDone)
                .filter(res -> !res.isDone() && res.getMessage() != null)
                .map(res -> res.getMessage().getContent())
                .filter(content -> content != null && !content.isEmpty())
                .onErrorResume(e -> {
                    log.error("Ollama 오류: {}", e.getMessage());
                    return Flux.just("\n[오류] AI 연결 실패: " + e.getMessage());
                });
    }
}
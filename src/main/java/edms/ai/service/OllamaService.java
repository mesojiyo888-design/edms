package edms.ai.service;

import reactor.core.publisher.Flux;

import java.util.List;

public interface OllamaService {

    /**
     * 단순 질의 스트리밍
     */
    Flux<String> streamChat(String userMessage, String reasoning);

    /**
     * 멀티턴 대화 스트리밍
     */
    Flux<String> streamChatWithHistory(List<OllamaChatRequest.Message> history, String userMessage);

    /**
     * 문서 요약 스트리밍
     */
    Flux<String> streamSummarize(String documentContent);

    /**
     * 관련 문서 추천 스트리밍
     */
    Flux<String> streamRecommend(String currentDoc, List<String> candidateTitles);
}
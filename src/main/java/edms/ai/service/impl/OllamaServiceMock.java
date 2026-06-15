package edms.ai.service.impl;

import edms.ai.service.OllamaChatRequest;
import edms.ai.service.OllamaService;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service("ollamaService")
@Profile("local")   // application-local.yml 에서만 활성화
@Primary            // OllamaServiceImpl 대신 우선 주입
public class OllamaServiceMock extends EgovAbstractServiceImpl implements OllamaService {
    public OllamaServiceMock() {
        log.info("========= OllamaServiceMock 로딩됨 =========");
    }

    private static final String MOCK_CHAT =
            "안녕하세요! 저는 문서 관리 AI 어시스턴트입니다.\n" +
                    "현재 Mock 모드로 동작 중입니다.\n" +
                    "실제 Ollama 연동 시 gpt-oss:20b 모델이 응답합니다.";

    private static final String MOCK_SUMMARIZE =
            "## 핵심 내용\n문서의 핵심 내용입니다. (Mock)\n\n" +
                    "## 주요 결정사항\n주요 결정사항입니다. (Mock)\n\n" +
                    "## 후속 조치\n후속 조치 내용입니다. (Mock)\n\n" +
                    "## 관련 키워드\n문서, 관리, 시스템, Mock";

    private static final String MOCK_RECOMMEND =
            "관련 문서 추천 결과입니다. (Mock)\n\n" +
                    "1. 후보 문서 1 - 현재 문서와 높은 관련성\n" +
                    "2. 후보 문서 2 - 키워드 일치\n" +
                    "3. 후보 문서 3 - 내용 유사";

    @Override
    public Flux<String> streamChat(String userMessage, String reasoning) {
        log.debug("Mock streamChat - prompt: {}, reasoning: {}", userMessage, reasoning);
        return tokenize(MOCK_CHAT);
    }

    @Override
    public Flux<String> streamChatWithHistory(
            List<OllamaChatRequest.Message> history, String userMessage) {
        log.debug("Mock streamChatWithHistory - prompt: {}", userMessage);
        return tokenize(MOCK_CHAT);
    }

    @Override
    public Flux<String> streamSummarize(String documentContent) {
        log.debug("Mock streamSummarize");
        return tokenize(MOCK_SUMMARIZE);
    }

    @Override
    public Flux<String> streamRecommend(String currentDoc, List<String> candidateTitles) {
        log.debug("Mock streamRecommend - currentDoc: {}", currentDoc);
        return tokenize(MOCK_RECOMMEND);
    }

    /**
     * 문자열을 한 글자씩 끊어서 스트리밍처럼 흘려보냄
     */
    private Flux<String> tokenize(String text) {
        String[] chars = text.split("");
        return Flux.fromArray(chars)
                .delayElements(Duration.ofMillis(30), Schedulers.parallel());
    }
}
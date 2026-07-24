package edms.batch.config;

import edms.batch.service.BatchServerOption;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * application.yml의 batch.api.* 설정 보유 컴포넌트.
 *
 * 이 프로젝트(edms)는 순수 Spring(egovframework) 기반이라 spring-boot 의존성이 없어
 * {@code @ConfigurationProperties}(spring-boot 전용)를 쓸 수 없다. 대신 다른 설정 클래스
 * ({@link edms.llm.config.LlmProperties}와 동일하게 {@code @Value}로 값을 매핑한다.
 *
 * 서버 목록은 YAML 리스트(map 배열) 대신, 이름/URL을 콤마(,)로 구분한 병렬 목록으로 관리한다
 * (순수 Spring PropertySourcesPlaceholderConfigurer는 Boot의 relaxed list-of-object 바인딩을
 *  지원하지 않기 때문).
 *
 *   batch:
 *     api:
 *       base-url: http://localhost:8081
 *       server-names: batch-server-01,batch-server-02
 *       server-urls: http://10.0.0.11:8081,http://10.0.0.12:8081
 */
@Getter
@Component
public class BatchServerProperties {

    @Value("${batch.api.base-url}")
    private String baseUrl;

    @Value("${batch.api.server-names:}")
    private String serverNamesCsv;

    @Value("${batch.api.server-urls:}")
    private String serverUrlsCsv;

    /** 이름/URL 목록을 같은 순서로 짝지어 반환. 둘 중 하나라도 비어있으면 빈 목록. */
    public List<BatchServerOption> getServers() {
        List<BatchServerOption> result = new ArrayList<>();
        if (isBlank(serverNamesCsv) || isBlank(serverUrlsCsv)) {
            return result;
        }

        String[] names = serverNamesCsv.split(",");
        String[] urls  = serverUrlsCsv.split(",");
        int len = Math.min(names.length, urls.length);

        for (int i = 0; i < len; i++) {
            String name = names[i].trim();
            String url  = urls[i].trim();
            if (name.isEmpty() || url.isEmpty()) continue;

            BatchServerOption opt = new BatchServerOption();
            opt.setName(name);
            opt.setUrl(url);
            result.add(opt);
        }
        return result;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

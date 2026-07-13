package egovframework.config;

import edms.com.service.CommonJobConfigService;
import edms.com.service.impl.CommonJobConfigServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CacheRefreshScheduler {

    private static final Logger log = LoggerFactory.getLogger(CacheRefreshScheduler.class);

    private final CommonJobConfigService commonJobConfigService;

    public CacheRefreshScheduler(CommonJobConfigService commonJobConfigService) {
        log.debug("@@@ CacheRefreshScheduler.CacheRefreshScheduler");

        this.commonJobConfigService = commonJobConfigService;
    }

    /**
     * 주기적으로 캐시를 비웁니다.
     * 예: fixedDelay = 3600000 (1시간마다)
     * 아래 예시는 테스트를 위해 10초(10000ms)마다 작동하도록 설정했습니다.
     */
    @Scheduled(fixedDelay = 10000)
    public void refreshCommonJobConfig() {
        log.debug("@@@ CacheRefreshScheduler.refreshCommonJobConfig");

        commonJobConfigService.clearAllJobConfigCache();

        // (선택사항) 캐시를 비운 직후, 자주 쓰는 공통코드를 미리 로딩(Warm-up)하고 싶다면 아래처럼 호출해 둡니다.
        // commonCodeService.getCommonCodes("USER_ROLE");
    }
}

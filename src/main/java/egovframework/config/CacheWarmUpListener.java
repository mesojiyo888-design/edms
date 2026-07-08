package egovframework.config;

import edms.com.service.CommonJobConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * 초기 서버 기동 후 캐시에 데이터 올리기 위한 클래스
 */
@Component
public class CacheWarmUpListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(CacheWarmUpListener.class);

    private final CommonJobConfigService commonJobConfigService;

    // 생성자 주입
    public CacheWarmUpListener(CommonJobConfigService commonJobConfigService) {
        log.debug("@@@ CacheWarmUpListener INIT");

        this.commonJobConfigService = commonJobConfigService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.debug("@@@ CacheWarmUpListener.onApplicationEvent");

        // Root 웹 컨텍스트가 초기화될 때만 실행 (서블릿 컨텍스트 중복 실행 방지)
        if (event.getApplicationContext().getParent() == null) {
            log.debug("@@@ CacheWarmUpListener - Spring 순수 환경: 서버 기동 후 캐시 미리 로딩 시작");

            commonJobConfigService.getJobConfig();

            log.debug("@@@ CacheWarmUpListener - jobConfig 캐시 로딩 완료");
        }
    }
}
package egovframework.config;

import egovframework.filter.AccessLogFilter;
import org.springframework.context.annotation.*;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(
        basePackages = {"edms", "egovframework"},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ANNOTATION,
                        classes = org.springframework.stereotype.Controller.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ANNOTATION,
                        classes = org.springframework.web.bind.annotation.ControllerAdvice.class
                )
        }
)
@Import({
        EgovConfigCommon.class,      // 공통 빈
        EgovConfigDatasource.class,  // DataSource, Connection Pool
        EgovConfigIdGeneration.class,// ID 생성
        EgovConfigMapper.class,      // MyBatis
        EgovConfigProperties.class,  // 프로퍼티
        EgovConfigTransaction.class, // 트랜잭션
        EgovConfigValidation.class,  // 검증
        SsoIntegratedConfig.class,   // SSO 통합 설정
        AccessLogFilter.class,       // AOP 설정(접근 로그)
        P6SpyConfig.class           // P6Spy 설정(SQL 로깅)
})
public class EgovConfigAppRoot {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EgovConfigAppRoot.class);

    static {
        log.info("EgovConfigAppRoot initialized");
    }
}
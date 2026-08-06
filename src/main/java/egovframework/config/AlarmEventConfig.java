package egovframework.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AlarmEventConfig {

    private static final Logger log = LoggerFactory.getLogger(AlarmEventConfig.class);

    // @Async("alarmEventAsyncExecutor") 에서 사용할 전용 스레드 풀 빈 등록
    @Bean(name = "alarmEventAsyncExecutor")
    public Executor alarmEventAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        
        // OOM 방어 로직 큐 크기 제한
        executor.setQueueCapacity(10000);

        executor.setThreadNamePrefix("ALARM-EVENT-Thread-");

        // 거부 정책 설정 큐와 스레드가 모두 가득 찼을 때 어떻게 할 것인가?
        // CallerRunsPolicy: 이벤트를 발행한 메인 스레드(톰캣 스레드)가 직접 리스너 로직을 실행하게 합니다.
        // 이를 통해 자연스럽게 메인 스레드에 제동(Backpressure)이 걸려 유입 속도가 조절됩니다.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize(); // 초기화 필수
        return executor;
    }

    // 기본 @Async (이름 없이 붙일 때) 사용될 기본 Executor (선택 사항)
    @Bean(name = "taskExecutor") // 관례상 taskExecutor 이름으로 등록하면 기본값이 됨
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setThreadNamePrefix("Default-Async-");
        executor.initialize();
        return executor;
    }

    @Bean
    public AsyncUncaughtExceptionHandler alarmEventAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> log.error("[ALARM LOG] alarmEventAsyncUncaughtExceptionHandler Async Event Error: {}", method.getName(), ex);
    }

}

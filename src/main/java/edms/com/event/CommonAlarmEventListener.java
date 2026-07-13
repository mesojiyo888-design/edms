package edms.com.event;

import edms.com.vo.CommonAlarmEventVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class CommonAlarmEventListener {

    private static final Logger log = LoggerFactory.getLogger(CommonAlarmEventListener.class);

    // 동기 처리 (기본): 발행 스레드에서 바로 실행됨. 발행자가 기다림.
//    @EventListener
//    public void handleSendAlarmEventSync(CommonAlarmEventVo event) {
//        log.info("[AlARM_LOG] CommonAlarmEventListener.handleSendAlarmEventSync - Sync Listener - 알림발송 START : {} (Thread: {})", event.getDocId(), Thread.currentThread().getName());
//
//        // TODO 이벤트 수신 발신
//        try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
//
//        log.info("[AlARM_LOG] CommonAlarmEventListener.handleSendAlarmEventSync - Sync Listener - 알림발송 END : {}", event.getDocId());
//    }

    // 비동기 처리: 별도 스레드 풀에서 실행. 발행자는 바로 리턴.
    // @EnableAsync 설정 클래스에 반드시 TaskExecutor 빈이 등록되어 있어야 함.
    @Async("alarmEventAsyncExecutor") // 사용할 Executor 빈 이름 지정 (미지정 시 기본 default executor 사용)
    @EventListener
    public void handleSendAlarmEventAsync(CommonAlarmEventVo event) {
        log.info("[AlARM_LOG] CommonAlarmEventListener.handleSendAlarmEventAsync - Async Listener - 알림발송 START : {} (Thread: {})", event.getDocId(), Thread.currentThread().getName());

        // TODO 이벤트 수신해 발신 처리
        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        log.info("[AlARM_LOG] CommonAlarmEventListener.handleSendAlarmEventAsync - Async Listener - 알림발송 END : {}", event.getDocId());
    }

    // 조건부 처리 (SpEL 사용) 샘플
    @Async("alarmEventAsyncExecutor") // 사용할 Executor 빈 이름 지정 (미지정 시 기본 default executor 사용)
    @EventListener(condition = "#event.alarmType == 'DOCUMENT'")
    public void handleSendAlarmEventCondition(CommonAlarmEventVo event) {
        log.info("[AlARM_LOG] CommonAlarmEventListener.handleSendAlarmEventCondition - Async Listener - 알림발송 START : {} (Thread: {})", event.getDocId(), Thread.currentThread().getName());

        // TODO 이벤트 수신해 발신 처리

        log.info("[AlARM_LOG] CommonAlarmEventListener.handleSendAlarmEventCondition - Async Listener - 알림발송 END : {}", event.getDocId());
    }
}

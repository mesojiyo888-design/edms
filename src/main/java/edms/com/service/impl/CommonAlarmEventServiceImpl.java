package edms.com.service.impl;

import edms.com.service.CommonAlarmEventService;
import edms.com.vo.CommonAlarmEventVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service(value = "commonAlarmEventService")
public class CommonAlarmEventServiceImpl implements CommonAlarmEventService {

    private static final Logger log = LoggerFactory.getLogger(CommonAlarmEventServiceImpl.class);

     private final ApplicationEventPublisher eventPublisher;

     public CommonAlarmEventServiceImpl(ApplicationEventPublisher eventPublisher) {
         this.eventPublisher = eventPublisher;
     }

    /**
     * 알림 전송
     * @param vo
     */
    @Override
    public void sendAlarm(CommonAlarmEventVo vo) {
        log.debug("[ALARM_LOG] CommonAlarmEventServiceImpl.sendAlarm - VO : {}", vo);

        // TODO 알림 테이블 저장
        // 알림전송 이벤트 발행
        eventPublisher.publishEvent(vo);

        log.debug("[ALARM_LOG] CommonAlarmEventServiceImpl.sendAlarm - EVENT 발생 완료");
    }
}

package edms.common.service;

import edms.common.vo.CommonAlarmEventVo;

public interface CommonAlarmEventService {

    public void sendAlarm(CommonAlarmEventVo vo);

}

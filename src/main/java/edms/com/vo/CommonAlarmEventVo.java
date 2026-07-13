package edms.com.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CommonAlarmEventVo {
    private final String docId;
    private final String alarmType;
    private final String alarmMessage;

    public CommonAlarmEventVo(String docId, String alarmType, String alarmMessage) {
        this.docId = docId;
        this.alarmType = alarmType;
        this.alarmMessage = alarmMessage;
    }
}

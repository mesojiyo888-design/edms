package edms.batch.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용여부 일괄 변경(toggle-bulk) 요청 항목.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatchToggleItem {
    private String jobName;
    private String useYn;
}

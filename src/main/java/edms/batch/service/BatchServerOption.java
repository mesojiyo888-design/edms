package edms.batch.service;

import lombok.Getter;
import lombok.Setter;

/**
 * application.yml의 batch.api.servers 목록 항목 (다중 배치서버 선택용).
 */
@Getter
@Setter
public class BatchServerOption {
    private String name;
    private String url;
}

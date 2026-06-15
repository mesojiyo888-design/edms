package edms.ai.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OllamaChatResponse {

    private String model;
    private Message message;
    private boolean done;

    @JsonProperty("total_duration")
    private Long totalDuration;

    @JsonProperty("eval_count")
    private Integer evalCount;

    @Getter
    @Setter
    public static class Message {
        private String role;
        private String content;
    }
}
package edms.ai.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OllamaChatRequest {

    private String model;
    private List<Message> messages;
    private boolean stream = true;
    private Options options;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }

    @Getter
    @Setter
    public static class Options {
        private float temperature = 0.7f;

        @JsonProperty("num_predict")
        private int numPredict = 2048;
    }
}
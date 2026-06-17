package edms.llm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** LLM 응답 공통 래퍼 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LlmResponse {
    private String result;
    private String provider;
    private String errorMessage;

    public static LlmResponse success(String result, String provider) {
        return LlmResponse.builder().result(result).provider(provider).build();
    }

    public static LlmResponse error(String errorMessage, String provider) {
        return LlmResponse.builder().errorMessage(errorMessage).provider(provider).build();
    }
}

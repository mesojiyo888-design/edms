package edms.llm.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 채팅 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class ChatRequest {
    @Size(max = 1000, message = "systemPrompt 는 1000자 이하여야 합니다.")
    private String systemPrompt;

    @NotBlank(message = "userMessage 는 필수입니다.")
    @Size(max = 4000, message = "userMessage 는 4000자 이하여야 합니다.")
    private String userMessage;
}

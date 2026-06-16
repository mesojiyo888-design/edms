package edms.llm.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 단순 완성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class CompleteRequest {

    @NotBlank(message = "prompt 는 필수입니다.")
    @Size(max = 4000, message = "prompt 는 4000자 이하여야 합니다.")
    private String prompt;
}
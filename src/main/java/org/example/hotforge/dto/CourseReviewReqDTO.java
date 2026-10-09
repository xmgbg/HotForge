package org.example.hotforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseReviewReqDTO {
    @NotBlank
    @Pattern(regexp = "APPROVED|REJECTED", message = "审核结果只能为 APPROVED 或 REJECTED")
    private String decision;

    @Size(max = 500)
    private String comment;
}

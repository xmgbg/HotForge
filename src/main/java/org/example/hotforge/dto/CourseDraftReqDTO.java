package org.example.hotforge.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseDraftReqDTO {
    @NotBlank
    @Size(max = 100)
    private String name;

    private String description;

    @Size(max = 500)
    private String coverImage;

    @NotNull
    @Min(1)
    private Integer duration;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer isVipOnly;
}

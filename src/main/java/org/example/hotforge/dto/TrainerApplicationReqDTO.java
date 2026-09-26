package org.example.hotforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class TrainerApplicationReqDTO {
    @NotBlank
    @Size(max = 50)
    private String realName;

    @NotEmpty
    @Size(max = 5)
    private List<@NotBlank String> certificationPhotos;

    @Size(max = 500)
    private String bio;
}

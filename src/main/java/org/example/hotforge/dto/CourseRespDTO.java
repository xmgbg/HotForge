package org.example.hotforge.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseRespDTO {
    private Long id;
    private Long publisherId;
    private String name;
    private String type;
    private String description;
    private String coverImage;
    private Integer duration;
    private Integer isVipOnly;
}

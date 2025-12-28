package ens.edupath.module.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer credits;
    private Boolean active;
    private EnrollmentPeriodResponse enrollmentPeriod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



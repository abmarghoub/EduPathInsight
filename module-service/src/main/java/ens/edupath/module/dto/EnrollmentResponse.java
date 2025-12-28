package ens.edupath.module.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {
    private Long id;
    private Long moduleId;
    private String moduleCode;
    private String moduleName;
    private String studentId;
    private String studentUsername;
    private String studentEmail;
    private String status;
    private LocalDateTime enrolledAt;
    private LocalDateTime approvedAt;
}



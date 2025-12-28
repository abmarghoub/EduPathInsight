package ens.edupath.note.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {
    private Long id;
    private String studentId;
    private Long moduleId;
    private String type;
    private String title;
    private String message;
    private String status;
    private Double thresholdValue;
    private Double actualValue;
    private LocalDateTime createdAt;
    private LocalDateTime acknowledgedAt;
}



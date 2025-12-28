package ens.edupath.note.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KPIResponse {
    private Long id;
    private String studentId;
    private Long moduleId;
    private Double averageScore;
    private Double totalScore;
    private Double totalMaxScore;
    private Integer numberOfEvaluations;
    private Integer passingCount;
    private Integer failingCount;
    private Double highestScore;
    private Double lowestScore;
    private LocalDateTime lastCalculatedAt;
}



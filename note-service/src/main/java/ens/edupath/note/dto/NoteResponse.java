package ens.edupath.note.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponse {
    private Long id;
    private String studentId;
    private String studentUsername;
    private Long moduleId;
    private String moduleCode;
    private String moduleName;
    private String evaluationType;
    private String evaluationTitle;
    private Double score;
    private Double maxScore;
    private Double percentage;
    private Boolean passing;
    private String comments;
    private LocalDateTime evaluationDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



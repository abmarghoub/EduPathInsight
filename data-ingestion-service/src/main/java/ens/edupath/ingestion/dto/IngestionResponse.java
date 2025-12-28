package ens.edupath.ingestion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngestionResponse {
    private Long logId;
    private String fileName;
    private String entityType;
    private String status;
    private Integer totalRecords;
    private Integer successfulRecords;
    private Integer failedRecords;
    private String message;
    private LocalDateTime processedAt;
}



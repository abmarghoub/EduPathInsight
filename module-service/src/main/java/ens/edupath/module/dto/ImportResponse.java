package ens.edupath.module.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResponse {
    private Long logId;
    private String fileName;
    private String status;
    private Integer totalRecords;
    private Integer successfulRecords;
    private Integer failedRecords;
    private String message;
    private LocalDateTime processedAt;
}



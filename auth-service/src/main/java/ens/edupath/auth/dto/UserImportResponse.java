package ens.edupath.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserImportResponse {
    private boolean success;
    private String message;
    private String status;
    private int importedCount;
    private int failedCount;
    private List<String> errors = new ArrayList<>();
}



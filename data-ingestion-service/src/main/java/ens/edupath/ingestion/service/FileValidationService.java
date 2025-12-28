package ens.edupath.ingestion.service;

import ens.edupath.ingestion.dto.ValidationResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
public class FileValidationService {

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "text/csv",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("csv", "xlsx", "xls");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public ValidationResult validateFile(MultipartFile file, String entityType) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);

        if (file == null || file.isEmpty()) {
            result.addError("Le fichier est vide ou manquant");
            return result;
        }

        // Vérifier la taille
        if (file.getSize() > MAX_FILE_SIZE) {
            result.addError("La taille du fichier dépasse la limite de 10MB");
        }

        // Vérifier l'extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            result.addError("Le nom du fichier est invalide");
            return result;
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            result.addError("Le type de fichier n'est pas supporté. Types autorisés: CSV, XLS, XLSX");
        }

        // Vérifier le type MIME (optionnel, peut être imprécis)
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_TYPES.contains(contentType)) {
            result.addWarning("Le type MIME du fichier est suspect: " + contentType);
        }

        // Vérifier le type d'entité
        List<String> validEntityTypes = Arrays.asList("User", "Module", "Note", "Presence", "Activity");
        if (!validEntityTypes.contains(entityType)) {
            result.addError("Type d'entité invalide. Types valides: " + String.join(", ", validEntityTypes));
        }

        return result;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}



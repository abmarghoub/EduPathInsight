package ens.edupath.ingestion.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "note-service", path = "/api/notes/admin")
public interface NoteServiceClient {

    @PostMapping("/notes")
    ResponseEntity<?> createNote(@RequestBody Map<String, Object> noteRequest);
}



package ens.edupath.ingestion.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "activities-service", url = "${services.activities-service:http://localhost:5000}")
public interface ActivitiesServiceClient {

    @PostMapping("/api/activities/admin/presences")
    ResponseEntity<?> createPresence(@RequestBody Map<String, Object> presenceRequest);

    @PostMapping("/api/activities/admin/activities")
    ResponseEntity<?> createActivity(@RequestBody Map<String, Object> activityRequest);
}



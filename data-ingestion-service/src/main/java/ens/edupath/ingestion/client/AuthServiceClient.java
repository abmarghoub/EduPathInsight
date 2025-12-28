package ens.edupath.ingestion.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "auth-service", path = "/api/auth/admin")
public interface AuthServiceClient {

    @PostMapping("/create-user")
    ResponseEntity<Object> createUser(@RequestBody Map<String, Object> userRequest);
}



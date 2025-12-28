package ens.edupath.note.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(name = "module-service", path = "/api/modules/admin")
public interface ModuleServiceClient {

    @GetMapping("/modules/{moduleId}")
    Map<String, Object> getModuleById(@PathVariable("moduleId") Long moduleId);

    @GetMapping("/modules/{moduleId}/enrollments")
    List<Map<String, Object>> getModuleEnrollments(@PathVariable("moduleId") Long moduleId);
}



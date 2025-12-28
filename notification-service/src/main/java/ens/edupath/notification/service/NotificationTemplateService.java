package ens.edupath.notification.service;

import ens.edupath.notification.entity.Notification;
import ens.edupath.notification.entity.NotificationTemplate;
import ens.edupath.notification.repository.NotificationTemplateRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationTemplateService {

    private final NotificationTemplateRepository templateRepository;
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    public NotificationTemplateService(NotificationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public Optional<NotificationTemplate> getTemplate(String notificationType, Notification.NotificationChannel channel) {
        List<NotificationTemplate> templates = templateRepository.findByNotificationTypeAndChannel(notificationType, channel);
        return templates.stream()
                .filter(t -> t.getActive() != null && t.getActive())
                .findFirst();
    }

    public String renderTemplate(String templateBody, Map<String, String> variables) {
        if (templateBody == null) {
            return "";
        }

        String result = templateBody;
        Matcher matcher = VARIABLE_PATTERN.matcher(templateBody);

        while (matcher.find()) {
            String variableName = matcher.group(1);
            String value = variables.getOrDefault(variableName, "");
            result = result.replace("{{" + variableName + "}}", value);
        }

        return result;
    }

    public Map<String, String> extractVariables(String templateBody) {
        Map<String, String> variables = new HashMap<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(templateBody);

        while (matcher.find()) {
            String variableName = matcher.group(1);
            variables.put(variableName, "");
        }

        return variables;
    }
}


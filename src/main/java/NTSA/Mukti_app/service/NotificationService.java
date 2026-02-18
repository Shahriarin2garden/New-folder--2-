package NTSA.Mukti_app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void notifyUser(String userId, String message, String type) {
        // Send to specific user topic: /topic/notifications/{userId}
        // This avoids dependency on Spring Security Principal for convertAndSendToUser
        Map<String, String> payload = Map.of(
                "message", message,
                "type", type,
                "timestamp", String.valueOf(System.currentTimeMillis()));
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, payload);
    }

    public void notifyAll(String message, String type) {
        // Broadcast to public topic: /topic/notifications
        Map<String, String> payload = Map.of(
                "message", message,
                "type", type,
                "timestamp", String.valueOf(System.currentTimeMillis()));
        messagingTemplate.convertAndSend("/topic/notifications", payload);
    }
}

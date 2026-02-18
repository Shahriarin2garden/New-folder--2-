package NTSA.Mukti_app.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final Map<Long, CopyOnWriteArraySet<WebSocketSession>> chatRooms = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("WebSocket connection established: {}", session.getId());

        // Send welcome message
        Map<String, String> welcome = Map.of(
                "type", "system",
                "message", "Connected to chat"
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(welcome)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.debug("Received message from {}: {}", session.getId(), payload);

        try {
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            String type = (String) data.get("type");

            if (type == null) {
                sendError(session, "Message type is required");
                return;
            }

            if (!data.containsKey("activityId")) {
                sendError(session, "activityId is required");
                return;
            }

            Long activityId;
            try {
                activityId = Long.parseLong(data.get("activityId").toString());
            } catch (NumberFormatException e) {
                sendError(session, "Invalid activityId format");
                return;
            }

            switch (type) {
                case "join":
                    handleJoin(activityId, session, data);
                    break;
                case "message":
                    handleMessage(activityId, session, data);
                    break;
                case "leave":
                    handleLeave(activityId, session);
                    break;
                case "ping":
                    handlePing(session);
                    break;
                default:
                    sendError(session, "Unknown message type: " + type);
                    break;
            }
        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage(), e);
            sendError(session, "Invalid message format");
        }
    }

    private void handleJoin(Long activityId, WebSocketSession session, Map<String, Object> data) {
        chatRooms.computeIfAbsent(activityId, k -> new CopyOnWriteArraySet<>()).add(session);
        logger.info("Session {} joined chat room {}", session.getId(), activityId);

        // Notify user they joined
        try {
            Map<String, Object> response = Map.of(
                    "type", "system",
                    "message", "Joined chat room",
                    "activityId", activityId,
                    "timestamp", System.currentTimeMillis()
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));

            // Notify others in the room
            notifyUserJoined(activityId, session, data);
        } catch (Exception e) {
            logger.error("Error sending join confirmation: {}", e.getMessage(), e);
        }
    }

    private void notifyUserJoined(Long activityId, WebSocketSession session, Map<String, Object> data) {
        CopyOnWriteArraySet<WebSocketSession> room = chatRooms.get(activityId);
        if (room == null || room.isEmpty()) return;

        String username = (String) data.getOrDefault("username", "Anonymous");

        Map<String, Object> notification = Map.of(
                "type", "notification",
                "message", username + " joined the chat",
                "activityId", activityId,
                "timestamp", System.currentTimeMillis()
        );

        try {
            String jsonMessage = objectMapper.writeValueAsString(notification);

            for (WebSocketSession s : room) {
                if (s.isOpen() && !s.equals(session)) {
                    s.sendMessage(new TextMessage(jsonMessage));
                }
            }
        } catch (Exception e) {
            logger.error("Error notifying users: {}", e.getMessage(), e);
        }
    }

    private void handleMessage(Long activityId, WebSocketSession session, Map<String, Object> data) {
        CopyOnWriteArraySet<WebSocketSession> room = chatRooms.get(activityId);
        if (room == null) {
            sendError(session, "You are not in this chat room");
            return;
        }

        // Validate message
        if (!data.containsKey("message") || ((String) data.get("message")).trim().isEmpty()) {
            sendError(session, "Message cannot be empty");
            return;
        }

        // Prepare broadcast message
        Map<String, Object> broadcastMessage = Map.of(
                "type", "message",
                "activityId", activityId,
                "message", data.get("message"),
                "sender", data.getOrDefault("userType", "unknown"),
                "username", data.getOrDefault("username", "Anonymous"),
                "timestamp", data.getOrDefault("timestamp", System.currentTimeMillis()),
                "sessionId", session.getId()
        );

        try {
            String jsonMessage = objectMapper.writeValueAsString(broadcastMessage);

            // Broadcast to all sessions in the room except sender
            for (WebSocketSession s : room) {
                if (s.isOpen()) {
                    // Include sender for message delivery confirmation
                    // Remove !s.equals(session) if you want sender to see their own messages
                    if (!s.equals(session)) {
                        s.sendMessage(new TextMessage(jsonMessage));
                    }
                }
            }

            logger.debug("Message broadcast in room {} by {}", activityId, session.getId());
        } catch (Exception e) {
            logger.error("Error broadcasting message: {}", e.getMessage(), e);
        }
    }

    private void handleLeave(Long activityId, WebSocketSession session) {
        CopyOnWriteArraySet<WebSocketSession> room = chatRooms.get(activityId);
        if (room != null) {
            room.remove(session);
            logger.info("Session {} left chat room {}", session.getId(), activityId);

            if (room.isEmpty()) {
                chatRooms.remove(activityId);
                logger.info("Chat room {} removed (empty)", activityId);
            }
        }

        // Notify user they left
        try {
            Map<String, Object> response = Map.of(
                    "type", "system",
                    "message", "Left chat room",
                    "activityId", activityId,
                    "timestamp", System.currentTimeMillis()
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (Exception e) {
            logger.error("Error sending leave confirmation: {}", e.getMessage(), e);
        }
    }

    private void handlePing(WebSocketSession session) {
        try {
            Map<String, String> pong = Map.of(
                    "type", "pong",
                    "timestamp", String.valueOf(System.currentTimeMillis())
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(pong)));
            logger.debug("Ping-pong with session {}", session.getId());
        } catch (Exception e) {
            logger.error("Error sending pong: {}", e.getMessage(), e);
        }
    }

    private void sendError(WebSocketSession session, String errorMessage) {
        try {
            Map<String, Object> error = Map.of(
                    "type", "error",
                    "message", errorMessage,
                    "timestamp", System.currentTimeMillis()
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
            logger.warn("Sent error to session {}: {}", session.getId(), errorMessage);
        } catch (Exception e) {
            logger.error("Error sending error message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        logger.info("WebSocket connection closed: {}, status: {}", session.getId(), status);

        // Remove session from all rooms
        for (Map.Entry<Long, CopyOnWriteArraySet<WebSocketSession>> entry : chatRooms.entrySet()) {
            Long activityId = entry.getKey();
            CopyOnWriteArraySet<WebSocketSession> room = entry.getValue();

            if (room.remove(session)) {
                logger.info("Removed session {} from room {} due to connection close", session.getId(), activityId);

                if (room.isEmpty()) {
                    chatRooms.remove(activityId);
                    logger.info("Chat room {} removed (empty after connection close)", activityId);
                }
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Transport error for session {}: {}", session.getId(), exception.getMessage(), exception);
        session.close();
    }

    // Utility method to get room statistics (optional, for monitoring)
    public Map<String, Object> getRoomStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalRooms", chatRooms.size());

        Map<Long, Integer> roomSizes = new ConcurrentHashMap<>();
        for (Map.Entry<Long, CopyOnWriteArraySet<WebSocketSession>> entry : chatRooms.entrySet()) {
            roomSizes.put(entry.getKey(), entry.getValue().size());
        }
        stats.put("roomSizes", roomSizes);

        return stats;
    }
}
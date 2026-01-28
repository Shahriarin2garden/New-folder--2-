package NTSA.Mukti_app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long foodPostId; // Reference to the food post

    @Column(nullable = false)
    private String senderPhone;

    @Column(nullable = false)
    private String senderName;

    @Column(nullable = false)
    private String receiverPhone;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private boolean readStatus = false;

    @Column(nullable = false)
    private String messageType = "TEXT"; // TEXT, SYSTEM

    @Column(name = "is_request", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isRequest = false; // Flag for food requests

    // Constructors
    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatMessage(Long foodPostId, String senderPhone, String senderName,
            String receiverPhone, String message) {
        this.foodPostId = foodPostId;
        this.senderPhone = senderPhone;
        this.senderName = senderName;
        this.receiverPhone = receiverPhone;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ChatMessage(Long foodPostId, String senderPhone, String senderName,
            String receiverPhone, String message, boolean isRequest) {
        this(foodPostId, senderPhone, senderName, receiverPhone, message);
        this.isRequest = isRequest;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFoodPostId() {
        return foodPostId;
    }

    public void setFoodPostId(Long foodPostId) {
        this.foodPostId = foodPostId;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isReadStatus() {
        return readStatus;
    }

    public void setReadStatus(boolean readStatus) {
        this.readStatus = readStatus;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public boolean isRequest() {
        return isRequest;
    }

    public void setRequest(boolean request) {
        isRequest = request;
    }
}

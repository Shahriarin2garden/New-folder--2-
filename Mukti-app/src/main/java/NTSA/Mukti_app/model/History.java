package NTSA.Mukti_app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userPhone;
    private String foodName;
    private String location;
    private String role; // DONOR or RECEIVER
    private String status; // Available, Processing, Received, Cancelled
    private String otherPartyName;
    private String otherPartyPhone;
    private LocalDateTime activityTime = LocalDateTime.now();

    // Constructors, getters, and setters
    public History() {}

    public History(String userPhone, String foodName, String location, String role, String status, String otherPartyName, String otherPartyPhone) {
        this.userPhone = userPhone;
        this.foodName = foodName;
        this.location = location;
        this.role = role;
        this.status = status;
        this.otherPartyName = otherPartyName;
        this.otherPartyPhone = otherPartyPhone;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOtherPartyName() {
        return otherPartyName;
    }

    public void setOtherPartyName(String otherPartyName) {
        this.otherPartyName = otherPartyName;
    }

    public String getOtherPartyPhone() {
        return otherPartyPhone;
    }

    public void setOtherPartyPhone(String otherPartyPhone) {
        this.otherPartyPhone = otherPartyPhone;
    }

    public LocalDateTime getActivityTime() {
        return activityTime;
    }

    public void setActivityTime(LocalDateTime activityTime) {
        this.activityTime = activityTime;
    }
}
package com.example.notificationservice.event;

import java.time.LocalDateTime;
import java.util.Objects;

public class UserEvent {
    private String eventType;
    private Long userId;
    private String userEmail;
    private String userName;
    private LocalDateTime timestamp;

    // Конструкторы
    public UserEvent() {}

    public UserEvent(String eventType, Long userId, String userEmail, String userName, LocalDateTime timestamp) {
        this.eventType = eventType;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.timestamp = timestamp;
    }

    // Геттеры и сеттеры
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEvent userEvent = (UserEvent) o;
        return Objects.equals(eventType, userEvent.eventType) &&
                Objects.equals(userId, userEvent.userId) &&
                Objects.equals(userEmail, userEvent.userEmail) &&
                Objects.equals(userName, userEvent.userName) &&
                Objects.equals(timestamp, userEvent.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, userId, userEmail, userName, timestamp);
    }

    @Override
    public String toString() {
        return "UserEvent{" +
                "eventType='" + eventType + '\'' +
                ", userId=" + userId +
                ", userEmail='" + userEmail + '\'' +
                ", userName='" + userName + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
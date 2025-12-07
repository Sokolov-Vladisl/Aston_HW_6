package com.example.notificationservice.consumer;

import com.example.notificationservice.event.UserEvent;
import com.example.notificationservice.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);

    private final EmailService emailService;

    public UserEventConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "user-events", groupId = "notification-group")
    public void handleUserEvent(UserEvent event) {
        log.info("Received user event: {}", event);

        String email = event.getUserEmail();
        String userName = event.getUserName();

        if ("USER_CREATED".equals(event.getEventType())) {
            String creationSubject = "Добро пожаловать!";
            String creationText = String.format(
                    "Здравствуйте, %s! Ваш аккаунт был создан.",
                    userName
            );
            emailService.sendEmail(email, creationSubject, creationText);
        } else if ("USER_DELETED".equals(event.getEventType())) {
            String deletionSubject = "Аккаунт удален";
            String deletionText = String.format(
                    "Здравствуйте, %s! Ваш аккаунт был удален.",
                    userName
            );
            emailService.sendEmail(email, deletionSubject, deletionText);
        }

        log.info("Email sent to: {}", email);
    }
}
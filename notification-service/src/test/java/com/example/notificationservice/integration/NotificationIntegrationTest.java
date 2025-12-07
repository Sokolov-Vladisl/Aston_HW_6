package com.example.notificationservice.integration;

import com.example.notificationservice.event.UserEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = {"user-events"})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.mail.host=localhost",
        "spring.mail.port=3025"
})
class NotificationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Test
    void testSendEmailViaAPI() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/notifications/email?to=test@example.com&subject=Test&text=Hello",
                null,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testKafkaUserCreatedEvent() {
        UserEvent event = new UserEvent();
        event.setEventType("USER_CREATED");
        event.setUserId(1L);
        event.setUserEmail("JUST_PLACE_HERE@gmail.com");
        event.setUserName("Test User");
        event.setTimestamp(LocalDateTime.now());


        kafkaTemplate.send("user-events", event);
    }
}
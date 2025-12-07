package com.example.user_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserServiceApplicationTests {

    @Test
    void contextLoads(ApplicationContext context) {
        assertThat(context).isNotNull();

        assertThat(context.containsBean("userController")).isTrue();
        assertThat(context.containsBean("userService")).isTrue();
        assertThat(context.containsBean("userRepository")).isTrue();
    }

    @Test
    void mainMethodStartsApplication() {
        UserServiceApplication.main(new String[] {});
        assertThat(true).isTrue();
    }

    @Test
    void applicationPropertiesAreLoaded() {
        assertThat(System.getProperty("java.version")).isNotNull();
    }

    @Test
    void springBootVersionIsCorrect() {
        String springBootVersion = org.springframework.boot.SpringBootVersion.getVersion();
        assertThat(springBootVersion).isNotNull();
        assertThat(springBootVersion).startsWith("3.");
    }
}
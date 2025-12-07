package com.example.user_service.controller;

import com.example.user_service.dto.UserRequest;
import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_ShouldReturnCreatedUserWithLinks() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Alice Smith");
        userRequest.setEmail("alice@example.com");
        userRequest.setAge(25);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content.name", is("Alice Smith")))
                .andExpect(jsonPath("$.content.email", is("alice@example.com")))
                .andExpect(jsonPath("$.content.age", is(25)))
                .andExpect(jsonPath("$._links.self.href", notNullValue()))
                .andExpect(jsonPath("$._links.update.href", notNullValue()))
                .andExpect(jsonPath("$._links.delete.href", notNullValue()));
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenEmailExists() throws Exception {
        User existingUser = new User();
        existingUser.setName("Existing User");
        existingUser.setEmail("existing@example.com");
        existingUser.setAge(30);
        existingUser.setCreatedAt(LocalDateTime.now());
        userRepository.save(existingUser);

        UserRequest userRequest = new UserRequest();
        userRequest.setName("New User");
        userRequest.setEmail("existing@example.com");
        userRequest.setAge(25);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_ShouldReturnUserWithLinks() throws Exception {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAge(30);
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        mockMvc.perform(get("/api/users/{id}", savedUser.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.name", is("Test User")))
                .andExpect(jsonPath("$.content.email", is("test@example.com")))
                .andExpect(jsonPath("$._links.self.href", notNullValue()))
                .andExpect(jsonPath("$._links.all-users.href", notNullValue()));
    }

    @Test
    void getAllUsers_ShouldReturnUsersListWithLinks() throws Exception {
        User user1 = new User();
        user1.setName("User 1");
        user1.setEmail("user1@example.com");
        user1.setAge(25);
        user1.setCreatedAt(LocalDateTime.now());
        userRepository.save(user1);

        User user2 = new User();
        user2.setName("User 2");
        user2.setEmail("user2@example.com");
        user2.setAge(30);
        user2.setCreatedAt(LocalDateTime.now());
        userRepository.save(user2);

        mockMvc.perform(get("/api/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.userResponseList").isArray())
                .andExpect(jsonPath("$._embedded.userResponseList.length()", is(2)))
                .andExpect(jsonPath("$._links.self.href", notNullValue()))
                .andExpect(jsonPath("$._links.create.href", notNullValue()));
    }

    @Test
    void updateUser_ShouldUpdateSuccessfully() throws Exception {
        User existingUser = new User();
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");
        existingUser.setAge(25);
        existingUser.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(existingUser);

        UserRequest updateRequest = new UserRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setAge(26);

        mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.name", is("Updated Name")))
                .andExpect(jsonPath("$.content.email", is("updated@example.com")))
                .andExpect(jsonPath("$.content.age", is(26)));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        User user = new User();
        user.setName("To Delete");
        user.setEmail("delete@example.com");
        user.setAge(30);
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        mockMvc.perform(delete("/api/users/{id}", savedUser.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", savedUser.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Test");
        userRequest.setEmail("invalid-email");
        userRequest.setAge(25);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
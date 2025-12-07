package com.example.user_service.controller;

import com.example.user_service.dto.UserRequest;
import com.example.user_service.dto.UserResponse;
import com.example.user_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserRequest validUserRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        validUserRequest = new UserRequest();
        validUserRequest.setName("John Doe");
        validUserRequest.setEmail("john@example.com");
        validUserRequest.setAge(30);

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setName("John Doe");
        userResponse.setEmail("john@example.com");
        userResponse.setAge(30);
        userResponse.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createUser_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        UserRequest invalidRequest = new UserRequest();
        invalidRequest.setName("");
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setAge(null);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(UserRequest.class));
    }


    @Test
    void getUserById_WithNonExistingId_ShouldReturnBadRequest() throws Exception {
        when(userService.getUserById(999L))
                .thenThrow(new RuntimeException("User not found with id: 999"));

        mockMvc.perform(get("/api/users/{id}", 999L))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found with id: 999"));

        verify(userService, times(1)).getUserById(999L);
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void deleteUser_WithNonExistingId_ShouldReturnBadRequest() throws Exception {
        doThrow(new RuntimeException("User not found with id: 999"))
                .when(userService).deleteUser(999L);

        mockMvc.perform(delete("/api/users/{id}", 999L))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found with id: 999"));

        verify(userService, times(1)).deleteUser(999L);
    }
}
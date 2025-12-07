package com.example.user_service.service;

import com.example.user_service.dto.UserRequest;
import com.example.user_service.dto.UserResponse;
import com.example.user_service.event.UserEvent;
import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRequest testUserRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setAge(30);
        testUser.setCreatedAt(LocalDateTime.now());

        testUserRequest = new UserRequest();
        testUserRequest.setName("John Doe");
        testUserRequest.setEmail("john@example.com");
        testUserRequest.setAge(30);
    }

    @Test
    void createUser_ShouldReturnEntityModelWithLinks() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        EntityModel<UserResponse> result = userService.createUser(testUserRequest);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotNull();
        assertThat(result.getContent().getName()).isEqualTo("John Doe");
        assertThat(result.getLinks()).isNotEmpty();

        verify(userRepository, times(1)).existsByEmail("john@example.com");
        verify(userRepository, times(1)).save(any(User.class));
        verify(kafkaTemplate, times(1)).send(anyString(), any(UserEvent.class));
    }

    @Test
    void getAllUsers_ShouldReturnCollectionModelWithLinks() {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("User 1");
        user1.setEmail("user1@example.com");
        user1.setAge(25);

        User user2 = new User();
        user2.setId(2L);
        user2.setName("User 2");
        user2.setEmail("user2@example.com");
        user2.setAge(30);

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        var result = userService.getAllUsers();

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getLinks()).isNotEmpty();

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void updateUser_WithSameEmail_ShouldUpdateSuccessfully() {
        UserRequest updateRequest = new UserRequest();
        updateRequest.setName("John Updated");
        updateRequest.setEmail("john@example.com");
        updateRequest.setAge(31);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        EntityModel<UserResponse> result = userService.updateUser(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getLinks()).isNotEmpty();

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_WithDifferentEmail_ShouldCheckEmailExists() {
        UserRequest updateRequest = new UserRequest();
        updateRequest.setName("John Updated");
        updateRequest.setEmail("newemail@example.com");
        updateRequest.setAge(31);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        EntityModel<UserResponse> result = userService.updateUser(1L, updateRequest);

        assertThat(result).isNotNull();

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByEmail("newemail@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }


}

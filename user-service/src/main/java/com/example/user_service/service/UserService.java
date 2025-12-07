package com.example.user_service.service;

import com.example.user_service.controller.UserController;
import com.example.user_service.dto.UserRequest;
import com.example.user_service.dto.UserResponse;
import com.example.user_service.event.UserEvent;
import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserEventProducer userEventProducer;

    public UserService(UserRepository userRepository, UserEventProducer userEventProducer) {
        this.userRepository = userRepository;
        this.userEventProducer = userEventProducer;
    }

    public EntityModel<UserResponse> createUser(UserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RuntimeException("Email already exists: " + userRequest.getEmail());
        }

        User user = new User();
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setAge(userRequest.getAge());

        User savedUser = userRepository.save(user);

        UserEvent event = new UserEvent();
        event.setEventType(UserEvent.EventType.USER_CREATED);
        event.setUserId(savedUser.getId());
        event.setUserEmail(savedUser.getEmail());
        event.setUserName(savedUser.getName());
        event.setTimestamp(LocalDateTime.now());
        userEventProducer.sendUserEvent(event);

        UserResponse response = convertToResponse(savedUser);
        return addLinksToResponse(response);
    }

    public EntityModel<UserResponse> getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        UserResponse response = convertToResponse(user);
        return addLinksToResponse(response);
    }

    public CollectionModel<EntityModel<UserResponse>> getAllUsers() {
        List<EntityModel<UserResponse>> users = userRepository.findAll().stream()
                .map(user -> {
                    UserResponse response = convertToResponse(user);
                    return addLinksToResponse(response);
                })
                .collect(Collectors.toList());

        Link selfLink = linkTo(UserController.class).withSelfRel();
        Link createLink = linkTo(methodOn(UserController.class).createUser(null)).withRel("create");

        return CollectionModel.of(users, selfLink, createLink);
    }

    public EntityModel<UserResponse> updateUser(Long id, UserRequest userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (!user.getEmail().equals(userRequest.getEmail()) &&
                userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RuntimeException("Email already exists: " + userRequest.getEmail());
        }

        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setAge(userRequest.getAge());

        User updatedUser = userRepository.save(user);
        UserResponse response = convertToResponse(updatedUser);
        return addLinksToResponse(response);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        userRepository.deleteById(id);

        UserEvent event = new UserEvent();
        event.setEventType(UserEvent.EventType.USER_DELETED);
        event.setUserId(user.getId());
        event.setUserEmail(user.getEmail());
        event.setUserName(user.getName());
        event.setTimestamp(LocalDateTime.now());
        userEventProducer.sendUserEvent(event);
    }

    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setAge(user.getAge());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    private EntityModel<UserResponse> addLinksToResponse(UserResponse response) {
        Long userId = response.getId();

        EntityModel<UserResponse> entityModel = EntityModel.of(response);

        entityModel.add(linkTo(methodOn(UserController.class).getUserById(userId)).withSelfRel());
        entityModel.add(linkTo(methodOn(UserController.class).updateUser(userId, null)).withRel("update"));
        entityModel.add(linkTo(methodOn(UserController.class).deleteUser(userId)).withRel("delete"));
        entityModel.add(linkTo(UserController.class).withRel("all-users"));

        return entityModel;
    }
}
package com.thiagosalvess.ticketsales.features.user.service;

import com.thiagosalvess.ticketsales.common.exception.NotFoundException;
import com.thiagosalvess.ticketsales.features.user.model.dto.UserResponse;
import com.thiagosalvess.ticketsales.features.user.model.entity.User;
import com.thiagosalvess.ticketsales.features.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository repo) {
        this.userRepository = repo;
    }

    public UserResponse findById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        return toResponse(user);
    }

    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
        return toResponse(user);
    }


    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }
}

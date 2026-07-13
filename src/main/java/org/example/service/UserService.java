package org.example.service;

import org.example.entity.User;
import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User create(User user) {
        // проверка уникальности email
        Optional<User> existing = userRepository.findByEmail(user.getEmail());
        if (existing.isPresent()) {
            throw new BusinessException("Email already exists: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    public User update(Long id, User updated) {
        User existing = findById(id);
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        // email меняем, проверяем уникальность
        Optional<User> conflict = userRepository.findByEmail(updated.getEmail());
        if (conflict.isPresent() && !conflict.get().getId().equals(id)) {
            throw new BusinessException("Email already exists: " + updated.getEmail());
        }
        return userRepository.save(existing);
    }

    public void delete(Long id) {
        findById(id); // проверим существование
        userRepository.deleteById(id);
    }
}
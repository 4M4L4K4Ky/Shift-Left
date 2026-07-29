package com.amalakaky.aegiscode.application.port.out.db;

import com.amalakaky.aegiscode.domain.model.User;
import java.util.Optional;

public interface UserRepositoryPort {

  Optional<User> findByUsername(String username);

  User save(User user);
}

package com.amalakaky.aegiscode.infrastructure.adapter.out.db.repository;

import com.amalakaky.aegiscode.infrastructure.adapter.out.db.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, String> {

  Optional<UserEntity> findByUsername(String username);
}

package com.unifor.monitoria.repository;

import com.unifor.monitoria.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByMatricula(String matricula);
}

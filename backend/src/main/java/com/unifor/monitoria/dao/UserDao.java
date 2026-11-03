package com.unifor.monitoria.dao;

import com.unifor.monitoria.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao extends GenericDao<User, Integer> {

    Optional<User> findByMatricula(String matricula);

    List<User> findByRole(User.Role role);
}
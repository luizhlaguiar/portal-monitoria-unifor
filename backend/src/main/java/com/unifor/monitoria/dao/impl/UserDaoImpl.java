package com.unifor.monitoria.dao.impl;

import com.unifor.monitoria.dao.Dao;
import com.unifor.monitoria.dao.UserDao;
import com.unifor.monitoria.model.User;

import java.util.List;
import java.util.Optional;

@Dao
public class UserDaoImpl extends JpaGenericDao<User, Integer> implements UserDao {

    @Override
    public Optional<User> findByMatricula(String matricula) {
        return queryFirst("select u from User u where u.matricula = :matricula", "matricula", matricula);
    }

    @Override
    public List<User> findByRole(User.Role role) {
        return queryList("select u from User u where u.role = :role", "role", role);
    }
}
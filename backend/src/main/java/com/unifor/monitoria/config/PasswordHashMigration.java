package com.unifor.monitoria.config;

import com.unifor.monitoria.dao.UserDao;
import com.unifor.monitoria.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Ao subir o sistema, converte qualquer senha ainda em texto puro para hash BCrypt.
 * Roda de novo sem problema: senhas que já são hash (começam com "$2") são ignoradas.
 */
@Component
public class PasswordHashMigration implements ApplicationRunner {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        int migrated = 0;
        for (User user : userDao.findAll()) {
            String senha = user.getSenha();
            if (senha != null && !senha.startsWith("$2")) {
                user.setSenha(passwordEncoder.encode(senha));
                userDao.save(user);
                migrated++;
            }
        }
        if (migrated > 0) {
        }
    }
}
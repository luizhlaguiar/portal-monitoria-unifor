package com.unifor.monitoria.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca a implementacao de um DAO.
 * Reune em uma unica anotacao: bean Spring com traducao de excecoes de persistencia (@Repository)
 * e transacao somente-leitura por padrao (os metodos que gravam declaram o proprio @Transactional).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repository
@Transactional(readOnly = true)
public @interface Dao {
}
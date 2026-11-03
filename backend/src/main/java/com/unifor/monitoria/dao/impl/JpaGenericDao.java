package com.unifor.monitoria.dao.impl;

import com.unifor.monitoria.dao.GenericDao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.core.GenericTypeResolver;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementacao base dos DAOs com JPA (EntityManager). Todo o CRUD fica aqui, escrito uma unica vez;
 * os DAOs concretos so declaram as consultas especificas usando os metodos auxiliares abaixo.
 * Parametros das consultas sao passados em pares nome/valor: queryList(jpql, "nome", valor, ...).
 */
public abstract class JpaGenericDao<T, ID> implements GenericDao<T, ID> {

    @PersistenceContext
    protected EntityManager em;

    protected final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    protected JpaGenericDao() {
        this.entityClass = (Class<T>) GenericTypeResolver.resolveTypeArguments(getClass(), JpaGenericDao.class)[0];
    }

    @Override
    @Transactional
    public T save(T entity) {
        Object id = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
        if (id == null) {
            em.persist(entity);
            return entity;
        }
        return em.merge(entity);
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(em.find(entityClass, id));
    }

    @Override
    public List<T> findAll() {
        return queryList("select e from " + entityClass.getSimpleName() + " e");
    }

    @Override
    public boolean existsById(ID id) {
        return em.find(entityClass, id) != null;
    }

    @Override
    public long count() {
        return em.createQuery("select count(e) from " + entityClass.getSimpleName() + " e", Long.class).getSingleResult();
    }

    @Override
    @Transactional
    public void delete(T entity) {
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        T entity = em.find(entityClass, id);
        if (entity != null) {
            em.remove(entity);
        }
    }

    /** JPQL que retorna a propria entidade. */
    protected List<T> queryList(String jpql, Object... params) {
        return queryListOf(jpql, entityClass, params);
    }

    /** JPQL que retorna outro tipo (ex.: select m.aluno ...). */
    protected <R> List<R> queryListOf(String jpql, Class<R> resultType, Object... params) {
        return bind(em.createQuery(jpql, resultType), params).getResultList();
    }

    /** Primeiro resultado do JPQL, se houver. */
    protected Optional<T> queryFirst(String jpql, Object... params) {
        return bind(em.createQuery(jpql, entityClass).setMaxResults(1), params).getResultList().stream().findFirst();
    }

    /** Recebe um "select count(...)" e diz se existe ao menos um registro. */
    protected boolean queryExists(String countJpql, Object... params) {
        return bind(em.createQuery(countJpql, Long.class), params).getSingleResult() > 0;
    }

    @SuppressWarnings("unchecked")
    protected List<T> nativeList(String sql, Object... params) {
        return bind(em.createNativeQuery(sql, entityClass), params).getResultList();
    }

    protected int nativeUpdate(String sql, Object... params) {
        return bind(em.createNativeQuery(sql), params).executeUpdate();
    }

    private static <Q extends Query> Q bind(Q query, Object... params) {
        for (int i = 0; i + 1 < params.length; i += 2) {
            query.setParameter((String) params[i], params[i + 1]);
        }
        return query;
    }
}
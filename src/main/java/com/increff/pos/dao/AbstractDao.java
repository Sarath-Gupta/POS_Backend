package com.increff.pos.dao;

import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.lang.reflect.ParameterizedType;
import java.util.List;

@Repository
public abstract class AbstractDao<T> {

    @PersistenceContext
    private EntityManager entityManager;

    private final Class<T> entityClass;

    public AbstractDao() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    public T findById(Integer id) {
        return entityManager.find(entityClass,id);
    }

    public List<T> findAll() {
        String className = entityClass.getSimpleName();
        TypedQuery<T> query = entityManager.createQuery("SELECT e FROM " + className + " e", entityClass);
        return query.getResultList();
    }

    public void add(T entity) {
        entityManager.persist(entity);
    }


    public void update(T entity) {
        entityManager.merge(entity);
    }

    public void delete(T entity) {
        entityManager.remove(entity);
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }
}
package com.increff.pos.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    public Page<T> findAll(Pageable pageable) {
        String className = entityClass.getSimpleName();

        TypedQuery<T> query = entityManager.createQuery("SELECT e FROM " + className + " e", entityClass);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<T> content = query.getResultList();

        TypedQuery<Long> countQuery = entityManager.createQuery("SELECT COUNT(e.id) FROM " + className + " e", Long.class);
        long total = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    public List<T> getAll() {
        String className = entityClass.getSimpleName();
        String selectAllQuery = "SELECT e FROM " + className + " e";
        TypedQuery<T> query = entityManager.createQuery(selectAllQuery, entityClass);
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
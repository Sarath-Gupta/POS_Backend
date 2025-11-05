package com.increff.pos.dao;

import com.increff.pos.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class ClientDao extends AbstractDao<Client> {

    public static final String SELECT_BY_NAME = "SELECT c FROM Client c WHERE c.clientName = :name";
    public static final String FILTER_QUERY = "SELECT c FROM Client c " +
            "WHERE (:name IS NULL OR :name = '' OR LOWER(c.clientName) LIKE LOWER(CONCAT('%', :name, '%')))";

    public static final String COUNT_QUERY = "SELECT COUNT(c) FROM Client c " +
            "WHERE (:name IS NULL OR :name = '' OR LOWER(c.clientName) LIKE LOWER(CONCAT('%', :name, '%')))";

    public Client findByName(String name) {
        TypedQuery<Client> query = getEntityManager()
                .createQuery(SELECT_BY_NAME, Client.class);
        query.setParameter("name", name);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public Page<Client> getFilteredAll(Pageable pageable, String name) {
        TypedQuery<Client> query = getEntityManager().createQuery(FILTER_QUERY, Client.class);
        query.setParameter("name", name);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Client> resultList = query.getResultList();
        Query countQuery = getEntityManager().createQuery(COUNT_QUERY);
        countQuery.setParameter("name", name);
        Long total = (Long) countQuery.getSingleResult();
        return new PageImpl<>(resultList, pageable, total);
    }

}
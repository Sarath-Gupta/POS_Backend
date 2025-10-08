package com.increff.pos.dao;

import com.increff.pos.entity.Client;
import org.springframework.stereotype.Repository;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@Repository
public class ClientDao extends AbstractDao<Client> {

    public static final String SELECT_BY_NAME = "SELECT c FROM Client c WHERE c.clientName = :name";

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

}
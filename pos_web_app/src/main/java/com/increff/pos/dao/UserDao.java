package com.increff.pos.dao;

import com.increff.pos.entity.User;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@Repository
public class UserDao extends AbstractDao<User>{
    private static final String SELECT_BY_EMAIL = "SELECT u FROM User u WHERE u.email = :email";
    private static final String LOGIN = "SELECT u from User u WHERE u.email = :email AND u.password = :password";
    public User findByEmail(String email) {
        TypedQuery<User> query = getEntityManager()
                .createQuery(SELECT_BY_EMAIL, User.class);
        query.setParameter("email", email);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}

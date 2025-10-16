package com.increff.pos.dao;

import com.increff.pos.entity.Inventory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;


@Repository
public class InventoryDao extends AbstractDao<Inventory> {

    public static final String SELECT_BY_PRODUCT_ID = "SELECT i FROM Inventory i WHERE i.productId = :productId";
    public static final String SELECT_BETWEEN_QUANTITY = "SELECT i FROM Inventory i WHERE i.quantity BETWEEN :min AND :max";
    public static final String SELECT_BETWEEN_DATE = "SELECT i FROM Inventory i WHERE i.dateTime BETWEEN :startDate AND :endDate";

    @Transactional
    public Inventory findByProductId(Integer productId) {
        TypedQuery<Inventory> query = getEntityManager()
                .createQuery(SELECT_BY_PRODUCT_ID, Inventory.class);
        query.setParameter("productId", productId);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Inventory> findByQuantityBetween(Integer min, Integer max) {
        TypedQuery<Inventory> query = getEntityManager()
                .createQuery(SELECT_BETWEEN_QUANTITY, Inventory.class);
        query.setParameter("min", min);
        query.setParameter("max", max);
        return query.getResultList();
    }

    public List<Inventory> findByDateBetween(LocalDate startDate, LocalDate endDate) {
        TypedQuery<Inventory> query = getEntityManager()
                .createQuery(SELECT_BETWEEN_DATE, Inventory.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }

}

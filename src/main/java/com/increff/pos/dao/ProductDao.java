package com.increff.pos.dao;

import com.increff.pos.entity.Product;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class ProductDao extends AbstractDao<Product> {

    private static final String SELECT_BY_BARCODE = "SELECT p FROM Product p WHERE p.barcode = :barcode";
    private static final String SELECT_BY_CLIENT_ID = "SELECT p FROM Product p WHERE p.clientId = :clientId";
    private static final String SELECT_BY_NAME = "SELECT p FROM Product p WHERE p.name = :name";
    private static final String SELECT_BY_MRP_RANGE = "SELECT p FROM Product p WHERE p.mrp BETWEEN :minMrp AND :maxMrp";

    public Product findByBarcode(String barcode) {
        TypedQuery<Product> query = getEntityManager()
                .createQuery(SELECT_BY_BARCODE, Product.class);
        query.setParameter("barcode", barcode);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Product> findByClientId(Integer clientId) {
        TypedQuery<Product> query = getEntityManager()
                .createQuery(SELECT_BY_CLIENT_ID, Product.class);
        query.setParameter("clientId", clientId);
        return query.getResultList();
    }

    public Product findByName(String name) {
        TypedQuery<Product> query = getEntityManager()
                .createQuery(SELECT_BY_NAME, Product.class);
        query.setParameter("name",name);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Product> findByMrpBetween(Double minMrp, Double maxMrp) {
        TypedQuery<Product> query = getEntityManager()
                .createQuery(SELECT_BY_MRP_RANGE, Product.class);
        query.setParameter("minMrp", minMrp);
        query.setParameter("maxMrp", maxMrp);
        return query.getResultList();
    }
}
package com.increff.pos.dao;

import com.increff.pos.entity.Product;
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
public class ProductDao extends AbstractDao<Product> {

    private static final String SELECT_BY_BARCODE = "SELECT p FROM Product p WHERE p.barcode = :barcode";
    private static final String SELECT_BY_CLIENT_ID = "SELECT p FROM Product p WHERE p.clientId = :clientId";
    private static final String SELECT_BY_NAME = "SELECT p FROM Product p WHERE p.name = :name";
    private static final String SELECT_BY_MRP_RANGE = "SELECT p FROM Product p WHERE p.mrp BETWEEN :minMrp AND :maxMrp";

    public static final String FILTER_QUERY = "SELECT p FROM Product p " +
            "WHERE (:productName IS NULL OR :productName = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :productName, '%'))) " +
            "AND (:barcode IS NULL OR :barcode = '' OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', :barcode, '%'))) " +
            "AND (:clientId IS NULL OR p.clientId = :clientId) " +
            "AND (:minMRP IS NULL OR p.mrp >= :minMRP) " +
            "AND (:maxMRP IS NULL OR p.mrp <= :maxMRP)";

    public static final String COUNT_QUERY = "SELECT COUNT(p) FROM Product p " +
            "WHERE (:productName IS NULL OR :productName = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :productName, '%'))) " +
            "AND (:barcode IS NULL OR :barcode = '' OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', :barcode, '%'))) " +
            "AND (:clientId IS NULL OR p.clientId = :clientId) " +
            "AND (:minMRP IS NULL OR p.mrp >= :minMRP) " +
            "AND (:maxMRP IS NULL OR p.mrp <= :maxMRP)";


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

    @Transactional(readOnly = true)
    public Page<Product> getFilteredAll(Pageable pageable, String productName, String barcode, Integer clientId, Double minMRP, Double maxMRP) {
        TypedQuery<Product> query = getEntityManager().createQuery(FILTER_QUERY, Product.class);
        query.setParameter("productName", productName);
        query.setParameter("barcode", barcode);
        query.setParameter("clientId", clientId);
        query.setParameter("minMRP", minMRP);
        query.setParameter("maxMRP", maxMRP);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Product> resultList = query.getResultList();

        Query countQuery = getEntityManager().createQuery(COUNT_QUERY);
        countQuery.setParameter("productName", productName);
        countQuery.setParameter("barcode", barcode);
        countQuery.setParameter("clientId", clientId);
        countQuery.setParameter("minMRP", minMRP);
        countQuery.setParameter("maxMRP", maxMRP);

        Long total = (Long) countQuery.getSingleResult();

        return new PageImpl<>(resultList, pageable, total);
    }

}
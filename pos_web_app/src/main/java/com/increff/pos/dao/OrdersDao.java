package com.increff.pos.dao;

import com.increff.pos.entity.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.Query;
import java.time.ZonedDateTime;
import java.util.List;


@Repository
public class OrdersDao extends AbstractDao<Orders> {

    public static final String UPDATE_STATUS_INVOICED = "UPDATE Orders SET status = 'INVOICED' WHERE id = :orderId";
    public static final String UPDATE_STATUS_CANCELLED = "UPDATE Orders SET status = 'CANCELLED' WHERE id = :orderId";
    public static final String FILTER_QUERY = "SELECT o FROM Orders o " +
                    "WHERE (:orderId IS NULL OR o.id = :orderId) " +
                    "AND (:status IS NULL OR :status = '' OR LOWER(o.status) LIKE LOWER(CONCAT('%', :status, '%'))) " +
                    "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
                    "AND (:endDate IS NULL OR o.createdAt <= :endDate)";

    public static final String COUNT_QUERY = "SELECT COUNT(o) FROM Orders o WHERE "
            + "(:orderId IS NULL OR o.id = :orderId) "
            + "AND (:status IS NULL OR :status = '' OR LOWER(o.status) LIKE LOWER(CONCAT('%', :status, '%'))) "
            + "AND (:startDate IS NULL OR o.createdAt >= :startDate) "
            + "AND (:endDate IS NULL OR o.createdAt <= :endDate)";

    @Transactional
    public void updateStatusToInvoiced(Integer orderId) {
        Query query = getEntityManager()
                .createQuery(UPDATE_STATUS_INVOICED);
        query.setParameter("orderId", orderId);
        query.executeUpdate();
    }

    @Transactional
    public void cancelOrder(Integer orderId) {
        Query query = getEntityManager()
                .createQuery(UPDATE_STATUS_CANCELLED);
        query.setParameter("orderId", orderId);
        query.executeUpdate();
    }

    @Transactional(readOnly = true)
    public Page<Orders> getFilteredAll(Pageable pageable, Integer orderId, String status, ZonedDateTime startDate, ZonedDateTime endDate) {
        Query query = getEntityManager().createQuery(FILTER_QUERY);
        query.setParameter("orderId", orderId);
        query.setParameter("status", status);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Orders> resultList = query.getResultList();
        Query countQuery = getEntityManager().createQuery(COUNT_QUERY);
        countQuery.setParameter("orderId", orderId);
        countQuery.setParameter("status", status);
        countQuery.setParameter("startDate", startDate);
        countQuery.setParameter("endDate", endDate);
        Long total = (Long) countQuery.getSingleResult();
        return new PageImpl<>(resultList, pageable, total);
    }






}
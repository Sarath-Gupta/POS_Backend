package com.increff.pos.factory;

import com.increff.pos.entity.Orders;
import com.increff.pos.commons.OrderStatus;
import org.instancio.Instancio;
import org.instancio.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;

import static org.instancio.Select.field;

public final class OrderFactory {

    /**
     * Private constructor to prevent instantiation.
     */
    private OrderFactory() {
    }

    /**
     * Model for a 'new' Order, as if it were just created.
     * It will always have a null ID and a 'CREATED' status.
     */
    private static final Model<Orders> NEW_ORDER_MODEL = Instancio.of(Orders.class)
            .set(field(Orders::getId), null)
            // --- FIX IS HERE: Pass the enum directly, not a String ---
            .set(field(Orders::getStatus), OrderStatus.CREATED)
            .toModel();

    /**
     * Model for a 'persisted' Order, as if it were loaded from the DB.
     * It will always have a non-null, positive ID.
     */
    private static final Model<Orders> PERSISTED_ORDER_MODEL = Instancio.of(Orders.class)
            .generate(field(Orders::getId), gen -> gen.ints().min(1))
            .toModel();


    // --- Methods for NEW objects (ID is always null) ---

    /**
     * Creates a mock Order object representing a new entity.
     * Its ID will always be null and status will be CREATED.
     *
     * @param totalAmount The total amount for the order.
     * @return A new Orders object.
     */
    public static Orders mockNewObject(Double totalAmount) {
        return Instancio.of(NEW_ORDER_MODEL)
                .set(field(Orders::getTotal_amount), totalAmount)
                .create();
    }

    // --- Methods for PERSISTED objects (ID is non-null) ---

    /**
     * Creates a mock Order object representing a persisted entity with a specific ID.
     *
     * @param id The specific ID to set on the object.
     * @return An Orders object with the specified ID.
     */
    public static Orders mockPersistedObject(Integer id) {
        return Instancio.of(PERSISTED_ORDER_MODEL)
                .set(field(Orders::getId), id)
                .create();
    }

    /**
     * Creates a mock Order object representing a persisted entity with full details.
     *
     * @param id          The specific ID.
     * @param totalAmount The total amount.
     * @param status      The order status (e.g., CREATED, INVOICED).
     * @return A persisted Orders object.
     */
    public static Orders mockPersistedObject(Integer id, Double totalAmount, OrderStatus status) {
        return Instancio.of(PERSISTED_ORDER_MODEL)
                .set(field(Orders::getId), id)
                .set(field(Orders::getTotal_amount), totalAmount)
                // --- FIX IS HERE: Pass the enum directly, not a String ---
                .set(field(Orders::getStatus), status)
                .create();
    }

    /**
     * Creates a list of two sample persisted orders.
     * @return A List of Orders.
     */
    public static List<Orders> createOrderList() {
        return Arrays.asList(
                mockPersistedObject(1, 100.50, OrderStatus.CREATED),
                mockPersistedObject(2, 25.00, OrderStatus.INVOICED)
        );
    }

    // --- Methods for Spring Data Pagination ---

    /**
     * Creates a default Pageable object (Page 0, 10 items).
     * @return A Pageable instance.
     */
    public static Pageable createPageable() {
        return PageRequest.of(0, 10);
    }

    /**
     * Creates a Page of Orders from a given list and pageable.
     *
     * @param orderList The list of orders for the current page.
     * @param pageable  The Pageable object.
     * @return A Page<Orders> instance.
     */
    public static Page<Orders> createOrderPage(List<Orders> orderList, Pageable pageable) {
        // PageImpl takes (content, pageable, totalElements)
        return new PageImpl<>(orderList, pageable, orderList.size());
    }
}


package com.increff.pos.factory; // Use your project's test package

import com.increff.pos.entity.OrderItem;
import org.instancio.Instancio;
import org.instancio.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;

import static org.instancio.Select.field;

/**
 * Test Data Factory for creating OrderItem entities using Instancio.
 * This class provides standardized objects for use in unit tests,
 * modeling the "new" vs. "persisted" object state.
 */
public final class OrderItemFactory {

    /**
     * Private constructor to prevent instantiation.
     */
    private OrderItemFactory() {
    }

    /**
     * Model for a 'new' OrderItem, as if it came from a form.
     * It will always have a null ID.
     */
    private static final Model<OrderItem> NEW_ORDER_ITEM_MODEL = Instancio.of(OrderItem.class)
            .set(field(OrderItem::getId), null)
            .toModel();

    /**
     * Model for a 'persisted' OrderItem, as if it were loaded from the DB.
     * It will always have a non-null, positive ID.
     */
    private static final Model<OrderItem> PERSISTED_ORDER_ITEM_MODEL = Instancio.of(OrderItem.class)
            .generate(field(OrderItem::getId), gen -> gen.ints().min(1))
            .toModel();


    // --- Methods for NEW objects (ID is always null) ---

    /**
     * Creates a mock OrderItem object representing a new entity with key details.
     * Its ID will always be null.
     *
     * @param orderId     The associated order ID.
     * @param productId   The associated product ID.
     * @param quantity    The quantity for this item.
     * @param sellingPrice The selling price for this item.
     * @return A new OrderItem object.
     */
    public static OrderItem mockNewObject(Integer orderId, Integer productId, Integer quantity, Double sellingPrice) {
        return Instancio.of(NEW_ORDER_ITEM_MODEL)
                .set(field(OrderItem::getOrderId), orderId)
                .set(field(OrderItem::getProductId), productId)
                .set(field(OrderItem::getQuantity), quantity)
                .set(field(OrderItem::getSellingPrice), sellingPrice)
                .create();
    }

    // --- Methods for PERSISTED objects (ID is non-null) ---

    /**
     * Creates a mock OrderItem object representing a persisted entity with full details.
     *
     * @param id          The specific ID.
     * @param orderId     The associated order ID.
     * @param productId   The associated product ID.
     * @param quantity    The quantity for this item.
     * @param sellingPrice The selling price for this item.
     * @return A persisted OrderItem object.
     */
    public static OrderItem mockPersistedObject(Integer id, Integer orderId, Integer productId, Integer quantity, Double sellingPrice) {
        return Instancio.of(PERSISTED_ORDER_ITEM_MODEL)
                .set(field(OrderItem::getId), id)
                .set(field(OrderItem::getOrderId), orderId)
                .set(field(OrderItem::getProductId), productId)
                .set(field(OrderItem::getQuantity), quantity)
                .set(field(OrderItem::getSellingPrice), sellingPrice)
                .create();
    }

    /**
     * Creates a list of two sample persisted order items for a specific order.
     * @param orderId The order ID all items should belong to.
     * @return A List of OrderItems.
     */
    public static List<OrderItem> createOrderItemList(Integer orderId) {
        return Arrays.asList(
                mockPersistedObject(1, orderId, 101, 2, 10.99),
                mockPersistedObject(2, orderId, 102, 5, 20.49)
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
     * Creates a Page of OrderItems from a given list and pageable.
     *
     * @param orderItemList The list of order items for the current page.
     * @param pageable      The Pageable object.
     * @return A Page<OrderItem> instance.
     */
    public static Page<OrderItem> createOrderItemPage(List<OrderItem> orderItemList, Pageable pageable) {
        // PageImpl takes (content, pageable, totalElements)
        return new PageImpl<>(orderItemList, pageable, orderItemList.size());
    }
}

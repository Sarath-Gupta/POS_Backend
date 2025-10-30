package com.increff.pos.factory; // Use your project's test package

import com.increff.pos.entity.Inventory;
import org.instancio.Instancio;
import org.instancio.Model;

import java.util.Arrays;
import java.util.List;

import static org.instancio.Select.field;

/**
 * Test Data Factory for creating Inventory entities using Instancio.
 * This class provides standardized objects for use in unit tests,
 * modeling the "new" vs. "persisted" object state.
 */
public final class InventoryFactory {

    /**
     * Private constructor to prevent instantiation.
     */
    private InventoryFactory() {
    }

    /**
     * Model for a 'new' Inventory, as if it were just created.
     * It will always have a null ID.
     */
    private static final Model<Inventory> NEW_INVENTORY_MODEL = Instancio.of(Inventory.class)
            .set(field(Inventory::getId), null)
            .toModel();

    /**
     * Model for a 'persisted' Inventory, as if it were loaded from the DB.
     * It will always have a non-null, positive ID.
     */
    private static final Model<Inventory> PERSISTED_INVENTORY_MODEL = Instancio.of(Inventory.class)
            .generate(field(Inventory::getId), gen -> gen.ints().min(1))
            .toModel();


    // --- Methods for NEW objects (ID is always null) ---

    /**
     * Creates a mock Inventory object representing a new entity.
     * Its ID will always be null.
     *
     * @param productId The product ID for this inventory item.
     * @param quantity  The initial quantity.
     * @return A new Inventory object.
     */
    public static Inventory mockNewObject(Integer productId, Integer quantity) {
        return Instancio.of(NEW_INVENTORY_MODEL)
                .set(field(Inventory::getProductId), productId)
                .set(field(Inventory::getQuantity), quantity)
                .create();
    }

    // --- Methods for PERSISTED objects (ID is non-null) ---

    /**
     * Creates a mock Inventory object representing a persisted entity.
     *
     * @param id        The specific inventory ID.
     * @param productId The product ID.
     * @param quantity  The current quantity.
     * @return A persisted Inventory object.
     */
    public static Inventory mockPersistedObject(Integer id, Integer productId, Integer quantity) {
        return Instancio.of(PERSISTED_INVENTORY_MODEL)
                .set(field(Inventory::getId), id)
                .set(field(Inventory::getProductId), productId)
                .set(field(Inventory::getQuantity), quantity)
                .create();
    }

    /**
     * Creates a list of two sample persisted inventory items.
     * @return A List of Inventory items.
     */
    public static List<Inventory> createInventoryList() {
        return Arrays.asList(
                mockPersistedObject(1, 101, 50),
                mockPersistedObject(2, 102, 200)
        );
    }
}

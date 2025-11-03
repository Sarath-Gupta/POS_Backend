package com.increff.pos.factory; // Use your project's test package

import com.increff.pos.model.form.OrderItemForm;
import org.instancio.Instancio;
import org.instancio.Model;
import java.util.List;

import static org.instancio.Select.field;

/**
 * Test Data Factory for creating OrderItemForm DTOs using Instancio.
 * This class provides standardized objects for use in unit tests.
 */
public final class OrderItemFlowFactory {

    /**
     * Private constructor to prevent instantiation.
     */
    private OrderItemFlowFactory() {
    }

    /**
     * Model for a 'new' OrderItemForm.
     */
    private static final Model<OrderItemForm> FORM_MODEL = Instancio.of(OrderItemForm.class)
            // --- FIX: Removed .alphanumeric() as gen.string() is alphanumeric by default ---
            .generate(field(OrderItemForm::getBarcode), gen -> gen.string().length(8))
            .generate(field(OrderItemForm::getQuantity), gen -> gen.ints().min(1).max(10))
            .generate(field(OrderItemForm::getSellingPrice), gen -> gen.doubles().min(10.0).max(100.0))
            .toModel();

    /**
     * Creates a mock OrderItemForm object with random valid data.
     *
     * @return A mock OrderItemForm.
     */
    public static OrderItemForm mockOrderItemForm() {
        return Instancio.of(FORM_MODEL).create();
    }

    /**
     * Creates a mock OrderItemForm object with specific data.
     *
     * @param barcode       The barcode.
     * @param quantity      The quantity.
     * @param sellingPrice  The selling price.
     * @return A mock OrderItemForm.
     */
    public static OrderItemForm mockOrderItemForm(String barcode, Integer quantity, Double sellingPrice) {
        return Instancio.of(FORM_MODEL)
                .set(field(OrderItemForm::getBarcode), barcode)
                .set(field(OrderItemForm::getQuantity), quantity)
                .set(field(OrderItemForm::getSellingPrice), sellingPrice)
                .create();
    }

    /**
     * Creates a list of mock OrderItemForm objects.
     *
     * @param count The number of forms to create.
     * @return A list of mock OrderItemForms.
     */
    public static List<OrderItemForm> createOrderItemFormList(int count) {
        return Instancio.ofList(FORM_MODEL).size(count).create();
    }
}


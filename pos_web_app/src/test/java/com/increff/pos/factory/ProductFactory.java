package com.increff.pos.factory; // Use your project's test package

import com.increff.pos.entity.Product;
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
 * Test Data Factory for creating Product entities using Instancio.
 * This class provides standardized objects for use in unit tests,
 * modeling the "new" vs. "persisted" object state.
 */
public final class ProductFactory {

    /**
     * Private constructor to prevent instantiation.
     */
    private ProductFactory() {
    }

    /**
     * Model for a 'new' Product, as if it came from a form.
     * It will always have a null ID.
     */
    private static final Model<Product> NEW_PRODUCT_MODEL = Instancio.of(Product.class)
            .set(field(Product::getId), null)
            .toModel();

    /**
     * Model for a 'persisted' Product, as if it were loaded from the DB.
     * It will always have a non-null, positive ID.
     */
    private static final Model<Product> PERSISTED_PRODUCT_MODEL = Instancio.of(Product.class)
            .generate(field(Product::getId), gen -> gen.ints().min(1))
            .toModel();


    // --- Methods for NEW objects (ID is always null) ---

    /**
     * Creates a mock Product object representing a new entity with a specific name.
     * Its ID will always be null.
     *
     * @param name The specific name to set on the object.
     * @return A new Product object with a null ID and the specified name.
     */
    public static Product mockNewObject(String name) {
        return Instancio.of(NEW_PRODUCT_MODEL)
                .set(field(Product::getName), name)
                .create();
    }

    /**
     * Creates a mock Product object representing a new entity with full details.
     * Its ID will always be null.
     *
     * @param name    The product name.
     * @param barcode The product barcode.
     * @param mrp     The product MRP.
     * @return A new Product object.
     */
    public static Product mockNewObject(String name, String barcode, Double mrp) {
        return Instancio.of(NEW_PRODUCT_MODEL)
                .set(field(Product::getName), name)
                .set(field(Product::getBarcode), barcode)
                .set(field(Product::getMrp), mrp)
                .create();
    }

    // --- Methods for PERSISTED objects (ID is non-null) ---

    /**
     * Creates a mock Product object representing a persisted entity with a specific ID and name.
     *
     * @param id   The specific ID to set on the object.
     * @param name The specific name to set on the object.
     * @return A Product object with the specified ID and name.
     */
    public static Product mockPersistedObject(Integer id, String name) {
        return Instancio.of(PERSISTED_PRODUCT_MODEL)
                .set(field(Product::getId), id)
                .set(field(Product::getName), name)
                .create();
    }

    /**
     * Creates a mock Product object representing a persisted entity with full details.
     *
     * @param id      The specific ID.
     * @param name    The product name.
     * @param barcode The product barcode.
     * @param mrp     The product MRP.
     * @return A persisted Product object.
     */
    public static Product mockPersistedObject(Integer id, String name, String barcode, Double mrp) {
        return Instancio.of(PERSISTED_PRODUCT_MODEL)
                .set(field(Product::getId), id)
                .set(field(Product::getName), name)
                .set(field(Product::getBarcode), barcode)
                .set(field(Product::getMrp), mrp)
                .create();
    }

    /**
     * Creates a list of two sample persisted products.
     * @return A List of Products.
     */
    public static List<Product> createProductList() {
        return Arrays.asList(
                mockPersistedObject(1, "Product A", "barcode_a", 10.99),
                mockPersistedObject(2, "Product B", "barcode_b", 20.49)
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
     * Creates a Page of Products from a given list and pageable.
     *
     * @param productList The list of products for the current page.
     * @param pageable    The Pageable object.
     * @return A Page<Product> instance.
     */
    public static Page<Product> createProductPage(List<Product> productList, Pageable pageable) {
        // PageImpl takes (content, pageable, totalElements)
        return new PageImpl<>(productList, pageable, productList.size());
    }
}

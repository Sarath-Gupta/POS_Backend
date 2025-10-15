package com.increff.pos.test;

import com.increff.pos.config.TestConfig;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.entity.Product;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@Transactional // Ensures test isolation and rollback
public class ProductDaoTest {

    @Autowired
    private ProductDao productDao;

    private Product createProduct(String barcode, Integer clientId, String name, Double mrp) {
        Product p = new Product();
        p.setBarcode(barcode);
        p.setClientId(clientId);
        p.setName(name);
        p.setMrp(mrp);
        p.setImgUrl("https://placehold.it/100x100");
        productDao.add(p);
        return p;
    }


    @Test
    public void testAddAndFindById() {
        Product product1 = createProduct("BAR123", 1, "Milk Chocolate", 50.0);
        assertNotNull("Product ID should be generated after add", product1.getId());

        Product found = productDao.findById(product1.getId());

        assertNotNull("Product should be found by ID", found);
        assertEquals("Barcode should match", "BAR123", found.getBarcode());
    }

    @Test
    public void testFindAll() {
        createProduct("SKU1", 1, "A", 10.0);
        createProduct("SKU2", 2, "B", 20.0);

        List<Product> list = productDao.findAll();

        assertEquals("FindAll should return 2 products", 2, list.size());
    }


    @Test
    public void testFindByBarcode_Found() {
        createProduct("TARGETCODE", 101, "Target Product", 100.0);

        Product found = productDao.findByBarcode("TARGETCODE");

        assertNotNull("Product should be found by barcode", found);
        assertEquals("Barcode should match lookup key", "TARGETCODE", found.getBarcode());
    }

    @Test
    public void testFindByClientId() {
        createProduct("A1", 5, "Product A", 10.0);
        createProduct("B2", 10, "Product B", 20.0);
        createProduct("C3", 5, "Product C", 30.0);

        List<Product> results = productDao.findByClientId(5);

        assertEquals("Should find exactly 2 products for client ID 5", 2, results.size());
        assertTrue("Results should contain Product C",
                results.stream().anyMatch(p -> p.getName().equals("Product C")));
        results.forEach(p -> assertEquals("All returned products must have client ID 5", (Integer) 5, p.getClientId()));
    }

    @Test
    public void testFindByName_Found() {
        createProduct("XXY", 1, "Specific Item", 15.0);

        Product found = productDao.findByName("Specific Item");

        assertNotNull("Product should be found by name", found);
    }

    @Test
    public void testFindByName_NotFound() {
        createProduct("ZZZ", 1, "Apples", 1.0);

        Product notFound = productDao.findByName("Missing Name");

        assertNull("findByName should return null if product is not found", notFound);
    }

    @Test
    public void testFindByMrpBetween() {
        createProduct("P1", 1, "Cheap", 5.0);
        createProduct("P2", 1, "Mid-Low", 49.99);
        createProduct("P3", 1, "Mid-High", 100.0);
        createProduct("P4", 1, "Expensive", 100.01);

        Double minMrp = 10.0;
        Double maxMrp = 100.0;

        List<Product> results = productDao.findByMrpBetween(minMrp, maxMrp);

        assertEquals("Should find exactly 2 products within the [49.99, 100.0] range", 2, results.size());

        assertTrue(results.stream().anyMatch(p -> p.getName().equals("Mid-Low")));
        assertTrue(results.stream().anyMatch(p -> p.getName().equals("Mid-High")));
    }
}

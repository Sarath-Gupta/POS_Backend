package com.increff.pos.integration.dao;

import com.increff.pos.config.TestDbConfig;
import com.increff.pos.dao.ClientDao;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.entity.Client;
import com.increff.pos.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestDbConfig.class})
@Transactional
public class ProductDaoIntegrationTest {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ClientDao clientDao;

    private Client testClient;
    private Client testClient2;

    @BeforeEach
    public void setup() {
        testClient = new Client();
        testClient.setClientName("Test Client 1");
        clientDao.add(testClient);

        testClient2 = new Client();
        testClient2.setClientName("Test Client 2");
        clientDao.add(testClient2);
    }

    @Test
    public void findByName_whenNameExists_shouldReturnProduct() {
        Product p = createTestProduct("barcode-1", "find-me", 10.0, testClient.getId());
        productDao.add(p);

        Product found = productDao.findByName("find-me");
        assertNotNull(found);
        assertEquals("find-me", found.getName());
    }

    @Test
    public void findByName_whenNameDoesNotExist_shouldReturnNull() {
        Product found = productDao.findByName("does-not-exist");
        assertNull(found);
    }

    @Test
    public void findByBarcode_whenBarcodeExists_shouldReturnProduct() {
        Product p = createTestProduct("find-me-barcode", "Test Product", 10.0, testClient.getId());
        productDao.add(p);

        Product found = productDao.findByBarcode("find-me-barcode");
        assertNotNull(found);
        assertEquals("find-me-barcode", found.getBarcode());
    }

    @Test
    public void findByBarcode_whenBarcodeDoesNotExist_shouldReturnNull() {
        Product found = productDao.findByBarcode("does-not-exist");
        assertNull(found);
    }

    @Test
    public void findByClientId_whenClientHasProducts_shouldReturnList() {
        productDao.add(createTestProduct("p1", "Product 1", 10.0, testClient.getId()));
        productDao.add(createTestProduct("p2", "Product 2", 10.0, testClient.getId()));
        productDao.add(createTestProduct("p3", "Product 3", 10.0, testClient2.getId()));

        List<Product> client1Products = productDao.findByClientId(testClient.getId());
        assertNotNull(client1Products);
        assertEquals(2, client1Products.size());
    }

    @Test
    public void findByClientId_whenClientHasNoProducts_shouldReturnEmptyList() {
        List<Product> client1Products = productDao.findByClientId(testClient.getId());
        assertNotNull(client1Products);
        assertTrue(client1Products.isEmpty());
    }

    @Test
    public void findByMrpBetween_whenProductsInRange_shouldReturnList() {
        productDao.add(createTestProduct("p1", "Product 1", 5.0, testClient.getId()));
        productDao.add(createTestProduct("p2", "Product 2", 10.0, testClient.getId()));
        productDao.add(createTestProduct("p3", "Product 3", 15.0, testClient.getId()));

        List<Product> inRange = productDao.findByMrpBetween(10.0, 20.0);
        assertNotNull(inRange);
        assertEquals(2, inRange.size());
    }

    @Test
    public void findByMrpBetween_whenNoProductsInRange_shouldReturnEmptyList() {
        productDao.add(createTestProduct("p1", "Product 1", 5.0, testClient.getId()));
        productDao.add(createTestProduct("p2", "Product 2", 25.0, testClient.getId()));

        List<Product> inRange = productDao.findByMrpBetween(10.0, 20.0);
        assertNotNull(inRange);
        assertTrue(inRange.isEmpty());
    }

    @Test
    public void add_and_findById_shouldPersistProduct() {
        Product p = createTestProduct("barcode-add", "Test Product", 10.0, testClient.getId());
        productDao.add(p);

        assertNotNull(p.getId());
        Product found = productDao.findById(p.getId());
        assertNotNull(found);
        assertEquals("Test Product", found.getName());
    }

    @Test
    public void findById_withNullId_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            productDao.findById(null);
        });
    }

    @Test
    public void add_withNullEntity_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            productDao.add(null);
        });
    }

    @Test
    public void update_shouldModifyProduct() {
        Product p = createTestProduct("barcode-update", "Old Name", 10.0, testClient.getId());
        productDao.add(p);
        Integer id = p.getId();

        Product toUpdate = productDao.findById(id);
        assertNotNull(toUpdate);
        toUpdate.setName("New Name");
        toUpdate.setMrp(20.0);
        productDao.update(toUpdate);

        Product updated = productDao.findById(id);
        assertNotNull(updated);
        assertEquals("New Name", updated.getName());
        assertEquals(20.0, updated.getMrp());
    }

    @Test
    public void delete_shouldRemoveProduct() {
        Product p = createTestProduct("barcode-delete", "Delete Me", 10.0, testClient.getId());
        productDao.add(p);
        Integer id = p.getId();
        assertNotNull(productDao.findById(id));

        productDao.delete(p);

        Product deleted = productDao.findById(id);
        assertNull(deleted);
    }

    @Test
    public void delete_withNullEntity_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            productDao.delete(null);
        });
    }

    @Test
    public void getAll_shouldReturnAllProducts() {
        productDao.add(createTestProduct("p1", "Product 1", 10.0, testClient.getId()));
        productDao.add(createTestProduct("p2", "Product 2", 10.0, testClient.getId()));

        List<Product> all = productDao.getAll();
        assertNotNull(all);
        assertEquals(2, all.size());
    }

    @Test
    public void getAll_whenNoProducts_shouldReturnEmptyList() {
        List<Product> all = productDao.getAll();
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    public void findAll_paginated_shouldReturnCorrectPage() {
        productDao.add(createTestProduct("p1", "P1", 10.0, testClient.getId()));
        productDao.add(createTestProduct("p2", "P2", 10.0, testClient.getId()));
        productDao.add(createTestProduct("p3", "P3", 10.0, testClient.getId()));

        Pageable pageable = PageRequest.of(0, 2);
        Page<Product> productPage = productDao.findAll(pageable);

        assertEquals(3, productPage.getTotalElements());
        assertEquals(2, productPage.getTotalPages());
        assertEquals(2, productPage.getContent().size());

        Pageable pageable2 = PageRequest.of(1, 2);
        Page<Product> productPage2 = productDao.findAll(pageable2);

        assertEquals(1, productPage2.getContent().size());
    }

    @Test
    public void findAll_whenNoProducts_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = productDao.findAll(pageable);

        assertNotNull(productPage);
        assertEquals(0, productPage.getTotalElements());
        assertTrue(productPage.getContent().isEmpty());
    }

    @Test
    public void findAll_withNullPageable_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            productDao.findAll(null);
        });
    }

    private Product createTestProduct(String barcode, String name, Double mrp, Integer clientId) {
        Product p = new Product();
        p.setClientId(clientId);
        p.setName(name);
        p.setBarcode(barcode);
        p.setMrp(mrp);
        return p;
    }
}


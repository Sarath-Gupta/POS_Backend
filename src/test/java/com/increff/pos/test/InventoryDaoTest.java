package com.increff.pos.test;

import com.increff.pos.config.TestConfig;
import com.increff.pos.dao.InventoryDao;
import com.increff.pos.entity.Inventory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@Transactional
public class InventoryDaoTest {

    @Autowired
    private InventoryDao inventoryDao;

    private Inventory createInventory(Integer productId, Integer quantity, LocalDate date) {
        Inventory i = new Inventory();
        i.setProductId(productId);
        i.setQuantity(quantity);
        inventoryDao.add(i);
        return i;
    }

    @Test
    public void testAddAndFindById() {
        Inventory record = createInventory(101, 50, LocalDate.now());
        assertNotNull("Record ID should be generated after add", record.getId());
        Inventory found = inventoryDao.findById(record.getId());
        assertNotNull("Record should be found by ID", found);
        assertEquals("Quantity should match", (Integer) 50, found.getQuantity());
    }

    @Test
    public void testFindAll() {
        createInventory(1, 10, LocalDate.now());
        createInventory(2, 20, LocalDate.now());
        List<Inventory> list = inventoryDao.findAll();
        assertEquals("FindAll should return 2 records", 2, list.size());
    }

    @Test
    public void testFindByProductId_Found() {
        createInventory(999, 150, LocalDate.now());
        Inventory found = inventoryDao.findByProductId(999);
        assertNotNull("Inventory should be found by product ID", found);
        assertEquals("Quantity should match", (Integer) 150, found.getQuantity());
    }

    @Test
    public void testFindByProductId_NotFound() {
        createInventory(111, 10, LocalDate.now());
        Inventory notFound = inventoryDao.findByProductId(9999);
        assertNull("findByProductId should return null if record is not found", notFound);
    }

    @Test
    public void testFindByQuantityBetween() {
        createInventory(1, 5, LocalDate.now());
        createInventory(2, 50, LocalDate.now());
        createInventory(3, 150, LocalDate.now());
        createInventory(4, 250, LocalDate.now());
        createInventory(5, 300, LocalDate.now());

        Integer min = 50;
        Integer max = 250;

        List<Inventory> results = inventoryDao.findByQuantityBetween(min, max);

        assertEquals("Should find exactly 3 records within the [50, 250] range", 3, results.size());

        results.forEach(i -> {
            assertTrue("Quantity must be >= " + min, i.getQuantity() >= min);
            assertTrue("Quantity must be <= " + max, i.getQuantity() <= max);
        });
    }

}

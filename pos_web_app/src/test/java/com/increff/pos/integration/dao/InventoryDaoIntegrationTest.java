package com.increff.pos.integration.dao;

import com.increff.pos.config.TestDbConfig;
import com.increff.pos.dao.ClientDao;
import com.increff.pos.dao.InventoryDao;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.entity.Client;
import com.increff.pos.entity.Inventory;
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

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestDbConfig.class})
@Transactional
public class InventoryDaoIntegrationTest {

    @Autowired
    private InventoryDao inventoryDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ClientDao clientDao;

    private Client testClient;
    private Product testProduct1;
    private Product testProduct2;
    private Product testProduct3;

    @BeforeEach
    public void setup() {
        testClient = new Client();
        testClient.setClientName("Test Client");
        clientDao.add(testClient);

        testProduct1 = createTestProduct("barcode-p1", "Product 1");
        testProduct2 = createTestProduct("barcode-p2", "Product 2");
        testProduct3 = createTestProduct("barcode-p3", "Product 3");
    }

    @Test
    public void findByProductId_whenExists_shouldReturnInventory() {
        createTestInventory(testProduct1.getId(), 50, ZonedDateTime.now());
        Inventory found = inventoryDao.findByProductId(testProduct1.getId());
        assertNotNull(found);
        assertEquals(testProduct1.getId(), found.getProductId());
        assertEquals(50, found.getQuantity());
    }

    @Test
    public void findByProductId_whenDoesNotExist_shouldReturnNull() {
        Inventory found = inventoryDao.findByProductId(-99);
        assertNull(found);
    }

    @Test
    public void findByQuantityBetween_whenItemsInRange_shouldReturnList() {
        createTestInventory(testProduct1.getId(), 5, ZonedDateTime.now());
        createTestInventory(testProduct2.getId(), 10, ZonedDateTime.now());
        createTestInventory(testProduct3.getId(), 20, ZonedDateTime.now());

        List<Inventory> items = inventoryDao.findByQuantityBetween(10, 20);
        assertNotNull(items);
        assertEquals(2, items.size());
    }

    @Test
    public void findByQuantityBetween_whenNoItemsInRange_shouldReturnEmptyList() {
        createTestInventory(testProduct1.getId(), 5, ZonedDateTime.now());
        createTestInventory(testProduct2.getId(), 25, ZonedDateTime.now());

        List<Inventory> items = inventoryDao.findByQuantityBetween(10, 20);
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    public void findByDateBetween_whenNoItemsInRange_shouldReturnEmptyList() {
        ZonedDateTime farFuture = ZonedDateTime.now().plusYears(1);
        ZonedDateTime distantFuture = ZonedDateTime.now().plusYears(2);
        createTestInventory(testProduct1.getId(), 10, ZonedDateTime.now());

        List<Inventory> items = inventoryDao.findByDateBetween(farFuture, distantFuture);
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    public void add_and_findById_shouldPersistInventory() {
        Inventory inv = new Inventory();
        inv.setProductId(testProduct1.getId());
        inv.setQuantity(100);
        inventoryDao.add(inv);
        Integer id = inv.getId();

        assertNotNull(id);
        Inventory found = inventoryDao.findById(id);
        assertNotNull(found);
        assertEquals(testProduct1.getId(), found.getProductId());
        assertEquals(100, found.getQuantity());
    }

    @Test
    public void add_withNullEntity_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryDao.add(null);
        });
    }

    @Test
    public void findById_withNullId_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryDao.findById(null);
        });
    }

    @Test
    public void update_shouldModifyInventory() {
        Inventory inv = createTestInventory(testProduct1.getId(), 50, ZonedDateTime.now());
        Integer id = inv.getId();

        Inventory toUpdate = inventoryDao.findById(id);
        assertNotNull(toUpdate);
        toUpdate.setQuantity(99);
        inventoryDao.update(toUpdate);

        Inventory updated = inventoryDao.findById(id);
        assertNotNull(updated);
        assertEquals(99, updated.getQuantity());
    }

    @Test
    public void delete_shouldRemoveInventory() {
        Inventory inv = createTestInventory(testProduct1.getId(), 50, ZonedDateTime.now());
        Integer id = inv.getId();
        assertNotNull(inventoryDao.findById(id));

        inventoryDao.delete(inv);

        Inventory deleted = inventoryDao.findById(id);
        assertNull(deleted);
    }

    @Test
    public void delete_withNullEntity_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryDao.delete(null);
        });
    }

    @Test
    public void getAll_whenItemsExist_shouldReturnList() {
        createTestInventory(testProduct1.getId(), 10, ZonedDateTime.now());
        createTestInventory(testProduct2.getId(), 20, ZonedDateTime.now());

        List<Inventory> allItems = inventoryDao.getAll();
        assertNotNull(allItems);
        assertEquals(2, allItems.size());
    }

    @Test
    public void getAll_whenNoItemsExist_shouldReturnEmptyList() {
        List<Inventory> allItems = inventoryDao.getAll();
        assertNotNull(allItems);
        assertTrue(allItems.isEmpty());
    }

    @Test
    public void findAll_withPagination_shouldReturnPage() {
        createTestInventory(testProduct1.getId(), 10, ZonedDateTime.now());
        createTestInventory(testProduct2.getId(), 20, ZonedDateTime.now());
        createTestInventory(testProduct3.getId(), 30, ZonedDateTime.now());

        Pageable pageable = PageRequest.of(0, 2);
        Page<Inventory> page = inventoryDao.findAll(pageable);

        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    public void findAll_whenNoItems_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Inventory> page = inventoryDao.findAll(pageable);

        assertNotNull(page);
        assertEquals(0, page.getTotalElements());
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    public void findAll_withNullPageable_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            inventoryDao.findAll(null);
        });
    }

    private Product createTestProduct(String barcode, String name) {
        Product p = new Product();
        p.setClientId(testClient.getId());
        p.setName(name);
        p.setBarcode(barcode);
        p.setMrp(10.0);
        productDao.add(p);
        return p;
    }

    private Inventory createTestInventory(Integer productId, Integer quantity, ZonedDateTime dateTime) {
        Inventory inv = new Inventory();
        inv.setProductId(productId);
        inv.setQuantity(quantity);
        inventoryDao.add(inv);
        return inv;
    }
}


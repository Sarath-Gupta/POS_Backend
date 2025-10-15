package com.increff.pos.test;

import com.increff.pos.config.TestConfig;
import com.increff.pos.dao.ClientDao;
import com.increff.pos.entity.Client;
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
@Transactional // Ensures each test is isolated and changes are rolled back
public class ClientDaoTest {

    @Autowired
    private ClientDao clientDao;

    private Client createClient(String name) {
        Client c = new Client();
        c.setClientName(name);
        clientDao.add(c);
        return c;
    }



    @Test
    public void testAddAndFindById() {

        Client client1 = createClient("Acme Corp");
        assertNotNull("Client ID should be generated after add", client1.getId());

        Client found = clientDao.findById(client1.getId());

        assertNotNull("Client should be found by ID", found);
        assertEquals("Client name should match", "Acme Corp", found.getClientName());

        Client notFound = clientDao.findById(-1);
        assertNull("Non-existent client should return null", notFound);
    }

    @Test
    public void testUpdate() {
        Client original = createClient("Original Name");

        original.setClientName("Updated Name");
        clientDao.update(original);

        Client updated = clientDao.findById(original.getId());
        assertEquals("Client name should be updated", "Updated Name", updated.getClientName());
    }

    @Test
    public void testFindAll() {
        createClient("Client-1");
        createClient("Client-2");

        List<Client> list = clientDao.findAll();

        assertEquals("FindAll should return 2 clients", 2, list.size());
        assertTrue("List should contain the expected name",
                list.stream().anyMatch(c -> c.getClientName().equals("Client-2")));
    }


    @Test
    public void testFindByName_Found() {
        createClient("Target Client Name");
        createClient("Another Client");

        Client found = clientDao.findByName("Target Client Name");

        assertNotNull("Client should be found by name", found);
        assertEquals("Client name should match the lookup", "Target Client Name", found.getClientName());
    }

    @Test
    public void testFindByName_NotFound() {
        createClient("UniqueNameXYZ");

        Client notFound = clientDao.findByName("Missing Client");

        assertNull("findByName should return null if client is not found", notFound);
    }
}

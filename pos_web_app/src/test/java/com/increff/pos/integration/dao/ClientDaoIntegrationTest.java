package com.increff.pos.integration.dao;

import com.increff.pos.config.TestDbConfig;
import com.increff.pos.dao.ClientDao;
import com.increff.pos.entity.Client;
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
public class ClientDaoIntegrationTest {

    @Autowired
    private ClientDao clientDao;

    @Test
    public void findByName_whenClientExists_shouldReturnClient() {
        Client client = new Client();
        client.setClientName("find-me");
        clientDao.add(client);

        Client found = clientDao.findByName("find-me");

        assertNotNull(found);
        assertEquals("find-me", found.getClientName());
    }

    @Test
    public void findByName_whenClientDoesNotExist_shouldReturnNull() {
        Client found = clientDao.findByName("does-not-exist");
        assertNull(found);
    }

    @Test
    public void findByName_withNullName_shouldReturnNull() {
        Client found = clientDao.findByName(null);
        assertNull(found);
    }

    @Test
    public void add_and_findById_shouldPersistClient() {
        Client client = new Client();
        client.setClientName("Test Client");
        clientDao.add(client);

        assertNotNull(client.getId());
        Client found = clientDao.findById(client.getId());
        assertNotNull(found);
        assertEquals("Test Client", found.getClientName());
    }

    @Test
    public void findById_withNullId_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            clientDao.findById(null);
        });
    }

    @Test
    public void add_withNullEntity_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            clientDao.add(null);
        });
    }

    @Test
    public void update_shouldModifyExistingClient() {
        Client client = new Client();
        client.setClientName("Original Name");
        clientDao.add(client);
        Integer id = client.getId();

        Client clientToUpdate = clientDao.findById(id);
        assertNotNull(clientToUpdate);
        clientToUpdate.setClientName("Updated Name");
        clientDao.update(clientToUpdate);

        Client found = clientDao.findById(id);
        assertNotNull(found);
        assertEquals("Updated Name", found.getClientName());
    }

    @Test
    public void delete_shouldRemoveClient() {
        Client client = new Client();
        client.setClientName("To Be Deleted");
        clientDao.add(client);
        Integer id = client.getId();

        Client managedClient = clientDao.findById(id);
        assertNotNull(managedClient);

        clientDao.delete(managedClient);

        Client found = clientDao.findById(id);
        assertNull(found);
    }

    @Test
    public void delete_withNullEntity_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            clientDao.delete(null);
        });
    }

    @Test
    public void getAll_shouldReturnAllClients() {
        Client clientA = new Client();
        clientA.setClientName("Client A");
        clientDao.add(clientA);

        Client clientB = new Client();
        clientB.setClientName("Client B");
        clientDao.add(clientB);

        List<Client> clients = clientDao.getAll();
        assertEquals(2, clients.size());
    }

    @Test
    public void getAll_whenNoClients_shouldReturnEmptyList() {
        List<Client> clients = clientDao.getAll();
        assertNotNull(clients);
        assertTrue(clients.isEmpty());
    }

    @Test
    public void findAll_paginated_shouldReturnCorrectPage() {
        Client clientA = new Client();
        clientA.setClientName("Client A");
        clientDao.add(clientA);

        Client clientB = new Client();
        clientB.setClientName("Client B");
        clientDao.add(clientB);

        Client clientC = new Client();
        clientC.setClientName("Client C");
        clientDao.add(clientC);

        Pageable pageable = PageRequest.of(0, 2);
        Page<Client> clientPage = clientDao.findAll(pageable);

        assertEquals(3, clientPage.getTotalElements());
        assertEquals(2, clientPage.getTotalPages());
        assertEquals(2, clientPage.getContent().size());

        Pageable pageable2 = PageRequest.of(1, 2);
        Page<Client> clientPage2 = clientDao.findAll(pageable2);

        assertEquals(1, clientPage2.getContent().size());
    }

    @Test
    public void findAll_whenNoClients_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Client> clientPage = clientDao.findAll(pageable);

        assertNotNull(clientPage);
        assertEquals(0, clientPage.getTotalElements());
        assertTrue(clientPage.getContent().isEmpty());
    }

    @Test
    public void findAll_withNullPageable_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            clientDao.findAll(null);
        });
    }
}


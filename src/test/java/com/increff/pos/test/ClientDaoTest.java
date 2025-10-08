package com.increff.pos.test;

import com.increff.pos.config.DbConfig;
import com.increff.pos.dao.ClientDao;
import com.increff.pos.entity.Client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DbConfig.class)
public class ClientDaoTest {

    @Autowired
    private ClientDao clientRepository;

    @Test
    @Transactional
    public void testInsert() {
        Client client = new Client();
        client.setClientName("testclient1");
        clientRepository.add(client);
        assertNotNull(client.getId());
    }

    @Test
    @Transactional
    public void testFindByName() {
        Client client = new Client();
        client.setClientName("testclient2");
        clientRepository.add(client);

        Client retrievedClient = clientRepository.findByName("testclient2");
        assertNotNull(retrievedClient);
        assertEquals("testclient2", retrievedClient.getClientName());
    }


    @Test
    @Transactional
    public void testFindById() {
        Client client = new Client();
        client.setClientName("Hello");
        clientRepository.add(client);

        Integer id = client.getId();
        Client retrievedClient = clientRepository.findById(id);
        assertNotNull(retrievedClient);
        assertEquals("Hello",retrievedClient.getClientName());
    }
}
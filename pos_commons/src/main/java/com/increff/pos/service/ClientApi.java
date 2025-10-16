package com.increff.pos.service;

import com.increff.pos.dao.ClientDao;
import com.increff.pos.entity.Client;
import com.increff.pos.commons.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class ClientApi extends AbstractApi<Client>{

    @Autowired
    private ClientDao clientDao;

    @PostConstruct
    public void logClassLoader() {
        System.out.println("ClientApi classloader: " + this.getClass().getClassLoader());
    }

    @Transactional
    public void add(Client client) throws ApiException {
        Client existingClient = clientDao.findByName(client.getClientName());
        ifExists(existingClient);
        clientDao.add(client);
    }

    public Client getCheckById(Integer id) throws ApiException {
        Client clientPojo = clientDao.findById(id);
        ifNotExists(clientPojo);
        return clientPojo;
    }

    public Client getById(Integer id) {
        Client client = clientDao.findById(id);
        return client;
    }

    public List<Client> getAll() {
        return clientDao.findAll();
    }

    @Transactional
    public Client update(Integer id, Client client) throws ApiException {
        Client oldClient = clientDao.findById(id);
        Client checkClient = clientDao.findByName(client.getClientName());
        ifExists(checkClient);
        oldClient.setClientName(client.getClientName());
        return oldClient;
    }


}
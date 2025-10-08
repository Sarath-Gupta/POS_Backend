package com.increff.pos.api.service;

import com.increff.pos.dao.ClientDao;
import com.increff.pos.entity.Client;
import com.increff.pos.commons.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ClientApi {

    @Autowired
    private ClientDao clientDao;

    @Transactional
    public void add(Client client) throws ApiException {
        Client existingClient = clientDao.findByName(client.getClientName());
        AbstractApi.ifExists(existingClient);
        clientDao.add(client);
    }

    public Client getCheckById(Integer id) throws ApiException {
        Client clientPojo = clientDao.findById(id);
        AbstractApi.ifNotExists(clientPojo);
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
        if(oldClient.getClientName().equals(client.getClientName())) {
            throw new ApiException("Client name already exists");
        }
        oldClient.setClientName(client.getClientName());
        clientDao.update(oldClient);
        return oldClient;
    }

    @Transactional
    public void delete(Integer id) throws ApiException {
        Client deleteClient = clientDao.findById(id);
        AbstractApi.ifNotExists(deleteClient);
        clientDao.delete(deleteClient);
    }

}
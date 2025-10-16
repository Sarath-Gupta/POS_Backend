package com.increff.pos.dto;

import com.increff.pos.service.ClientApi;
import com.increff.pos.commons.ApiException;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.entity.Client;
import com.increff.pos.util.AbstractMapper;
import com.increff.pos.util.ClientUtil;
import com.increff.pos.util.NormalizeUtil;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Component
public class ClientDto {

    @Autowired
    ClientApi clientApi;
    
    @Autowired
    AbstractMapper mapper;

    @Autowired
    private ValidationUtil validationUtil;

    public ClientData add(ClientForm clientForm) throws ApiException {
        //TODO: first do the validation and then normalize
        validationUtil.validate(clientForm);
        NormalizeUtil.normalize(clientForm);
        Client clientPojo = mapper.convert(clientForm, Client.class);
        clientApi.add(clientPojo);
        return mapper.convert(clientPojo, ClientData.class);
    }

    //TODO: it should return a tsv file as response
    public List<ClientData> addFile(MultipartFile file) throws ApiException {
        List<ClientForm> clientForms = ClientUtil.parseTSV(file);
        List<ClientData> addedClients = new ArrayList<>();
        for (ClientForm clientForm : clientForms) {
            try {
                validationUtil.validate(clientForm);
                NormalizeUtil.normalize(clientForm);
                Client clientPojo = mapper.convert(clientForm, Client.class);
                clientApi.add(clientPojo);
                addedClients.add(mapper.convert(clientPojo, ClientData.class));
            } catch (ApiException e) {
                //TODO: rollback the entire operation if even one of the row is invalid
                System.out.println("Skipping invalid row: " + clientForm + " Reason: " + e.getMessage());
            }
        }
        return addedClients;
    }

    public ClientData getById(Integer id) throws ApiException {
        Client clientPojo = clientApi.getById(id);
        if(clientPojo == null) {
            return null;
        }
        return mapper.convert(clientPojo, ClientData.class);
    }

    public List<ClientData> getAll() {
        List<Client> list = clientApi.getAll();
        return mapper.convert(list, ClientData.class);
    }

    public ClientData update(Integer id, ClientForm clientForm) throws ApiException {
        validationUtil.validate(clientForm);
        NormalizeUtil.normalize(clientForm);
        Client clientPojo = mapper.convert(clientForm, Client.class);
        Client updatedPojo = clientApi.update(id, clientPojo);
        return mapper.convert(updatedPojo, ClientData.class);
    }


}

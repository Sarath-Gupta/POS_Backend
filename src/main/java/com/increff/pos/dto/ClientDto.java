package com.increff.pos.dto;

import com.increff.pos.api.service.ClientApi;
import com.increff.pos.commons.ApiException;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.entity.Client;
import com.increff.pos.util.AbstractMapper;
import com.increff.pos.util.NormalizeUtil;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        NormalizeUtil.normalize(clientForm);
        validationUtil.validate(clientForm);
        Client clientPojo = mapper.convert(clientForm, Client.class);
        clientApi.add(clientPojo);
        return mapper.convert(clientPojo, ClientData.class);
    }

//    public List<ClientData> addFile(MultipartFile file) throws ApiException {
//        List<ClientForm> listClient = ClientUtil.parse(file,Client.class);
//    }

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
        NormalizeUtil.normalize(clientForm);
        validationUtil.validate(clientForm);
        Client clientPojo = mapper.convert(clientForm, Client.class);
        Client updatedPojo = clientApi.update(id, clientPojo);
        return mapper.convert(updatedPojo, ClientData.class);
    }

    public void delete(Integer id) throws ApiException {
        clientApi.delete(id);
    }



}

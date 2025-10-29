package com.increff.pos.dto;

import com.increff.pos.model.form.TsvRowResultClient;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClientDto {

    @Autowired
    ClientApi clientApi;

    @Autowired
    AbstractMapper mapper;

    @Autowired
    private ValidationUtil validationUtil;


    public ClientData add(ClientForm clientForm) throws ApiException {
        validationUtil.validate(clientForm);
        NormalizeUtil.normalize(clientForm);
        Client clientPojo = mapper.convert(clientForm, Client.class);
        clientApi.add(clientPojo);
        return mapper.convert(clientPojo, ClientData.class);
    }

    public String processTsvWithRemarks(MultipartFile file) throws ApiException {
        List<ClientForm> clientForms = ClientUtil.parseTSV(file);
        List<TsvRowResultClient> results = new ArrayList<>();
        boolean allValid = true;
        for (ClientForm clientForm : clientForms) {
            TsvRowResultClient result = new TsvRowResultClient(clientForm);
            try {
                NormalizeUtil.normalize(clientForm);
                validationUtil.validate(clientForm);
                Client client = mapper.convert(clientForm, Client.class);
                clientApi.add(client);

            } catch (ApiException e) {
                result.setRemarks(e.getMessage());
                allValid = false;
            }
            results.add(result);
        }
        if (allValid) {
            List<Client> clientPojos = results.stream()
                    .map(r -> mapper.convert(r.form, Client.class))
                    .collect(Collectors.toList());

            try {
                for (Client clientPojo : clientPojos) {
                    clientApi.add(clientPojo);
                }
            } catch (ApiException e) {
                throw new ApiException("Critical Database/Service Error during insertion: " + e.getMessage());
            }
        }
        return convertResultsToTsvString(results);
    }

    private String convertResultsToTsvString(List<TsvRowResultClient> results) {
        StringBuilder tsvContent = new StringBuilder();
        tsvContent.append("clientName\tRemarks\n");
        for (TsvRowResultClient result : results) {
            String outputRemarks = result.isValid() ? "" : result.remarks;
            tsvContent.append(result.form.getClientName()).append("\t")
                    .append(outputRemarks).append("\n");
        }
        return tsvContent.toString();
    }


    public ClientData getById(Integer id) throws ApiException {
        Client clientPojo = clientApi.getById(id);
        if(clientPojo == null) {
            return null;
        }
        return mapper.convert(clientPojo, ClientData.class);
    }

    public Page<ClientData> getAll(Pageable pageable) {
        Page<Client> clientPage = clientApi.getAll(pageable);
        return clientPage.map(client -> mapper.convert(client, ClientData.class));
    }

    public ClientData update(Integer id, ClientForm clientForm) throws ApiException {
        validationUtil.validate(clientForm);
        NormalizeUtil.normalize(clientForm);
        Client clientPojo = mapper.convert(clientForm, Client.class);
        Client updatedPojo = clientApi.update(id, clientPojo);
        return mapper.convert(updatedPojo, ClientData.class);
    }
}
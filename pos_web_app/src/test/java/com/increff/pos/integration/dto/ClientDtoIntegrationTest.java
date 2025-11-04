package com.increff.pos.integration.dto;

import com.increff.pos.commons.ApiException;
import com.increff.pos.config.SpringConfig;
import com.increff.pos.dto.ClientDto;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.form.ClientForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpringConfig.class})
@WebAppConfiguration
@Transactional
public class ClientDtoIntegrationTest {

    @Autowired
    private ClientDto clientDto;

    private ClientForm createClientForm(String name) {
        ClientForm form = new ClientForm();
        form.setClientName(name);
        return form;
    }

    @Test
    public void testAddClient_Success() throws ApiException {
        ClientForm form = createClientForm("Test Client");
        ClientData data = clientDto.add(form);

        assertNotNull(data);
        assertNotNull(data.getId());
        assertEquals("test client", data.getClientName());
    }

    @Test
    public void testAddClient_InvalidName_ShouldThrow() {
        ClientForm form = createClientForm("  ");
        ApiException e = assertThrows(ApiException.class, () -> clientDto.add(form));
        assertEquals("Client Name cannot be empty", e.getMessage());
    }

    @Test
    public void testAddClient_Duplicate_ShouldThrow() throws ApiException {
        clientDto.add(createClientForm("Duplicate Client"));

        ClientForm duplicateForm = createClientForm("Duplicate Client");
        ApiException e = assertThrows(ApiException.class, () -> clientDto.add(duplicateForm));
        assertEquals("Client already exists", e.getMessage());
    }

    @Test
    public void testGetById_Exists() throws ApiException {
        ClientData addedClient = clientDto.add(createClientForm("Get Me"));
        ClientData foundClient = clientDto.getById(addedClient.getId());

        assertNotNull(foundClient);
        assertEquals(addedClient.getId(), foundClient.getId());
        assertEquals("get me", foundClient.getClientName());
    }

    @Test
    public void testGetById_NotExists() throws ApiException {
        ClientData foundClient = clientDto.getById(999);
        assertNull(foundClient);
    }

    @Test
    public void testUpdate_Success() throws ApiException {
        ClientData client = clientDto.add(createClientForm("Original Name"));
        ClientForm updateForm = createClientForm("Updated Name");

        ClientData updatedClient = clientDto.update(client.getId(), updateForm);

        assertNotNull(updatedClient);
        assertEquals(client.getId(), updatedClient.getId());
        assertEquals("updated name", updatedClient.getClientName());

        ClientData verifiedClient = clientDto.getById(client.getId());
        assertEquals("updated name", verifiedClient.getClientName());
    }
}

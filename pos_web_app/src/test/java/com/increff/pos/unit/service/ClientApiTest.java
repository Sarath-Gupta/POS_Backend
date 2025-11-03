package com.increff.pos.unit.service;

import com.increff.pos.dao.ClientDao;
import com.increff.pos.entity.Client;
import com.increff.pos.commons.ApiException;
import com.increff.pos.factory.ClientFactory;
import com.increff.pos.service.ClientApi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ClientApiTest {

    @Mock
    private ClientDao clientDao;

    @InjectMocks
    private ClientApi clientService;

    @Test
    @DisplayName("add() should save client when name is unique")
    public void add_validClient_shouldSucceed() throws ApiException {
        Client newClient = ClientFactory.mockNewObject("new-client");
        when(clientDao.findByName("new-client")).thenReturn(null);
        clientService.add(newClient);
        verify(clientDao, times(1)).findByName("new-client");
        verify(clientDao, times(1)).add(newClient);
    }

    @Test
    @DisplayName("add() should throw ApiException when name is duplicate")
    public void add_duplicateName_shouldThrowApiException() {
        Client newClient = ClientFactory.mockNewObject("duplicate-name");
        Client existingClient = ClientFactory.mockPersistedObject(1, "duplicate-name");
        when(clientDao.findByName("duplicate-name")).thenReturn(existingClient);
        Exception e = assertThrows(ApiException.class, () -> {
            clientService.add(newClient);
        });
        verify(clientDao, times(1)).findByName("duplicate-name");
        verify(clientDao, never()).add(any(Client.class));
    }

    @Test
    @DisplayName("add() should throw NullPointerException when client is null")
    public void add_nullClient_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            clientService.add(null);
        });
        verify(clientDao, never()).findByName(anyString());
        verify(clientDao, never()).add(any(Client.class));
    }

    @Test
    @DisplayName("getById() should return client when ID exists")
    public void getById_existingId_shouldReturnClient() {
        Integer clientId = 1;
        Client expectedClient = ClientFactory.mockPersistedObject(clientId, "test-client");
        when(clientDao.findById(clientId)).thenReturn(expectedClient);
        Client actualClient = clientService.getById(clientId);
        assertNotNull(actualClient);
        assertEquals(clientId, actualClient.getId());
        verify(clientDao, times(1)).findById(clientId);
    }

    @Test
    @DisplayName("getById() should return null when ID does not exist")
    public void getById_nonExistentId_shouldReturnNull() {
        Integer clientId = 999;
        when(clientDao.findById(clientId)).thenReturn(null);
        Client actualClient = clientService.getById(clientId);
        assertNull(actualClient);
        verify(clientDao, times(1)).findById(clientId);
    }

    @Test
    @DisplayName("getAll() should return a page of clients")
    public void getAll_clientsFound_shouldReturnPaginatedResult() {
        Pageable pageable = ClientFactory.createPageable();
        List<Client> clientList = ClientFactory.createClientList();
        Page<Client> clientPage = ClientFactory.createClientPage(clientList, pageable);
        when(clientDao.findAll(pageable)).thenReturn(clientPage);
        Page<Client> result = clientService.getAll(pageable);
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("Client One", result.getContent().get(0).getClientName());
        verify(clientDao, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("getAll() should return empty page when no clients found")
    public void getAll_noClientsFound_shouldReturnEmptyResult() {
        Pageable pageable = ClientFactory.createPageable();
        when(clientDao.findAll(pageable)).thenReturn(Page.empty(pageable));
        Page<Client> result = clientService.getAll(pageable);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(clientDao, times(1)).findAll(pageable);
    }


    @Test
    @DisplayName("update() should update client when name is new")
    public void update_validUpdate_shouldSucceed() throws ApiException {
        Integer clientId = 1;
        Client updateData = ClientFactory.mockNewObject("updated-name");
        Client existingClient = ClientFactory.mockPersistedObject(clientId, "original-name");

        when(clientDao.findById(clientId)).thenReturn(existingClient);
        when(clientDao.findByName("updated-name")).thenReturn(null); // No duplicate

        // When
        Client updatedClient = clientService.update(clientId, updateData);

        // Then
        assertNotNull(updatedClient);
        assertEquals(clientId, updatedClient.getId());
        // Verify the object in memory was mutated
        assertEquals("updated-name", updatedClient.getClientName());
        verify(clientDao, times(1)).findById(clientId);
        verify(clientDao, times(1)).findByName("updated-name");
    }

    @Test
    @DisplayName("update() should throw ApiException when client ID not found")
    public void update_clientNotFound_shouldThrowApiException() {
        Integer clientId = 999;
        Client updateData = ClientFactory.mockNewObject("updated-name");
        when(clientDao.findById(clientId)).thenReturn(null);
        ApiException e = assertThrows(ApiException.class, () -> {
            clientService.update(clientId, updateData);
        });

        assertTrue(e.getMessage().contains("doesn't exist"));

        verify(clientDao, times(1)).findById(clientId);
        verify(clientDao, never()).findByName(anyString());
    }

    @Test
    @DisplayName("update() should throw ApiException when new name duplicates another client")
    public void update_duplicateName_shouldThrowApiException() {
        Integer clientId = 1;
        Client updateData = ClientFactory.mockNewObject("duplicate-name");
        Client existingClient = ClientFactory.mockPersistedObject(clientId, "original-name");

        Client duplicateClient = ClientFactory.mockPersistedObject(2, "duplicate-name");

        when(clientDao.findById(clientId)).thenReturn(existingClient);
        when(clientDao.findByName("duplicate-name")).thenReturn(duplicateClient);

        assertThrows(ApiException.class, () -> {
            clientService.update(clientId, updateData);
        });

        verify(clientDao, times(1)).findById(clientId);
        verify(clientDao, times(1)).findByName("duplicate-name");
        assertEquals("original-name", existingClient.getClientName());
    }

    @Test
    @DisplayName("update() should throw ApiException when name is unchanged (due to logic bug)")
    public void update_sameName_shouldThrowApiException_BUG() {
        Integer clientId = 1;
        Client updateData = ClientFactory.mockNewObject("same-name");
        Client existingClient = ClientFactory.mockPersistedObject(clientId, "same-name");

        when(clientDao.findById(clientId)).thenReturn(existingClient);
        when(clientDao.findByName("same-name")).thenReturn(existingClient);
        assertThrows(ApiException.class, () -> {
            clientService.update(clientId, updateData);
        }, "Service logic incorrectly flags a client as its own duplicate");

        verify(clientDao, times(1)).findById(clientId);
        verify(clientDao, times(1)).findByName("same-name");
    }
}


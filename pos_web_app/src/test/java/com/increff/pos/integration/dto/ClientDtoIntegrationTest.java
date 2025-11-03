//package com.increff.pos.integration.dto;
//
//import com.increff.pos.config.SpringConfig;
//import com.increff.pos.config.SwaggerConfig;
//import com.increff.pos.config.TestDbConfig;
//import com.increff.pos.config.WebInitializer;
//import com.increff.pos.dto.ClientDto;
//import com.increff.pos.model.data.ClientData;
//import com.increff.pos.model.form.ClientForm;
//import com.increff.pos.commons.ApiException;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes = { TestDbConfig.class, SpringConfig.class, WebInitializer.class, SwaggerConfig.class})
//@Transactional
//public class ClientDtoIntegrationTest {
//
//    @Autowired
//    private ClientDto clientDto;
//
//
//    @Test
//    public void testAdd_HappyPath() throws ApiException {
//        ClientForm form = createClientForm("  Test Client  ");
//        ClientData data = clientDto.add(form);
//
//        assertNotNull(data);
//        assertNotNull(data.getId());
//        assertEquals("test client", data.getClientName()); // Check normalization
//    }
//
//    @Test
//    public void testAdd_ValidationFail_BlankName() {
//        ClientForm form = createClientForm(""); // Invalid
//        ApiException ex = assertThrows(ApiException.class, () -> clientDto.add(form));
//        assertTrue(ex.getMessage().toLowerCase().contains("clientname cannot be blank"));
//    }
//
//    @Test
//    public void testAdd_DuplicateName() throws ApiException {
//        clientDto.add(createClientForm("Duplicate Client")); // Add first client
//
//        // Add second client with same name
//        ClientForm form2 = createClientForm("Duplicate Client");
//        ApiException ex = assertThrows(ApiException.class, () -> clientDto.add(form2));
//        assertTrue(ex.getMessage().toLowerCase().contains("client with this name already exists"));
//    }
//
//    // endregion
//
//    // region getById(Integer id) Tests
//
//    @Test
//    public void testGetById_HappyPath() throws ApiException {
//        ClientData saved = clientDto.add(createClientForm("Client A"));
//        ClientData found = clientDto.getById(saved.getId());
//
//        assertNotNull(found);
//        assertEquals(saved.getId(), found.getId());
//        assertEquals("client a", found.getClientName());
//    }
//
//    @Test
//    public void testGetById_NotFound() throws ApiException {
//        // Per the DTO code, it returns null if the API returns null
//        ClientData found = clientDto.getById(-99);
//        assertNull(found);
//    }
//
//    // endregion
//
//    // region getAll(Pageable pageable) Tests
//
//    @Test
//    public void testGetAll_HappyPath() throws ApiException {
//        ClientData clientA = clientDto.add(createClientForm("Client A"));
//        ClientData clientB = clientDto.add(createClientForm("Client B"));
//
//        Pageable pageable = PageRequest.of(0, 5);
//        Page<ClientData> page = clientDto.getAll(pageable);
//
//        assertEquals(2, page.getTotalElements());
//        assertEquals(1, page.getTotalPages());
//        List<String> names = page.getContent().stream()
//                .map(ClientData::getClientName)
//                .collect(Collectors.toList());
//
//        assertTrue(names.contains(clientA.getClientName()));
//        assertTrue(names.contains(clientB.getClientName()));
//    }
//
//    @Test
//    public void testGetAll_Empty() {
//        Pageable pageable = PageRequest.of(0, 5);
//        Page<ClientData> page = clientDto.getAll(pageable);
//
//        assertEquals(0, page.getTotalElements());
//        assertTrue(page.getContent().isEmpty());
//    }
//
//    // endregion
//
//    // region update(Integer id, ClientForm) Tests
//
//    @Test
//    public void testUpdate_HappyPath() throws ApiException {
//        ClientData saved = clientDto.add(createClientForm("Original Name"));
//        ClientForm updateForm = createClientForm("  Updated Name  ");
//
//        ClientData updated = clientDto.update(saved.getId(), updateForm);
//
//        assertNotNull(updated);
//        assertEquals(saved.getId(), updated.getId());
//        assertEquals("updated name", updated.getClientName()); // Check normalization
//    }
//
//    @Test
//    public void testUpdate_NotFound() {
//        ClientForm updateForm = createClientForm("Test");
//        Integer nonExistentId = -99;
//
//        // Assumes clientApi.update() throws an exception if ID not found
//        ApiException ex = assertThrows(ApiException.class, () -> clientDto.update(nonExistentId, updateForm));
//        assertTrue(ex.getMessage().toLowerCase().contains("client not found"));
//    }
//
//    @Test
//    public void testUpdate_ValidationFail() throws ApiException {
//        ClientData saved = clientDto.add(createClientForm("Original Name"));
//        ClientForm updateForm = createClientForm(""); // Invalid
//
//        ApiException ex = assertThrows(ApiException.class, () -> clientDto.update(saved.getId(), updateForm));
//        assertTrue(ex.getMessage().toLowerCase().contains("clientname cannot be blank"));
//    }
//
//    @Test
//    public void testUpdate_DuplicateName() throws ApiException {
//        clientDto.add(createClientForm("Client A"));
//        ClientData clientB = clientDto.add(createClientForm("Client B"));
//
//        ClientForm updateForm = createClientForm("Client A"); // Duplicate name
//
//        ApiException ex = assertThrows(ApiException.class, () -> clientDto.update(clientB.getId(), updateForm));
//        assertTrue(ex.getMessage().toLowerCase().contains("client with this name already exists"));
//    }
//
//
//    @Test
//    public void testProcessTsv_SomeInvalid_AddsValidClients() throws ApiException {
//        String tsvContent = "clientName\n"
//                + "Client A\n"
//                + "  \n"
//                + "Client B\n";
//        MockMultipartFile file = new MockMultipartFile("file", "clients.tsv", "text/tab-separated-values", tsvContent.getBytes());
//
//        String remarks = clientDto.processTsvWithRemarks(file);
//
//        // Assert on the returned remarks string
//        assertNotNull(remarks);
//        assertTrue(remarks.contains("\tclientName cannot be blank\n"));
//        assertFalse(remarks.contains("Client A"));
//    }
//
//    @Test
//    public void testProcessTsv_AllValid_ThrowsException() {
//        // This test exposes the bug in the DTO's logic
//        String tsvContent = "clientName\n"
//                + "Client A\n"
//                + "Client B\n";
//        MockMultipartFile file = new MockMultipartFile("file", "clients.tsv", "text/tab-separated-values", tsvContent.getBytes());
//
//        // The method will add "Client A", then "Client B" in the loop.
//        // Then it will enter the if(allValid) block and try to add "Client A" *again*.
//        ApiException ex = assertThrows(ApiException.class, () -> clientDto.processTsvWithRemarks(file));
//        assertTrue(ex.getMessage().toLowerCase().contains("client with this name already exists"));
//    }
//
//    @Test
//    public void testProcessTsv_DuplicateInFileAndDb() throws ApiException {
//        clientDto.add(createClientForm("Client A")); // Already in DB
//
//        String tsvContent = "clientName\n"
//                + "Client B\n" // Valid
//                + "Client A\n"; // Invalid (duplicate)
//        MockMultipartFile file = new MockMultipartFile("file", "clients.tsv", "text/tab-separated-values", tsvContent.getBytes());
//
//        String remarks = clientDto.processTsvWithRemarks(file);
//
//        // Assert on the returned remarks string
//        assertTrue(remarks.contains("Client A\tclient with this name already exists\n"));
//        assertFalse(remarks.contains("Client B\t"));
//    }
//
//    // endregion
//
//    // region Helper Methods
//
//    private ClientForm createClientForm(String name) {
//        ClientForm form = new ClientForm();
//        form.setClientName(name);
//        return form;
//    }
//
//    // endregion
//}
//

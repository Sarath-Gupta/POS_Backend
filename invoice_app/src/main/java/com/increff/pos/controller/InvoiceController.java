// File: invoice_app/src/main/java/com/increff/pos/controller/InvoiceController.java

package com.increff.pos.controller;

import com.increff.pos.flow.InvoiceFlow;
import com.increff.pos.commons.ApiException;
import com.increff.pos.model.data.InvoiceRequest;
import com.increff.pos.model.data.InvoiceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// ... other imports ...

@RestController
@RequestMapping("/api/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceFlow invoiceFlow;

    @PostMapping(value = "/generate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public InvoiceData generateInvoice(@RequestBody InvoiceRequest invoiceRequest) throws ApiException {
        InvoiceData invoiceData = invoiceFlow.generateInvoice(invoiceRequest);
        return invoiceData;
    }
}
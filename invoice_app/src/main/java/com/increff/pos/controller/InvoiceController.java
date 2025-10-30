package com.increff.pos.controller;

import com.increff.pos.dto.InvoiceDto;
import com.increff.pos.commons.ApiException;
import com.increff.pos.model.data.InvoiceRequest;
import com.increff.pos.model.data.InvoiceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceDto invoiceDto;

    @PostMapping(value = "/generate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public InvoiceData generateInvoice(@RequestBody InvoiceRequest invoiceRequest) throws ApiException {
        InvoiceData invoiceData = invoiceDto.generateInvoice(invoiceRequest);
        return invoiceData;
    }
}
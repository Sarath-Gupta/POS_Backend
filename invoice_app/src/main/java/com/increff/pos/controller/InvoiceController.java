package com.increff.pos.controller;

import com.increff.pos.commons.ApiException;
import com.increff.pos.dto.InvoiceDto;
import com.increff.pos.model.data.InvoiceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InvoiceController {

    @Autowired
    private InvoiceDto invoiceDto;

    @RequestMapping(value = "/api/invoices/{orderId}", method = RequestMethod.POST)
    public InvoiceData generateInvoice(@PathVariable("orderId") Integer orderId) throws ApiException {
        return invoiceDto.generateInvoice(orderId);
    }
}
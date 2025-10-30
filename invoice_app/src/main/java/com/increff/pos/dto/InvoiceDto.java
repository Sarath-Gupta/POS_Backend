package com.increff.pos.dto;

import com.increff.pos.commons.ApiException;
import com.increff.pos.flow.InvoiceFlow;
import com.increff.pos.model.data.InvoiceData;
import com.increff.pos.model.data.InvoiceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvoiceDto {

    @Autowired
    private InvoiceFlow invoiceFlow;

    public InvoiceData generateInvoice(InvoiceRequest invoiceRequest) throws ApiException {
        return invoiceFlow.generateInvoice(invoiceRequest);
    }
}

package com.increff.pos.dto;


import com.increff.pos.commons.ApiException;
import com.increff.pos.flow.InvoiceFlow;
import com.increff.pos.model.data.InvoiceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvoiceDto {

    @Autowired
    private InvoiceFlow invoiceFlow;

    public InvoiceData generateInvoice(Integer orderId) throws ApiException {
        return invoiceFlow.generateInvoice(orderId);
    }
}

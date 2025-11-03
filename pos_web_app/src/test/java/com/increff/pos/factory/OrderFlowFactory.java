package com.increff.pos.factory;

import com.increff.pos.model.data.InvoiceData;
import com.increff.pos.model.data.InvoiceRequest;
import com.increff.pos.model.data.OrderData;
import org.instancio.Instancio;
import org.instancio.Model;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import static org.instancio.Select.field;

public final class OrderFlowFactory {

    private OrderFlowFactory() {
    }

    private static final Model<OrderData> NEW_ORDER_DATA_MODEL = Instancio.of(OrderData.class)
            .set(field(OrderData::getId), null)
            .set(field(OrderData::getCreatedAt), ZonedDateTime.now())
            .toModel();


    private static final Model<OrderData> PERSISTED_ORDER_DATA_MODEL = Instancio.of(OrderData.class)
            .generate(field(OrderData::getId), gen -> gen.ints().min(1))
            .set(field(OrderData::getCreatedAt), ZonedDateTime.now())
            .toModel();

    public static OrderData mockPersistedOrderData() {
        return Instancio.of(PERSISTED_ORDER_DATA_MODEL).create();
    }

    public static OrderData mockPersistedOrderData(Integer id) {
        return Instancio.of(PERSISTED_ORDER_DATA_MODEL)
                .set(field(OrderData::getId), id)
                .create();
    }

    public static InvoiceRequest mockEmptyInvoiceRequest() {
        return Instancio.of(InvoiceRequest.class)
                .set(field(InvoiceRequest::getOrderItemData), new ArrayList<>())
                .set(field(InvoiceRequest::getProductNames), new ArrayList<>())
                .create();
    }


    public static InvoiceData mockInvoiceData(Integer orderId, String base64Pdf) {
        InvoiceData invoiceData = new InvoiceData();
        invoiceData.setOrderId(orderId);
        invoiceData.setBase64Pdf(base64Pdf);
        return invoiceData;
    }
}

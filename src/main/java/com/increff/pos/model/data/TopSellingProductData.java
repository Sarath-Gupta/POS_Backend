package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopSellingProductData {
    private String productName;
    private String barcode;
    private String clientName;
    private Long totalQuantitySold;
    private Double totalRevenue;
    private Integer currentStock;
}

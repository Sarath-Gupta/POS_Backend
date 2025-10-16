package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardSummaryData {
    private Double totalRevenue;
    private Long totalOrders;
    private Long totalProducts;
    private Long totalClients;
    private Double averageOrderValue;
    private Long lowStockItems;
    private Double inventoryValue;
}

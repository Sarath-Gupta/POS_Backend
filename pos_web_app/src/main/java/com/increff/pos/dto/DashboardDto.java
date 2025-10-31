package com.increff.pos.dto;

import com.increff.pos.service.DashboardApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DashboardDto {

    @Autowired
    DashboardApi dashboardApi;

    public Long getTotalClients() {
        return dashboardApi.getTotalClients();
    }

    public Long getTotalProducts() {
        return dashboardApi.getTotalProducts();
    }

    public Long getTotalOrders() {
        return dashboardApi.getTotalOrders();
    }

    public Double getTotalRevenue() {
        return dashboardApi.getTotalRevenue();
    }
}

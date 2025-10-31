package com.increff.pos.service;

import com.increff.pos.dao.DashboardDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardApi {

    @Autowired
    DashboardDao dashboardDao;

    public Long getTotalClients() {
        return dashboardDao.getTotalClients();
    }

    public Long getTotalProducts() {
        return dashboardDao.getTotalProducts();
    }

    public Double getTotalRevenue() {
        return dashboardDao.getTotalRevenue();
    }

    public Long getTotalOrders() {
        return dashboardDao.getTotalOrders();
    }
}

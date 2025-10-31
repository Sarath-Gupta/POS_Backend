package com.increff.pos.controller;

import com.increff.pos.dto.DashboardDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {

    @Autowired
    DashboardDto dashboardDto;

    @RequestMapping(value = "/clients", method = RequestMethod.GET)
    public Long getTotalClients() {
        return dashboardDto.getTotalClients();
    }

    @RequestMapping(value = "/products", method = RequestMethod.GET)
    public Long getTotalProducts() {
        return dashboardDto.getTotalProducts();
    }

    @RequestMapping(value = "/revenue", method = RequestMethod.GET)
    public Double getTotalRevenue() {
        return dashboardDto.getTotalRevenue();
    }

    @RequestMapping(value = "/orders", method = RequestMethod.GET)
    public Long getTotalOrders() {
        return dashboardDto.getTotalOrders();
    }
}

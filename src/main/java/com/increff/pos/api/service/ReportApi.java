package com.increff.pos.api.service;

import com.increff.pos.dao.ReportDao;
import com.increff.pos.model.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.Tuple;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class ReportApi {

    @Autowired
    private ReportDao reportDao;

    @Transactional(readOnly = true)
    public DashboardSummaryData generateDashboardSummary() {
        DashboardSummaryData data = new DashboardSummaryData();
        List<Tuple> salesResults = reportDao.getSalesMetrics();
        if (!salesResults.isEmpty()) {
            Tuple salesRow = salesResults.get(0);
            Double totalRevenue = salesRow.get("totalRevenue", Double.class);
            Long totalOrders = salesRow.get("totalOrders", Long.class);
            
            data.setTotalRevenue(totalRevenue);
            data.setTotalOrders(totalOrders);

            if (totalOrders != null && totalOrders > 0 && totalRevenue != null) {
                data.setAverageOrderValue(totalRevenue / totalOrders);
            } else {
                data.setAverageOrderValue(0.0);
            }
        } else {
            data.setTotalRevenue(0.0);
            data.setTotalOrders(0L);
            data.setAverageOrderValue(0.0);
        }

        List<Tuple> inventoryResults = reportDao.getInventoryMetrics();
        if (!inventoryResults.isEmpty()) {
            Tuple inventoryRow = inventoryResults.get(0);
            data.setTotalProducts(inventoryRow.get("totalProducts", Long.class));
            data.setTotalClients(inventoryRow.get("totalClients", Long.class));
            data.setLowStockItems(inventoryRow.get("lowStockItems", Long.class));
            data.setInventoryValue(inventoryRow.get("inventoryValue", Double.class));
        } else {
            data.setTotalProducts(0L);
            data.setTotalClients(0L);
            data.setLowStockItems(0L);
            data.setInventoryValue(0.0);
        }
        
        return data;
    }

    @Transactional(readOnly = true)
    public List<TopSellingProductData> generateTopSellingProducts(Integer limit) {
        List<Tuple> results = reportDao.getTopSellingProducts(limit);
        List<TopSellingProductData> reportDataList = new ArrayList<>();

        for (Tuple row : results) {
            TopSellingProductData data = new TopSellingProductData();
            data.setProductName(row.get("productName", String.class));
            data.setBarcode(row.get("barcode", String.class));
            data.setClientName(row.get("clientName", String.class));
            data.setTotalQuantitySold(row.get("totalQuantitySold", Long.class));
            data.setTotalRevenue(row.get("totalRevenue", Double.class));
            data.setCurrentStock(row.get("currentStock", Integer.class));
            reportDataList.add(data);
        }
        return reportDataList;
    }

    @Transactional(readOnly = true)
    public List<RevenueTrendData> generateRevenueTrend(String period) {
        List<Tuple> results = reportDao.getRevenueTrend(period);
        List<RevenueTrendData> reportDataList = new ArrayList<>();

        for (Tuple row : results) {
            RevenueTrendData data = new RevenueTrendData();

            ZonedDateTime createdAt = row.get("period", ZonedDateTime.class);
            String formattedPeriod;
            if ("monthly".equals(period)) {
                formattedPeriod = createdAt.getYear() + "-" + String.format("%02d", createdAt.getMonthValue());
            } else {
                formattedPeriod = createdAt.toLocalDate().toString();
            }
            
            data.setPeriod(formattedPeriod);
            data.setRevenue(row.get("revenue", Double.class));
            data.setOrderCount(row.get("orderCount", Long.class));
            data.setItemsSold(row.get("itemsSold", Long.class));
            reportDataList.add(data);
        }
        return reportDataList;
    }
}

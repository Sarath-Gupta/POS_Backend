package com.increff.pos.dto;

import com.increff.pos.model.data.*;
import com.increff.pos.service.ReportApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.ZonedDateTime;
import java.util.List;


@Component
public class ReportDto {
    
    @Autowired
    private ReportApi reportApi;

    public DashboardSummaryData getDashboardSummary() {
        return reportApi.generateDashboardSummary();
    }

    public List<TopSellingProductData> getTopSellingProducts(Integer limit) {
        return reportApi.generateTopSellingProducts(limit);
    }

    public List<RevenueTrendData> getRevenueTrend(String period) {
        return reportApi.generateRevenueTrend(period);
    }
}

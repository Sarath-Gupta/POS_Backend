package com.increff.pos.controller;

import com.increff.pos.dto.ReportDto;
import com.increff.pos.model.data.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.ZonedDateTime;
import java.util.List;


@RestController
@RequestMapping("/reports")
public class ReportController {


    @Autowired
    private ReportDto reportDto;

    @RequestMapping(path = "/dashboard/summary", method = RequestMethod.GET)
    @ApiOperation(value = "Get dashboard summary statistics")
    public DashboardSummaryData getDashboardSummary() {
        return reportDto.getDashboardSummary();
    }

    @RequestMapping(path = "/dashboard/top-products", method = RequestMethod.GET)
    @ApiOperation(value = "Get top selling products")
    public List<TopSellingProductData> getTopSellingProducts(
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return reportDto.getTopSellingProducts(limit);
    }

    @RequestMapping(path = "/dashboard/revenue-trend", method = RequestMethod.GET)
    @ApiOperation(value = "Get revenue trend data")
    public List<RevenueTrendData> getRevenueTrend(
            @RequestParam(value = "period", defaultValue = "daily") String period) {
        return reportDto.getRevenueTrend(period);
    }
}
package com.increff.pos.unit.service; // Or your unit test package

import com.increff.pos.dao.ReportDao;
import com.increff.pos.factory.ReportFactory;
import com.increff.pos.model.data.DashboardSummaryData;
import com.increff.pos.model.data.RevenueTrendData;
import com.increff.pos.model.data.TopSellingProductData;
import com.increff.pos.service.ReportApi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.Tuple;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the ReportApi class.
 * This class mocks the ReportDao to isolate and test all business logic,
 * data transformation, and calculations.
 */
@ExtendWith(MockitoExtension.class)
public class ReportApiTest {

    @Mock
    private ReportDao reportDao;

    @InjectMocks
    private ReportApi reportApi; // The service class we are testing

    // --- generateDashboardSummary() Tests ---

    @Test
    @DisplayName("generateDashboardSummary() should return full summary when data exists")
    public void generateDashboardSummary_dataExists_shouldReturnFullSummary() {
        // Given
        Tuple salesTuple = ReportFactory.createSalesMetricsTuple(1000.0, 10L);
        Tuple inventoryTuple = ReportFactory.createInventoryMetricsTuple(50L, 20L, 5L, 25000.0);

        when(reportDao.getSalesMetrics()).thenReturn(Collections.singletonList(salesTuple));
        when(reportDao.getInventoryMetrics()).thenReturn(Collections.singletonList(inventoryTuple));

        // When
        DashboardSummaryData data = reportApi.generateDashboardSummary();

        // Then
        assertNotNull(data);
        assertEquals(1000.0, data.getTotalRevenue());
        assertEquals(10L, data.getTotalOrders());
        assertEquals(100.0, data.getAverageOrderValue()); // 1000 / 10
        assertEquals(50L, data.getTotalProducts());
        assertEquals(20L, data.getTotalClients());
        assertEquals(5L, data.getLowStockItems());
        assertEquals(25000.0, data.getInventoryValue());

        verify(reportDao, times(1)).getSalesMetrics();
        verify(reportDao, times(1)).getInventoryMetrics();
    }

    @Test
    @DisplayName("generateDashboardSummary() should return zeros when no data exists")
    public void generateDashboardSummary_noData_shouldReturnZeros() {
        // Given
        when(reportDao.getSalesMetrics()).thenReturn(Collections.emptyList());
        when(reportDao.getInventoryMetrics()).thenReturn(Collections.emptyList());

        // When
        DashboardSummaryData data = reportApi.generateDashboardSummary();

        // Then
        assertNotNull(data);
        assertEquals(0.0, data.getTotalRevenue());
        assertEquals(0L, data.getTotalOrders());
        assertEquals(0.0, data.getAverageOrderValue());
        assertEquals(0L, data.getTotalProducts());
        assertEquals(0L, data.getTotalClients());
        assertEquals(0L, data.getLowStockItems());
        assertEquals(0.0, data.getInventoryValue());
    }

    @Test
    @DisplayName("generateDashboardSummary() should handle zero orders gracefully")
    public void generateDashboardSummary_zeroOrders_shouldNotDivideByZero() {
        // Given
        Tuple salesTuple = ReportFactory.createSalesMetricsTuple(1000.0, 0L); // 0 orders
        when(reportDao.getSalesMetrics()).thenReturn(Collections.singletonList(salesTuple));
        when(reportDao.getInventoryMetrics()).thenReturn(Collections.emptyList());

        // When
        DashboardSummaryData data = reportApi.generateDashboardSummary();

        // Then
        assertNotNull(data);
        assertEquals(1000.0, data.getTotalRevenue());
        assertEquals(0L, data.getTotalOrders());
        assertEquals(0.0, data.getAverageOrderValue()); // Should default to 0.0
    }

    // --- generateTopSellingProducts() Tests ---

    @Test
    @DisplayName("generateTopSellingProducts() should return transformed list")
    public void generateTopSellingProducts_dataExists_shouldReturnList() {
        // Given
        Integer limit = 5;
        Tuple productTuple = ReportFactory.createTopSellingProductTuple(
                "Test Product", "test-barcode", "Test Client", 100L, 5000.0, 50
        );
        when(reportDao.getTopSellingProducts(limit)).thenReturn(Collections.singletonList(productTuple));

        // When
        List<TopSellingProductData> result = reportApi.generateTopSellingProducts(limit);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        TopSellingProductData data = result.get(0);
        assertEquals("Test Product", data.getProductName());
        assertEquals("test-barcode", data.getBarcode());
        assertEquals("Test Client", data.getClientName());
        assertEquals(100L, data.getTotalQuantitySold());
        assertEquals(5000.0, data.getTotalRevenue());
        assertEquals(50, data.getCurrentStock());

        verify(reportDao, times(1)).getTopSellingProducts(limit);
    }

    @Test
    @DisplayName("generateTopSellingProducts() should return empty list when no data")
    public void generateTopSellingProducts_noData_shouldReturnEmptyList() {
        // Given
        Integer limit = 5;
        when(reportDao.getTopSellingProducts(limit)).thenReturn(Collections.emptyList());

        // When
        List<TopSellingProductData> result = reportApi.generateTopSellingProducts(limit);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reportDao, times(1)).getTopSellingProducts(limit);
    }

    // --- generateRevenueTrend() Tests ---

    @Test
    @DisplayName("generateRevenueTrend() should format monthly data")
    public void generateRevenueTrend_monthlyData_shouldFormatPeriod() {
        // Given
        String period = "monthly";
        LocalDate date = LocalDate.of(2025, 9, 1); // 9th month
        Tuple trendTuple = ReportFactory.createRevenueTrendTuple(date, 500.0, 5L, 50L);
        when(reportDao.getRevenueTrend(period)).thenReturn(Collections.singletonList(trendTuple));

        // When
        List<RevenueTrendData> result = reportApi.generateRevenueTrend(period);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        RevenueTrendData data = result.get(0);
        assertEquals("2025-09", data.getPeriod()); // Verify YYYY-MM format
        assertEquals(500.0, data.getRevenue());
        assertEquals(5L, data.getOrderCount());
        assertEquals(50L, data.getItemsSold());
    }

    @Test
    @DisplayName("generateRevenueTrend() should format daily data")
    public void generateRevenueTrend_dailyData_shouldFormatPeriod() {
        // Given
        String period = "daily";
        LocalDate date = LocalDate.of(2025, 10, 30);
        Tuple trendTuple = ReportFactory.createRevenueTrendTuple(date, 100.0, 2L, 10L);
        when(reportDao.getRevenueTrend(period)).thenReturn(Collections.singletonList(trendTuple));

        // When
        List<RevenueTrendData> result = reportApi.generateRevenueTrend(period);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        RevenueTrendData data = result.get(0);
        assertEquals("2025-10-30", data.getPeriod()); // Verify YYYY-MM-DD format
        assertEquals(100.0, data.getRevenue());
    }

    @Test
    @DisplayName("generateRevenueTrend() should return empty list when no data")
    public void generateRevenueTrend_noData_shouldReturnEmptyList() {
        // Given
        String period = "monthly";
        when(reportDao.getRevenueTrend(period)).thenReturn(Collections.emptyList());

        // When
        List<RevenueTrendData> result = reportApi.generateRevenueTrend(period);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reportDao, times(1)).getRevenueTrend(period);
    }
}


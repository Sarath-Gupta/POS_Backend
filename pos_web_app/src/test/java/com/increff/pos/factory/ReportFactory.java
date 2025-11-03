package com.increff.pos.factory; // Use your project's test package

import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Test Data Factory for creating mock javax.persistence.Tuple objects.
 * This class provides standardized mock DAO results for use in ReportApi unit tests.
 */
public final class ReportFactory {

    /**
     * Private constructor to prevent instantiation.
     */
    private ReportFactory() {
    }

    /**
     * Creates a mock Tuple for sales metrics.
     *
     * @param totalRevenue Total revenue value.
     * @param totalOrders  Total orders count.
     * @return A mock Tuple.
     */
    public static Tuple createSalesMetricsTuple(Double totalRevenue, Long totalOrders) {
        Map<String, Object> data = new HashMap<>();
        data.put("totalRevenue", totalRevenue);
        data.put("totalOrders", totalOrders);
        return new MockTuple(data);
    }

    /**
     * Creates a mock Tuple for inventory metrics.
     *
     * @param totalProducts  Total number of products.
     * @param totalClients   Total number of clients.
     * @param lowStockItems  Count of items with low stock.
     * @param inventoryValue Total value of all inventory.
     * @return A mock Tuple.
     */
    public static Tuple createInventoryMetricsTuple(Long totalProducts, Long totalClients, Long lowStockItems, Double inventoryValue) {
        Map<String, Object> data = new HashMap<>();
        data.put("totalProducts", totalProducts);
        data.put("totalClients", totalClients);
        data.put("lowStockItems", lowStockItems);
        data.put("inventoryValue", inventoryValue);
        return new MockTuple(data);
    }

    /**
     * Creates a mock Tuple for the top-selling products report.
     *
     * @return A mock Tuple.
     */
    public static Tuple createTopSellingProductTuple(String productName, String barcode, String clientName, Long totalQuantitySold, Double totalRevenue, Integer currentStock) {
        Map<String, Object> data = new HashMap<>();
        data.put("productName", productName);
        data.put("barcode", barcode);
        data.put("clientName", clientName);
        data.put("totalQuantitySold", totalQuantitySold);
        data.put("totalRevenue", totalRevenue);
        data.put("currentStock", currentStock);
        return new MockTuple(data);
    }

    /**
     * Creates a mock Tuple for the revenue trend report.
     *
     * @param date       The SQL Date for the period.
     * @param revenue    Revenue for the period.
     * @param orderCount Order count for the period.
     * @param itemsSold  Items sold for the period.
     * @return A mock Tuple.
     */
    public static Tuple createRevenueTrendTuple(LocalDate date, Double revenue, Long orderCount, Long itemsSold) {
        Map<String, Object> data = new HashMap<>();
        data.put("period", Date.valueOf(date));
        data.put("revenue", revenue);
        data.put("orderCount", orderCount);
        data.put("itemsSold", itemsSold);
        return new MockTuple(data);
    }


    /**
     * A simple, private mock implementation of the Tuple interface for testing.
     */
    private static class MockTuple implements Tuple {
        private final Map<String, Object> data;
        private final List<TupleElement<?>> elements;

        MockTuple(Map<String, Object> data) {
            this.data = data;
            // Create mock TupleElements
            this.elements = data.keySet().stream()
                    .map(key -> (TupleElement<?>) new TupleElement<Object>() {
                        @Override
                        public Class<?> getJavaType() {
                            return data.get(key) != null ? data.get(key).getClass() : Object.class;
                        }

                        @Override
                        public String getAlias() {
                            return key;
                        }
                    })
                    .collect(Collectors.toList());
        }

        @Override
        public <X> X get(TupleElement<X> tupleElement) {
            return (X) data.get(tupleElement.getAlias());
        }

        @Override
        public <X> X get(String alias, Class<X> type) {
            return type.cast(data.get(alias));
        }

        @Override
        public Object get(String alias) {
            return data.get(alias);
        }

        @Override
        public <X> X get(int i, Class<X> type) {
            return get(this.elements.get(i).getAlias(), type);
        }

        @Override
        public Object get(int i) {
            return get(this.elements.get(i).getAlias());
        }

        @Override
        public Object[] toArray() {
            return data.values().toArray();
        }

        @Override
        public List<TupleElement<?>> getElements() {
            return this.elements;
        }
    }
}


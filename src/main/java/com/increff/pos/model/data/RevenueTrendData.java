package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RevenueTrendData {
    private String period;
    private Double revenue;
    private Long orderCount;
    private Long itemsSold;
}

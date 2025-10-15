package com.increff.pos.util;

import com.increff.pos.commons.ApiException;

@FunctionalInterface
public interface TsvMapper<T> {
    T mapTokensToForm(String[] tokens, int rowNumber) throws ApiException;
}

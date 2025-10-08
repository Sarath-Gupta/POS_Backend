package com.increff.pos.api.service;

import com.increff.pos.commons.ApiException;

import java.util.Objects;

public class AbstractApi {
    public static void ifNotExists(Object obj) throws ApiException{
        if(Objects.isNull(obj)) {
            //TODO: also take entity name as input param for better error message
            throw new ApiException("Object doesn't exist");
        }
    }

    public static void ifExists(Object obj) throws ApiException {
        if(!Objects.isNull(obj)) {
            throw new ApiException("Object does exist");
        }
    }
}

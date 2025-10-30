package com.increff.pos.service;

import com.increff.pos.commons.ApiException;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;

public class AbstractApi<T> {
    private final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    public AbstractApi() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    public void ifNotExists(Object obj) throws ApiException{
        if(Objects.isNull(obj)) {
            String message = entityClass.getSimpleName() + " doesn't exist";
            throw new ApiException(message);
        }
    }

    public void ifExists(Object obj) throws ApiException {
        if(!Objects.isNull(obj)) {
            String message = entityClass.getSimpleName();
            throw new ApiException(message + " already exists");
        }
    }
}

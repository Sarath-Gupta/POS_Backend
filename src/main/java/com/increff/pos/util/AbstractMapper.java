package com.increff.pos.util;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AbstractMapper {

    @Autowired
    private ModelMapper mapper;

    public <S, D> D convert(S source, Class<D> destinationType) {
        if (source == null) {
            return null;
        }
        return mapper.map(source, destinationType);
    }

    public <S, D> List<D> convert(List<S> sourceList, Class<D> destinationType) {
        if (sourceList == null) {
            return null;
        }
        return sourceList.stream()
                .map(element -> mapper.map(element, destinationType))
                .collect(Collectors.toList());
    }
}

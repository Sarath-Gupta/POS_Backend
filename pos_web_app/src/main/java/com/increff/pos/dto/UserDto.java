package com.increff.pos.dto;

import com.increff.pos.service.UserApi;
import com.increff.pos.commons.ApiException;
import com.increff.pos.entity.User;
import com.increff.pos.model.data.UserData;
import com.increff.pos.model.form.UserForm;
import com.increff.pos.util.AbstractMapper;
import com.increff.pos.util.NormalizeUtil;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UserDto {

    @Autowired
    UserApi userApi;

    @Autowired
    ValidationUtil validationUtil;

    @Autowired
    AbstractMapper mapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    public UserData add(UserForm userForm) throws ApiException {
        validationUtil.validate(userForm);
        NormalizeUtil.normalize(userForm);
        String hashedPassword = passwordEncoder.encode(userForm.getPassword());
        userForm.setPassword(hashedPassword);
        User userPojo = mapper.convert(userForm, User.class);
        userApi.add(userPojo);
        return mapper.convert(userPojo, UserData.class);
    }

    public UserData login(UserForm userForm) throws ApiException {
        validationUtil.validate(userForm);
        NormalizeUtil.normalize(userForm);
        User userPojo = userApi.findByEmail(userForm.getEmail());
        if(Objects.isNull(userPojo)) {
            throw new ApiException("Invalid email");
        }
        String hashedPassword = passwordEncoder.encode(userForm.getPassword());
        userForm.setPassword(hashedPassword);
        userApi.login(userPojo);
        UserData userData = mapper.convert(userPojo, UserData.class);
        return userData;
    }


}
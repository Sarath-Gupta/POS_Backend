package com.increff.pos.controller;

import com.increff.pos.commons.ApiException;
import com.increff.pos.dto.UserDto;
import com.increff.pos.model.data.UserData;
import com.increff.pos.model.form.UserForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")

public class UserController {

    @Autowired
    UserDto userDto;

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public void signUp(@RequestBody UserForm userForm) throws ApiException {
        userDto.add(userForm);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public UserData login(@RequestBody UserForm userForm) throws ApiException {
        return userDto.login(userForm);
    }


}

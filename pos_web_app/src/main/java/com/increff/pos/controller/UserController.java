package com.increff.pos.controller;

import com.increff.pos.commons.ApiException;
import com.increff.pos.dto.UserDto;
import com.increff.pos.model.data.JwtResponse;
import com.increff.pos.model.data.UserData;
import com.increff.pos.model.form.UserForm;
import com.increff.pos.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @Autowired
    UserDto userDto;

    @Autowired
    JwtService jwtService;

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public void signUp(@RequestBody UserForm userForm) throws ApiException {
        userDto.add(userForm);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> login(@RequestBody UserForm userForm) throws ApiException {
        UserData userData = userDto.login(userForm);
        String jwt = jwtService.generateToken(userData.getEmail(), userData.getRole());
        return ResponseEntity.ok(new JwtResponse(jwt, userData.getEmail(), userData.getRole()));
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("Logout successful");
    }

    @RequestMapping(value = "/me", method = RequestMethod.GET)
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            return ResponseEntity.ok("Current user: " + email);
        }
        return ResponseEntity.badRequest().body("Not authenticated");
    }
}

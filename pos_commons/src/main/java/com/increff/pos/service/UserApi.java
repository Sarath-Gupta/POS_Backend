package com.increff.pos.service;

import com.increff.pos.commons.ApiException;
import com.increff.pos.dao.UserDao;
import com.increff.pos.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserApi {

    @Autowired
    UserDao userDao;

    @Value("${auth.supervisor.emails}")
    private String supervisorEmailString;

    private Set<String> supervisorEmails;

    @PostConstruct
    public void initSupervisorEmails() {
        this.supervisorEmails = Arrays.stream(supervisorEmailString.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    @Transactional
    public void add(User user) {
        if(this.supervisorEmails.contains(user.getEmail())) {
            user.setRole("SUPERVISOR");
        }
        else {
            user.setRole("OPERATOR");
        }
        userDao.add(user);
    }

    public User findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    public User login(User user) throws ApiException {
        return userDao.login(user.getEmail(), user.getPassword());
    }
}

package com.increff.pos.test;

import com.increff.pos.config.TestConfig;
import com.increff.pos.entity.User;
import com.increff.pos.dao.UserDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@Transactional
public class UserDaoTest {
    @Autowired
    private UserDao userDao;

    private User createUser(String email, String password, String role) {
        User u = new User();
        u.setEmail(email);
        u.setPassword(password);
        u.setRole(role);
        userDao.add(u);
        return u;
    }

    @Test
    public void testAddAndFindById() {
        User user = createUser("test@increff.com", "hashed_pass1", "OPERATOR");
        assertNotNull("User ID should be generated after add", user.getId());

        User found = userDao.findById(user.getId());

        assertNotNull("User should be found by ID", found);
        assertEquals("Email should match", "test@increff.com", found.getEmail());
    }

    @Test
    public void testFindAll() {
        createUser("user1@test.com", "hash1", "OPERATOR");
        createUser("admin@test.com", "hash2", "SUPERVISOR");

        List<User> list = userDao.findAll();

        assertEquals("FindAll should return 2 users", 2, list.size());
    }

    @Test
    public void testFindByEmail_Found() {
        createUser("target@mail.com", "abc", "OPERATOR");

        User found = userDao.findByEmail("target@mail.com");

        assertNotNull("User should be found by email", found);
        assertEquals("Email should match the lookup key", "target@mail.com", found.getEmail());
    }

    @Test
    public void testFindByEmail_NotFound() {
        createUser("unique@mail.com", "xyz", "SUPERVISOR");

        User notFound = userDao.findByEmail("missing@mail.com");

        assertNull("findByEmail should return null if user is not found", notFound);
    }

    @Test
    public void testLogin_Success() {
        String mockHashedPass = "MOCKED_HASH_123";
        createUser("login_ok@test.com", mockHashedPass, "OPERATOR");

        User found = userDao.login("login_ok@test.com", mockHashedPass);

        assertNotNull("Login should succeed with correct credentials", found);
        assertEquals("Role should be correct", "OPERATOR", found.getRole());
    }

    @Test
    public void testLogin_Failure() {
        createUser("login_fail@test.com", "correct_hash", "SUPERVISOR");
        User failedLogin = userDao.login("login_fail@test.com", "incorrect_password");
        assertNull("Login should fail with incorrect password", failedLogin);
    }
}

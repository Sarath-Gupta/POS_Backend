package com.increff.pos.unit.service; // Or your unit test package

import com.increff.pos.dao.UserDao;
import com.increff.pos.entity.User;
import com.increff.pos.factory.UserFactory;
import com.increff.pos.service.UserApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the UserApi class.
 * This class mocks the UserDao and manually injects the @Value property
 * to test the role-assignment logic.
 */
@ExtendWith(MockitoExtension.class)
public class UserApiTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserApi userApi; // The service class we are testing

    @BeforeEach
    public void setUp() {
        // This is the crucial part.
        // We must manually set the @Value field and then manually call
        // the @PostConstruct method to simulate Spring's initialization.

        // 1. Define our mock supervisor email list
        String supervisorEmails = "admin@example.com, supervisor@example.com ,  spaced@example.com ";

        // 2. Use Spring's ReflectionTestUtils to inject the @Value
        ReflectionTestUtils.setField(
                userApi,                 // The object to inject into
                "supervisorEmailString", // The name of the private field
                supervisorEmails         // The value to inject
        );

        // 3. Manually call the @PostConstruct method
        userApi.initSupervisorEmails();
    }

    // --- add() Tests ---

    @Test
    @DisplayName("add() should set role to SUPERVISOR for a supervisor email")
    public void add_supervisorEmail_shouldSetRoleSupervisor() {
        // Given
        User supervisorUser = UserFactory.mockNewObject("admin@example.com");

        // When
        userApi.add(supervisorUser);

        // Then
        // We use ArgumentCaptor to capture the object passed to the DAO
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao, times(1)).add(userCaptor.capture());

        // Assert that the role was correctly set to SUPERVISOR
        assertEquals("SUPERVISOR", userCaptor.getValue().getRole());
    }

    @Test
    @DisplayName("add() should set role to OPERATOR for a non-supervisor email")
    public void add_operatorEmail_shouldSetRoleOperator() {
        // Given
        User operatorUser = UserFactory.mockNewObject("random-user@example.com");

        // When
        userApi.add(operatorUser);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao, times(1)).add(userCaptor.capture());

        // Assert that the role was correctly set to OPERATOR
        assertEquals("OPERATOR", userCaptor.getValue().getRole());
    }

    @Test
    @DisplayName("add() should be case-insensitive for supervisor emails")
    public void add_caseInsensitiveEmail_shouldSetRoleSupervisor() {
        // Given
        // The email list is lowercase, but the input is uppercase.
        User supervisorUser = UserFactory.mockNewObject("SUPERVISOR@example.com");

        // When
        userApi.add(supervisorUser);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao, times(1)).add(userCaptor.capture());

        // Assert that the role was still set to SUPERVISOR
        assertEquals("SUPERVISOR", userCaptor.getValue().getRole());
    }

    @Test
    @DisplayName("add() should handle trimmed whitespace for supervisor emails")
    public void add_whitespaceEmail_shouldSetRoleSupervisor() {
        // Given
        // The email in our properties is "  spaced@example.com "
        User supervisorUser = UserFactory.mockNewObject("spaced@example.com");

        // When
        userApi.add(supervisorUser);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao, times(1)).add(userCaptor.capture());

        // Assert that the role was still set to SUPERVISOR
        assertEquals("SUPERVISOR", userCaptor.getValue().getRole());
    }

    // --- findByEmail() Tests ---

    @Test
    @DisplayName("findByEmail() should return user when email exists")
    public void findByEmail_existingEmail_shouldReturnUser() {
        // Given
        String email = "test@example.com";
        User expectedUser = UserFactory.mockPersistedObject(email, "OPERATOR");
        when(userDao.findByEmail(email)).thenReturn(expectedUser);

        // When
        User actualUser = userApi.findByEmail(email);

        // Then
        assertNotNull(actualUser);
        assertEquals(email, actualUser.getEmail());
        verify(userDao, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("findByEmail() should return null when email does not exist")
    public void findByEmail_nonExistingEmail_shouldReturnNull() {
        // Given
        String email = "notfound@example.com";
        when(userDao.findByEmail(email)).thenReturn(null);

        // When
        User actualUser = userApi.findByEmail(email);

        // Then
        assertNull(actualUser);
        verify(userDao, times(1)).findByEmail(email);
    }
}

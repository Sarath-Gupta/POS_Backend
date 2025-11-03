package com.increff.pos.factory; // Use your project's test package

import com.increff.pos.entity.User;
import org.instancio.Instancio;
import org.instancio.Model;

import static org.instancio.Select.field;

/**
 * Test Data Factory for creating User entities using Instancio.
 * This class provides standardized objects for use in unit tests,
 * modeling the "new" vs. "persisted" object state.
 */
public final class UserFactory {

    /**
     * Private constructor to prevent instantiation.
     */
    private UserFactory() {
    }

    /**
     * Model for a 'new' User, as if it came from a form.
     * It will always have a null ID.
     */
    private static final Model<User> NEW_USER_MODEL = Instancio.of(User.class)
            .set(field(User::getId), null)
            .toModel();

    /**
     * Model for a 'persisted' User, as if it were loaded from the DB.
     * It will always have a non-null, positive ID.
     */
    private static final Model<User> PERSISTED_USER_MODEL = Instancio.of(User.class)
            .generate(field(User::getId), gen -> gen.ints().min(1))
            .toModel();

    // --- Methods for NEW objects (ID is always null) ---

    /**
     * Creates a mock User object representing a new entity (null ID).
     *
     * @return A new User object with a null ID.
     */
    public static User mockNewObject() {
        return Instancio.of(NEW_USER_MODEL).create();
    }

    /**
     * Creates a mock User object representing a new entity with a specific email.
     *
     * @param email The specific email to set on the object.
     * @return A new User object with a null ID and the specified email.
     */
    public static User mockNewObject(String email) {
        return Instancio.of(NEW_USER_MODEL)
                .set(field(User::getEmail), email)
                .create();
    }

    // --- Methods for PERSISTED objects (ID is non-null) ---

    /**
     * Creates a mock User object representing a persisted entity (non-null ID).
     *
     * @return A Client object with a non-null random ID.
     */
    public static User mockPersistedObject() {
        return Instancio.of(PERSISTED_USER_MODEL).create();
    }

    /**
     * Creates a mock User object representing a persisted entity with a specific email and role.
     *
     * @param email The specific email to set on the object.
     * @param role  The specific role to set on the object.
     * @return A User object with a random ID and the specified email/role.
     */
    public static User mockPersistedObject(String email, String role) {
        return Instancio.of(PERSISTED_USER_MODEL)
                .set(field(User::getEmail), email)
                .set(field(User::getRole), role)
                .create();
    }
}

package models;

import org.junit.Test;
import play.test.WithApplication;

import javax.persistence.PersistenceException;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.*;

public class UserTest extends WithApplication {

    @Test
    public void testCreate() {
        User user = new User("foo@foo.com", "password", "John Doe");
        user.save();
        assertNotNull(user.id);
        assertEquals("foo@foo.com", user.getEmailAddress());
        assertEquals("John Doe", user.fullName);
        assertNotNull(user.creationDate);

        try {
            // check the private shaPassword
            Field field = User.class.getDeclaredField("shaPassword");
            field.setAccessible(true);
            assertArrayEquals(User.getSha512("password"), (byte[])field.get(user));
            assertEquals(64, ((byte[])field.get(user)).length); // 512 bits = 64 bytes
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test(expected = PersistenceException.class)
    public void testCreateWithDuplicateEmail() {
        User user1 = new User("foo@foo.com", "password", "John Doe");
        user1.save();
        User user2 = new User("foo@foo.com", "password", "John Doe");
        user2.save();
    }

    @Test(expected = PersistenceException.class)
    public void testCreateWithDuplicateEmailDifferentCase() {
        User user1 = new User("foo@foo.com", "password", "John Doe");
        user1.save();
        User user2 = new User("FOO@FOO.COM", "password", "John Doe");
        user2.save();
    }

    @Test
    public void findAll() {
        User user1 = new User("foo@foo.com", "password", "John Doe");
        user1.save();

        User user2 = new User("bar@foo.com", "password", "Jane Doe");
        user2.save();

        List<User> users = User.find.all();

        assertEquals(4, users.size());
    }

    @Test
    public void findByEmailAddressAndPassword() {
        User newUser = new User("foo@foo.com", "password", "John Doe");
        newUser.save();

        User foundUser = User.findByEmailAddressAndPassword("foo@foo.com", "password");

        assertNotNull(foundUser);
        assertEquals("John Doe", foundUser.fullName);
    }

    @Test
    public void findByEmailAddressDifferentCaseAndPassword() {
        User newUser = new User("foo@foo.com", "password", "John Doe");
        newUser.save();

        User foundUser = User.findByEmailAddressAndPassword("FOO@FOO.COM", "password");

        assertNotNull(foundUser);
        assertEquals("John Doe", foundUser.fullName);
    }

    @Test
    public void findByInvalidEmailAddressAndPassword() {
        User newUser = new User("foo@foo.com", "password", "John Doe");
        newUser.save();

        User foundUser = User.findByEmailAddressAndPassword("foo@foo.com", "wrong!");

        assertNull(foundUser);
    }

    @Test
    public void createToken() {
        User newUser = new User("foo@foo.com", "password", "John Doe");
        newUser.save();

        assertNotNull(newUser.id);

        String token = newUser.createToken();

        assertNotNull(token);

        User foundUser = User.findByAuthToken(token);

        assertEquals(foundUser.id, newUser.id);
    }

}

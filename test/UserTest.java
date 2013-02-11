import models.User;

import org.junit.Test;

import javax.persistence.PersistenceException;
import java.lang.reflect.Field;
import java.util.List;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

public class UserTest {

    @Test
    public void testCreate() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                User user = new User("foo@foo.com", "password", "John Doe");
                user.save();
                assertThat(user.id).isNotNull();
                assertThat(user.getEmailAddress()).isEqualTo("foo@foo.com");
                assertThat(user.fullName).isEqualTo("John Doe");
                assertThat(user.creationDate).isNotNull();

                try {
                    // check the private shaPassword
                    Field field = User.class.getDeclaredField("shaPassword");
                    field.setAccessible(true);
                    assertThat(field.get(user)).isEqualTo(User.getSha512("password"));
                    assertThat(((byte[])field.get(user)).length).isEqualTo(64); // 512 bits = 64 bytes

                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

            }
        });
    }

    @Test(expected = PersistenceException.class)
    public void testCreateWithDuplicateEmail() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                User user1 = new User("foo@foo.com", "password", "John Doe");
                user1.save();
                User user2 = new User("foo@foo.com", "password", "John Doe");
                user2.save();
            }
        });
    }

    @Test(expected = PersistenceException.class)
    public void testCreateWithDuplicateEmailDifferentCase() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                User user1 = new User("foo@foo.com", "password", "John Doe");
                user1.save();
                User user2 = new User("FOO@FOO.COM", "password", "John Doe");
                user2.save();
            }
        });
    }

    @Test
    public void findAll() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                User user1 = new User("foo@foo.com", "password", "John Doe");
                user1.save();

                User user2 = new User("bar@foo.com", "password", "Jane Doe");
                user2.save();

                List<User> users = User.find.all();

                assertThat(users.size()).isEqualTo(2);
            }
        });
    }

    @Test
    public void findByEmailAddressAndPassword() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                User newUser = new User("foo@foo.com", "password", "John Doe");
                newUser.save();

                User foundUser = User.findByEmailAddressAndPassword("foo@foo.com", "password");

                assertThat(foundUser).isNotNull();
                assertThat(foundUser.fullName).isEqualTo("John Doe");
            }
        });
    }

    @Test
    public void findByEmailAddressDifferentCaseAndPassword() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                User newUser = new User("foo@foo.com", "password", "John Doe");
                newUser.save();

                User foundUser = User.findByEmailAddressAndPassword("FOO@FOO.COM", "password");

                assertThat(foundUser).isNotNull();
                assertThat(foundUser.fullName).isEqualTo("John Doe");
            }
        });
    }

    @Test
    public void findByInvalidEmailAddressAndPassword() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                User newUser = new User("foo@foo.com", "password", "John Doe");
                newUser.save();

                User foundUser = User.findByEmailAddressAndPassword("foo@foo.com", "wrong!");

                assertThat(foundUser).isNull();
            }
        });
    }

    @Test
    public void createToken() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                User newUser = new User("foo@foo.com", "password", "John Doe");
                newUser.save();
                
                assertThat(newUser.id).isNotNull();
                
                String token = newUser.createToken();
                
                assertThat(token).isNotNull();

                User foundUser = User.findByAuthToken(token);

                assertThat(newUser.id).isEqualTo(foundUser.id);
            }
        });
    }

}

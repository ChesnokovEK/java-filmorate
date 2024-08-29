package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dao.Impl.UserDbStorage;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.DoesNotExistsException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase
@JdbcTest
@ComponentScan("ru.yandex.practicum.filmorate")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(value = {"/schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/clear.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UserControllerTest {
    @Autowired
    public UserController userController;

    @Autowired
    public UserDbStorage userDbStorage;


    @Test
    void createUser() {
        User user = new User(
                0,
                "test@testmail.test",
                "testUserLogin",
                "Foo Bar",
                LocalDate.of(2000, 1, 1),
                new HashSet<>()
        );

        assertEquals(user, userController.createUser(user));

        User user1 = new User(
                1,
                "test@testmail.test",
                "testUserLogin",
                "Foo Bar",
                LocalDate.of(2000, 1, 1),
                new HashSet<>()
        );

        assertEquals(1, userController.findAll().size(), "Неверное количество пользователей");
        assertThrows(AlreadyExistsException.class, () -> userController.createUser(user1), "Должен выбросить исключение");
        assertEquals(user, new LinkedList<>(userController.findAll()).getLast());
    }

    @Test
    void userValidationTest() {
        User user1 = new User(
                0,
                null,
                "testUserLogin",
                "Foo Bar",
                LocalDate.of(2000, 1, 1),
                new HashSet<>()
        );
        User user2 = new User(
                0,
                "testtestmail.test",
                "testUserLogin",
                "Foo Bar",
                LocalDate.of(2000, 1, 1),
                new HashSet<>()
        );
        User user3 = new User(
                0,
                "test@testmail.test",
                null,
                "Foo Bar",
                LocalDate.of(2000, 1, 1),
                new HashSet<>()
        );
        User user4 = new User(
                0,
                "test@testmail.test",
                "testUser Login",
                "Foo Bar",
                LocalDate.of(2000, 1, 1),
                new HashSet<>()
        );

        User user5 = new User(
                0,
                "test@testmail.test",
                "testUserLogin",
                "Foo Bar",
                LocalDate.of(2222, 1, 1),
                new HashSet<>()
        );

        assertThrows(ValidationException.class, () -> userController.createUser(user1), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> userController.createUser(user2), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> userController.createUser(user3), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> userController.createUser(user4), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> userController.createUser(user5), "Должен выбросить исключение");
    }

    @Test
    void userUpdateTest() {
        User updatable = new User(
                0,
                "test@testmail.test",
                "testUserLogin",
                "Foo Bar",
                LocalDate.of(2000, 1, 1),
                new HashSet<>()
        );

        userController.createUser(updatable);

        User nonUpdatable = new User(
                Integer.MAX_VALUE,
                "nonUpdatable@testmail.test",
                "nonUpdatabletestUserLogin",
                "nonUpdatableFoo Bar",
                LocalDate.of(2000, 1, 1),
                new HashSet<>()
        );

        assertThrows(DoesNotExistsException.class, () -> userController.updateUser(nonUpdatable), "Должен выбросить исключение");

        User updatedUser = new User(
                0,
                "updated@testmail.test",
                "updatedUserLogin",
                "updatedFoo Bar",
                LocalDate.of(2000, 1, 1),
                new HashSet<>()
        );
        updatedUser.setId(updatable.getId());

        userController.updateUser(updatedUser);

        assertEquals(1, userController.findAll().size(), "Неверное количество пользователей");
        assertEquals(updatedUser, userController.findAll().get(0), "Сохранен не тот пользователь");
    }

    @Test
    void findAllTest() {
        List<User> emptyUserList = userController.findAll();

        assertTrue(emptyUserList.isEmpty(), "Пользователи пока не были добавлены");

        User user = new User(
                0,
                "test@testmail.test",
                "testUserLogin",
                "Foo Bar",
                LocalDate.of(2000, 1, 1),
                new HashSet<>()
        );

        userController.createUser(user);

        assertEquals(1, userController.findAll().size(), "Неверный размер списка пользователей");
        assertEquals(user, userController.findAll().get(0), "Сохранен не тот пользователь");
    }
}

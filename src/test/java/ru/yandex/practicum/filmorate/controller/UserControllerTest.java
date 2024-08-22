package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.DoesNotExistsException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {
    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(new UserService(new InMemoryUserStorage()));
    }

    @Test
    void createUser() {
        User user = new User(0, "test@testmail.test", "testUserLogin",
                "Foo Bar", LocalDate.of(2000, 1, 1));

        assertEquals(user, userController.createUser(user));

        User user1 = userController.createUser(new User(1, "test@testmail.test", "testUserLogin",
                null, LocalDate.of(2000, 1, 1)));

        assertEquals(2, userController.findAll().size(), "Неверное количество пользователей");
        assertThrows(AlreadyExistsException.class, () -> userController.createUser(user), "Должен выбросить исключение");
        assertEquals(user1.getLogin(), new LinkedList<>(userController.findAll()).getLast().getName());
    }

    @Test
    void userValidationTest() {
        User user1 = new User(0, null, "testUserLogin",
                "Foo Bar", LocalDate.of(2000, 1, 1));
        User user2 = new User(0, "testtestmail.test", "testUserLogin",
                "Foo Bar", LocalDate.of(2000, 1, 1));
        User user3 = new User(0, "test@testmail.test", null,
                "Foo Bar", LocalDate.of(2000, 1, 1));
        User user4 = new User(0, "test@testmail.test", "test UserLogin",
                "Foo Bar", LocalDate.of(2000, 1, 1));
        User user5 = new User(0, "test@testmail.test", "testUserLogin",
                "Foo Bar", LocalDate.of(2222, 1, 1));

        assertThrows(ValidationException.class, () -> userController.createUser(user1), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> userController.createUser(user2), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> userController.createUser(user3), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> userController.createUser(user4), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> userController.createUser(user5), "Должен выбросить исключение");
    }

    @Test
    void userUpdateTest() {
        userController.createUser(new User(0, "test@testmail.test", "testUserLogin",
                "Foo Bar", LocalDate.of(2000, 1, 1)));

        User nonUpdatable = new User(Integer.MAX_VALUE, "updated@testmail.test", "updatedTestUserLogin",
                "UpdatedFoo Bar", LocalDate.of(2000, 1, 1));

        assertThrows(DoesNotExistsException.class, () -> userController.updateUser(nonUpdatable), "Должен выбросить исключение");

        User updatedUser = userController.updateUser(new User(0, "updated@testmail.test", "updatedTestUserLogin",
                "UpdatedFoo Bar", LocalDate.of(2000, 1, 1)));

        assertEquals(1, userController.findAll().size(), "Неверное количество пользователей");
        assertEquals(updatedUser, userController.findAll().get(0), "Сохранен не тот пользователь");
    }

    @Test
    void findAllTest() {
        List<User> emptyUserList = userController.findAll();

        assertTrue(emptyUserList.isEmpty(), "Пользователи пока не были добавлены");

        User user = userController.createUser(new User(0, "test@testmail.test", "testUserLogin",
                "Foo Bar", LocalDate.of(2000, 1, 1)));

        assertEquals(1, userController.findAll().size(), "Неверный размер списка пользователей");
        assertEquals(user, userController.findAll().get(0), "Сохранен не тот пользователь");
    }
}

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
        User user = User.builder()
                .id(0)
                .email("test@testmail.test")
                .login("testUserLogin")
                .name("Foo Bar")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        assertEquals(user, userController.createUser(user));

        User user1 = User.builder()
                .id(1)
                .email("test@testmail.test")
                .login("testUserLogin")
                .name("")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        userController.createUser(user1);

        assertEquals(2, userController.findAll().size(), "Неверное количество пользователей");
        assertThrows(AlreadyExistsException.class, () -> userController.createUser(user), "Должен выбросить исключение");
        assertEquals(user1.getLogin(), new LinkedList<>(userController.findAll()).getLast().getName());
    }

    @Test
    void userValidationTest() {
        User user1 = User.builder()
                .id(0)
                .email(null)
                .login("testUserLogin")
                .name("Foo Bar")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User user2 = User.builder()
                .id(0)
                .email("testtestmail.test")
                .login("testUserLogin")
                .name("Foo Bar")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User user3 = User.builder()
                .id(0)
                .email("test@testmail.test")
                .login(null)
                .name("Foo Bar")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User user4 = User.builder()
                .id(0)
                .email("test@testmail.test")
                .login("test UserLogin")
                .name("Foo Bar")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User user5 = User.builder()
                .id(0)
                .email("test@testmail.test")
                .login("testUserLogin")
                .name("Foo Bar")
                .birthday(LocalDate.of(2222, 1, 1))
                .build();

        assertThrows(ValidationException.class, () -> userController.createUser(user1), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> userController.createUser(user2), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> userController.createUser(user3), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> userController.createUser(user4), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> userController.createUser(user5), "Должен выбросить исключение");
    }

    @Test
    void userUpdateTest() {
        User updatable = User.builder()
                .id(0)
                .email("test@testmail.test")
                .login("testUserLogin")
                .name("Foo Bar")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        userController.createUser(updatable);

        User nonUpdatable = User.builder()
                .id(Integer.MAX_VALUE)
                .email("nonUpdatable@testmail.test")
                .login("nonUpdatableTestUserLogin")
                .name("nonUpdatableFoo Bar")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        assertThrows(DoesNotExistsException.class, () -> userController.updateUser(nonUpdatable), "Должен выбросить исключение");

        User updatedUser = User.builder()
                .id(0)
                .email("updated@testmail.test")
                .login("updatedTestUserLogin")
                .name("UpdatedFoo Bar")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        userController.updateUser(updatedUser);

        assertEquals(1, userController.findAll().size(), "Неверное количество пользователей");
        assertEquals(updatedUser, userController.findAll().get(0), "Сохранен не тот пользователь");
    }

    @Test
    void findAllTest() {
        List<User> emptyUserList = userController.findAll();

        assertTrue(emptyUserList.isEmpty(), "Пользователи пока не были добавлены");

        User user = User.builder()
                .id(0)
                .email("test@testmail.test")
                .login("testUserLogin")
                .name("Foo Bar")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        userController.createUser(user);

        assertEquals(1, userController.findAll().size(), "Неверный размер списка пользователей");
        assertEquals(user, userController.findAll().get(0), "Сохранен не тот пользователь");
    }
}

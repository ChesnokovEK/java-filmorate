package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.DoesNotExistsException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int userNextId = 1;

    @GetMapping
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Запущен метод по добавлению пользователя. Текущее количество пользователей в базе: {}", users.size());
        if (isIdExists(user)) {
            log.info("Выполнение метода прервано: пользователю был присвоен id {} до его добавления. " +
                    "Текущее количество пользователей в базе: {}", user.getId(), users.size());
            throw new AlreadyExistsException("Пользователь с таким id уже существует");
        }

        userValidationTest(user);

        if (user.getId() == null) {
            user.setId(userNextId);
            userNextId++;
        }

        users.put(user.getId(), user);
        log.info("Пользователь с email {}, логином {} и id {} был успешно добавлен. Текущее количество " +
                "пользователей в базе: {}", user.getEmail(), user.getLogin(), user.getId(), users.size());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Запущен метод по обновлению пользователя. Текущее количество пользователей в базе: {}", users.size());
        if (!users.containsKey(user.getId())) {
            log.info("Выполнение метода прервано: пользователь с id {} не зарегистрирован в базе. Текущее количество " +
                    "пользователей в базе: {}", user.getId(), users.size());
            throw new DoesNotExistsException("Пользователь не зарегистрирован");
        }
        userValidationTest(user);
        users.put(user.getId(), user);
        log.info("Данные пользователя с id {} были успешно обновлены. Текущее количество " +
                "пользователей в базе: {}", user.getId(), users.size());
        return user;
    }

    private void userValidationTest(User user) {
        final Pattern EMAIL_PATTERN = Pattern.compile("^(?=.{1,64}@)[A-Za-z\\d_-]+(\\\\.[A-Za-z\\d_-]+)*@[^-][A-Za-z\\d-]+(\\\\.[A-Za-z\\d-]+)*(\\\\.[A-Za-z]{2,})$");

        if (user.getEmail() == null || user.getEmail().isBlank() || !EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            log.info("Ошибка: некорректный email. Указанный email: {}", user.getEmail());
            throw new ValidationException("Некорректный адрес электронной почты");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.info("Ошибка: некорректный логин. Указанный логин: {}", user.getLogin());
            throw new ValidationException("Логин не может быть пустым или содержать пробелы");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.info("Ошибка: некорректная дата рождения пользователя. Указанная дата: {}",
                    user.getBirthday());
            throw new ValidationException("Указана неправильная дата рождения");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Внимание: пользователь не указал имя. Имени присвоено значение логина {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }

    private boolean isIdExists(User user) {
        return users.containsKey(user.getId());
    }
}

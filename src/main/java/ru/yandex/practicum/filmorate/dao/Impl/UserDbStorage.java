package ru.yandex.practicum.filmorate.dao.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.DoesNotExistsException;
import ru.yandex.practicum.filmorate.exception.UnexpectedException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.time.LocalDate;
import java.util.*;

@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class UserDbStorage implements Storage<User> {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY user_id";
        List<User> userList = jdbcTemplate.query(sql, userRowMapper());
        for (User user : userList) {
            user.setFriends(new HashSet<>(findUserFriends(user.getId())));
        }
        return userList;
    }

    @Override
    public User create(User user) {
        if (user.getId() != null) {
            throw new AlreadyExistsException("Пользователь уже существует");
        }
        userValidationTest(user);

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");
        int id = simpleJdbcInsert.executeAndReturnKey(userToRow(user)).intValue();
        user.setId(id);
        return user;
    }

    @Override
    public User add(int userId, int friendId) {
        try {
            findById(userId);
            findById(friendId);
        } catch (RuntimeException e) {
            throw new DoesNotExistsException("Пользователь не существует");
        }


        String sql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);

        return update(findById(userId));
    }

    @Override
    public User findById(int id) {
        try {
            String sql = "SELECT * FROM users WHERE user_id = ?";
            if (findUserFriends(id).isEmpty()) {
                return jdbcTemplate.queryForObject(sql, userRowMapper(), id);
            }

            User user = jdbcTemplate.queryForObject(sql, userRowMapper(), id);
            if (user == null) {
                throw new UnexpectedException("Произошла непредвиденная ошибка при создании пользователя");
            }
            user.setFriends(new TreeSet<>(findUserFriends(id)));
            return user;
        } catch (EmptyResultDataAccessException e) {
            throw new DoesNotExistsException("Пользователь с id " + id + " не найден");
        }
    }

    @Override
    public User update(User user) {
        if (user.getId() == 0) {
            throw new DoesNotExistsException("Пользователь с таким id не существует");
        }
        userValidationTest(user);

        findById(user.getId());

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        int updatedRows = jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(),
                user.getId());

        if (updatedRows != 1) {
            throw new UnexpectedException("При обновлении данных пользователя произошла непредвиденная ошибка");
        }
        return user;
    }

    @Override
    public User remove(int userId, int friendId) {
        try {
            findById(userId);
            findById(friendId);
        } catch (RuntimeException e) {
            throw new DoesNotExistsException("Пользователь уже существует");
        }

        String sql = "DELETE FROM friends WHERE user_id=? AND friend_id=?";
        jdbcTemplate.update(sql, userId, friendId);

        return update(findById(userId));

    }

    private RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> new User(rs.getInt("user_id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                rs.getDate("birthday").toLocalDate(),
                new HashSet<>()
        );
    }

    private Map<String, Object> userToRow(User user) {
        return new HashMap<>(Map.of("email", user.getEmail(),
                "login", user.getLogin(),
                "name", user.getName(),
                "birthday", user.getBirthday()));
    }

    private List<Integer> findUserFriends(int userId) {
        String sql = "SELECT friend_id FROM friends WHERE user_id=?";
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> resultSet.getInt("friend_id"), userId);
    }

    private void userValidationTest(User user) {
        emailValidation(user);
        loginValidation(user);
        birthdayValidation(user);
        setLoginAsNameIfBlankOrNull(user);
    }

    private void emailValidation(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.info("Выполнение метода прервано: некорректный email. Указанный email: {}", user.getEmail());
            throw new ValidationException("Некорректный адрес электронной почты");
        }
    }

    private void loginValidation(User user) {
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.info("Выполнение метода прервано: некорректный логин. Указанный логин: {}", user.getLogin());
            throw new ValidationException("Логин не может быть пустым или содержать пробелы");
        }
    }

    private void birthdayValidation(User user) {
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.info("Выполнение метода прервано: некорректная дата рождения пользователя. Указанная дата: {}", user.getBirthday());
            throw new ValidationException("Указана неправильная дата рождения");
        }
    }

    private void setLoginAsNameIfBlankOrNull(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Пользователь не указал имя. Имени присвоено значение логина {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }
}
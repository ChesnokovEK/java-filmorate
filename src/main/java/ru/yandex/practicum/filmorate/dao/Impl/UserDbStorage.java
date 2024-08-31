package ru.yandex.practicum.filmorate.dao.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UnexpectedException;
import ru.yandex.practicum.filmorate.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDbStorage implements Storage<User> {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY user_id";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public User create(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");
        int id = simpleJdbcInsert.executeAndReturnKey(userToRow(user)).intValue();
        user.setId(id);
        return user;
    }

    @Override
    public User add(int userId, int friendId) {
        String sql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);

        return update(findById(userId));
    }

    @Override
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
        assert user != null;
        user.setFriends(new TreeSet<>(findUserFriends(id)));
        return user;
    }

    @Override
    public User update(User user) {
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
        String sql = "DELETE FROM friends WHERE user_id=? AND friend_id=?";
        jdbcTemplate.update(sql, userId, friendId);

        return update(findById(userId));
    }
//
//    private RowMapper<User> userRowMapper() {
//        return (rs, rowNum) -> new User(rs.getInt("user_id"),
//                rs.getString("email"),
//                rs.getString("login"),
//                rs.getString("name"),
//                rs.getDate("birthday").toLocalDate(),
//                new HashSet<>()
//        );
//    }

    public List<Integer> findUserFriends(int userId) {
        String sql = "SELECT friend_id FROM friends WHERE user_id=?";
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> resultSet.getInt("friend_id"), userId);
    }

    private Map<String, Object> userToRow(User user) {
        return new HashMap<>(Map.of("email", user.getEmail(),
                "login", user.getLogin(),
                "name", user.getName(),
                "birthday", user.getBirthday()));
    }
}
package ru.yandex.practicum.filmorate.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

@RequiredArgsConstructor
@Component
public class UserRowMapper implements RowMapper<User> {
    private final JdbcTemplate jdbc;
    private static final String FIND_ALL_FRIENDS = "SELECT friend_id FROM friends WHERE user_id = ?";

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());

        List<Integer> friends = jdbc.queryForList(FIND_ALL_FRIENDS, Integer.class, user.getId());
        user.setFriends(new HashSet<>(friends));

        return user;
    }
}

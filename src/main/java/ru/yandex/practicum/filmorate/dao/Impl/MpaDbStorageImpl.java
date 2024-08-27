package ru.yandex.practicum.filmorate.dao.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.MpaDbStorage;
import ru.yandex.practicum.filmorate.exception.DoesNotExistsException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@RequiredArgsConstructor
@Component
public class MpaDbStorageImpl implements MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> findAll() {
        String sql = "SELECT * FROM mpa ORDER BY mpa_id";
        return jdbcTemplate.query(sql, mpaRowMapper());
    }

    @Override
    public Mpa findById(int id) {
        try {
            String sql = "SELECT * FROM mpa WHERE mpa_id = ?";
            return jdbcTemplate.queryForObject(sql, mpaRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new DoesNotExistsException("Возрастной рейтинг с идентификатором " + id + " не найден");
        }
    }

    private RowMapper<Mpa> mpaRowMapper() {
        return (rs, rowNum) -> new Mpa(rs.getInt("mpa_id"), rs.getString("name"));
    }
}
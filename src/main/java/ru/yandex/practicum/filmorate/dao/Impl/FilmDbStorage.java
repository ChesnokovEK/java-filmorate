package ru.yandex.practicum.filmorate.dao.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.exception.DoesNotExistsException;
import ru.yandex.practicum.filmorate.exception.UnexpectedException;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class FilmDbStorage implements Storage<Film> {
    private final JdbcTemplate jdbcTemplate;
    private final GenreDbStorage genreDbStorage;
    private final FilmRowMapper filmRowMapper;

    @Override
    public List<Film> findAll() {
        String films = "SELECT * FROM films";
        return jdbcTemplate.query(films, filmRowMapper);
    }

    @Override
    public Film create(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");
        int id = simpleJdbcInsert.executeAndReturnKey(filmToRow(film)).intValue();
        film.setId(id);

        List<Genre> filmGenre = new ArrayList<>(film.getGenres());

        if (filmGenre.size() != 0) {
            genreDbStorage.addGenreToTheFilm(id, filmGenre);
        }

        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, mpa_id = ?, release_date = ?, duration = ?" +
                " WHERE film_id = ?";
        int updatedRows = jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getMpa().getId(),
                film.getReleaseDate(), film.getDuration(), film.getId());
        if (updatedRows != 1) {
            throw new UnexpectedException("При обновлении данных произошла непредвиденная ошибка");
        }

        genreDbStorage.updateFilmGenre(film);

        return film;
    }

    @Override
    public Film findById(int id) {
        try {
            String sql = "SELECT * FROM films WHERE film_id = ?";
            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);

            if (film == null) {
                throw new UnexpectedException("Случилась непредвиденная ошибка - передан null");
            }

            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new DoesNotExistsException("Фильм с id " + id + " не найден");
        }
    }

    @Override
    public Film add(int filmId, int userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        int update = jdbcTemplate.update(sql, filmId, userId);

        if (update == 0) {
            throw new UnexpectedException("Произошла непредвиденная ошибка при обновлении списка лайков");
        }

        return update(findById(filmId));
    }

    @Override
    public Film remove(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id=? AND user_id=?";
        int update = jdbcTemplate.update(sql, filmId, userId);
        if (update == 0) {
            throw new UnexpectedException("Произошла ошибка при удалении лайка");
        }

        return update(findById(filmId));
    }

    private Map<String, Object> filmToRow(Film film) {
        Map<String, Object> values = new HashMap<>();
        values.put("name", film.getName());
        values.put("description", film.getDescription());
        values.put("mpa_id", film.getMpa().getId());
        values.put("release_date", film.getReleaseDate());
        values.put("duration", film.getDuration());
        return values;
    }

}

package ru.yandex.practicum.filmorate.dao.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.exception.DoesNotExistsException;
import ru.yandex.practicum.filmorate.exception.UnexpectedException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GenreDbStorageImpl implements GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> findAll() {
        String sql = "SELECT * FROM genres ORDER BY genre_id";
        return jdbcTemplate.query(sql, genreRowMapper());
    }

    @Override
    public Genre findById(int id) {
        try {
            String sql = "SELECT * FROM genres WHERE genre_id=?";
            return jdbcTemplate.queryForObject(sql, genreRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new DoesNotExistsException("Жанр с запрошенным идентификатором не найден");
        }
    }

    @Override
    public List<Genre> findByFilmId(int filmId) {
        List<Integer> idList = findIdsByFilm(filmId);
        List<Genre> genres = new ArrayList<>();
        for (int id : idList) {
            Genre genre = findById(id);
            genres.add(genre);
        }
        return genres;
    }

    @Override
    public void addGenreToTheFilm(int filmId, int genreId) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        int updatedRow = jdbcTemplate.update(sql, filmId, genreId);
        if (updatedRow != 1) {
            throw new UnexpectedException("Произошла ошибка при добавлении жанра");
        }
    }

    @Override
    public void updateFilmGenre(Film film) {
        String sql = "DELETE FROM film_genres WHERE film_id=?";
        jdbcTemplate.update(sql, film.getId());
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        List<Genre> filmGenre = new ArrayList<>(film.getGenres());

        for (Genre genre : filmGenre) {
            addGenreToTheFilm(film.getId(), genre.getId());
        }
    }

    private RowMapper<Genre> genreRowMapper() {
        return (rs, rowNum) -> new Genre(
                rs.getInt("genre_id"),
                rs.getString("name")
        );
    }

    private List<Integer> findIdsByFilm(int filmId) {
        String sql = "SELECT * FROM film_genres WHERE film_id=?";
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> resultSet.getInt("genre_id"), filmId);
    }
}
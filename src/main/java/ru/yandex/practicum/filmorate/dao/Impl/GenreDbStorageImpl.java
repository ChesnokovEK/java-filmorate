package ru.yandex.practicum.filmorate.dao.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.mappers.GenresRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GenreDbStorageImpl implements GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenresRowMapper genresRowMapper;

    @Override
    public List<Genre> findAll() {
        String sql = "SELECT * FROM genres ORDER BY genre_id";
        return jdbcTemplate.query(sql, genresRowMapper);
    }

    @Override
    public Genre findById(int id) {
        String sql = "SELECT * FROM genres WHERE genre_id=?";
        return jdbcTemplate.queryForObject(sql, genresRowMapper, id);
    }

    @Override
    public List<Genre> findByFilmId(int filmId) {
        String sql = "SELECT * FROM film_genres WHERE film_id=?";
        return jdbcTemplate.query(sql, genresRowMapper);
    }

    @Override
    public void addGenreToTheFilm(int filmId, List<Genre> genres) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Genre genre = genres.get(i);
                ps.setInt(1, filmId);
                ps.setInt(2, genre.getId());
            }

            @Override
            public int getBatchSize() {
                return genres.size();
            }
        });
//
//        StringBuilder sb = new StringBuilder(sql);
//        for (int i = 0; i < genres.size(); i++) {
//            sb.append("VALUES (").append(filmId).append(", ").append(genres.get(i).getId()).append(")");
//
//            if (i < genres.size() - 1) {
//                sb.append(", ");
//                continue;
//            }
//
//            sb.append(";");
//        }
//
//        jdbcTemplate.execute(sb.toString());
    }

    @Override
    public void updateFilmGenre(Film film) {
        String sql = "DELETE FROM film_genres WHERE film_id=?";
        jdbcTemplate.update(sql, film.getId());
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        List<Genre> filmGenre = new ArrayList<>(film.getGenres());

        addGenreToTheFilm(film.getId(), filmGenre);
    }
}
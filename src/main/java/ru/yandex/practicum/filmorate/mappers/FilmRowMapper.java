package ru.yandex.practicum.filmorate.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

@RequiredArgsConstructor
@Component
public class FilmRowMapper implements RowMapper<Film> {
    private final JdbcTemplate jdbc;
    private final RowMapper<Genre> genreRowMapper;
    private final RowMapper<Mpa> mpaRowMapper;

    private static final String FIND_ALL_USERS_THAT_LIKE = "SELECT user_id FROM likes WHERE film_id = ?";
    private static final String FIND_ALL_FILM_GENRES = "SELECT g.genre_id, g.name FROM film_genres AS fg " +
            " JOIN genres AS g ON g.genre_id = fg.genre_id" +
            " WHERE fg.film_id = ?";
    private static final String FIND_FILM_MPA = "SELECT m.mpa_id, m.name FROM films AS f" +
            " JOIN mpa AS m ON m.mpa_id = f.mpa_id" +
            " WHERE f.film_id = ?";

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));

        Date releaseDate = rs.getDate("release_date");
        film.setReleaseDate(releaseDate.toLocalDate());

        film.setDuration(rs.getInt("duration"));

        List<Integer> likes = jdbc.queryForList(FIND_ALL_USERS_THAT_LIKE, Integer.class, film.getId());
        film.setLikes(new HashSet<>(likes));

        List<Genre> genres = jdbc.query(FIND_ALL_FILM_GENRES, genreRowMapper, film.getId());
        film.setGenres(new HashSet<>(genres));

        film.setMpa(jdbc.queryForObject(FIND_FILM_MPA, mpaRowMapper, film.getId()));
        return film;
    }
}

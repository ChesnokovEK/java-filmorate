package ru.yandex.practicum.filmorate.dao.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.dao.MpaDbStorage;
import ru.yandex.practicum.filmorate.exception.DoesNotExistsException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.UnexpectedException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.time.LocalDate;
import java.util.*;

@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class FilmDbStorage implements Storage<Film> {
    private final JdbcTemplate jdbcTemplate;
    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;

    @Override
    public List<Film> findAll() {
        String sql = "SELECT * FROM films";
        List<Film> filmList = jdbcTemplate.query(sql, filmRowMapper());
        for (Film film : filmList) {
            film.getGenres().addAll(genreDbStorage.findByFilmId(film.getId()));
            film.getLikes().addAll(getFilmLikes(film.getId()));
        }
        return filmList;
    }

    @Override
    public Film create(Film film) {
        filmValidationTest(film);
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");
        int id = simpleJdbcInsert.executeAndReturnKey(filmToRow(film)).intValue();
        film.setId(id);

        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return film;
        }

        if (film.getMpa() == null) {
            return film;
        }

        validateGenre(film);
        List<Genre> filmGenre = new ArrayList<>(film.getGenres());

        for (Genre genre : filmGenre) {
            genreDbStorage.addGenreToTheFilm(id, genre.getId());
        }
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == 0) {
            throw new DoesNotExistsException("Запрошенного фильма не существует");
        }

        findById(film.getId());

        String sql = "UPDATE films SET name = ?, description = ?, mpa_id = ?, release_date = ?, duration = ?" +
                " WHERE film_id = ?";
        int updatedRows = jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getMpa().getId(),
                film.getReleaseDate(), film.getDuration(), film.getId());
        if (updatedRows != 1) {
            throw new UnexpectedException("При обновлении данных произошла непредвиденная ошибка");
        }

        if (film.getGenres() != null) {
            film.setGenres(new TreeSet<>(film.getGenres()));
        }

        genreDbStorage.updateFilmGenre(film);

        return film;
    }

    @Override
    public Film findById(int id) {
        try {
            String sql = "SELECT * FROM films WHERE film_id = ?";

            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper(), id);
            if (film == null) {
                throw new UnexpectedException("Случилась непредвиденная ошибка - передан null");
            }
            film.getGenres().addAll(genreDbStorage.findByFilmId(id));

            List<Integer> likes = getFilmLikes(id);

            if (likes.isEmpty()) {
                return film;
            }

            film.setLikes(new TreeSet<>(likes));
            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new DoesNotExistsException("Фильм с id " + id + " не найден");
        }
    }

    @Override
    public Film add( int filmId, int userId) {
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

    private RowMapper<Film> filmRowMapper() {
        return (rs, rowNum) -> new Film(rs.getInt("film_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("release_date").toLocalDate(),
                rs.getInt("duration"),
                mpaDbStorage.findById(rs.getInt("mpa_id")),
                new TreeSet<>(),
                new TreeSet<>()
        );
    }

    private List<Integer> getFilmLikes(int filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id=?";
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> resultSet.getInt("user_id"), filmId);
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

    private void filmValidationTest(Film film) {
        validateName(film);
        validateDescription(film);
        validateReleaseDate(film);
        validateDuration(film);
        validateMpa(film);
        log.info("Фильм успешно прошел валидацию");
    }

    private void validateName(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.info("Выполнение метода прервано. Ошибка названия фильма. Название фильма: {}", film.getName());
            throw new ValidationException("Название фильма не может быть пустым");
        }
    }

    private void validateDescription(Film film) {
        if (film.getDescription().length() > 200) {
            log.info("Выполнение метода прервано. Ошибка описания фильма. Текущая длина описания: {}",
                    film.getDescription().length());
            throw new ValidationException("Превышена максимально допустимая длина описания фильма");
        }
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.info("Выполнение метода прервано. Ошибка даты релиза фильма. Указанная дата релиза: {}",
                    film.getReleaseDate());
            throw new ValidationException("Ошибка даты релиза фильма");
        }
    }

    private void validateDuration(Film film) {
        if (film.getDuration() <= 0) {
            log.info("Выполнение метода прервано. Ошибка продолжительности фильма. Указана продолжительность: {}",
                    film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }

    private void validateMpa(Film film) {
        List<Mpa> mpaList = mpaDbStorage.findAll();
        for (Mpa mpa : mpaList) {
            if (mpa.getId() == film.getMpa().getId()) {
                return;
            }
        }

        log.info("Выполнение метода прервано. Ошибка рейтинга фильма. Рейтинг не указан либо не существует");
        throw new ValidationException("У фильма должен быть указан существующий рейтинг");
    }

    private void validateGenre(Film film) {
        List<Integer> genresId = genreDbStorage.findAll().stream().map(Genre::getId).toList();
        for (Genre genre : film.getGenres()) {
            if (!genresId.contains(genre.getId())) {
                log.info("Выполнение метода прервано. Указаный жанр не существует");
                throw new ValidationException("У фильма должен быть указан существующий жанр");
            }
        }
    }
}

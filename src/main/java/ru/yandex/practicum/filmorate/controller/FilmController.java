package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.DoesNotExistsException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private int filmNextId = 0;

    @GetMapping
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Попытка добавить новый фильм. Текущее количество фильмов в базе: {}", films.size());
        if (isIdExists(film)) {
            log.info("Добавление прервано. фильму был присвоен id {} до его добавления. " +
                    "Текущее количество фильмов в базе: {}", film.getId(), films.size());
            throw new AlreadyExistsException("Фильм с таким id уже существует");
        }
        filmValidationTest(film);
        film.setId(filmNextId);
        filmNextId++;
        films.put(film.getId(), film);
        log.info("Фильм с названием {} и id {} был успешно добавлен. Текущее количество фильмов в базе: {}",
                film.getName(), film.getId(), films.size());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Попытка обновить фильм. Текущее количество фильмов в базе: {}", films.size());
        if (!films.containsKey(film.getId())) {
            log.info("Обновление прервано: фильм с id {} не найден. " +
                    "Текущее количество фильмов в базе: {}", film.getId(), films.size());
            throw new DoesNotExistsException("Запрошенный для обновления фильм не найден");
        }
        filmValidationTest(film);
        films.put(film.getId(), film);
        log.info("Фильм с id {} был успешно обновлен. Текущее количество фильмов в базе: {}",
                film.getId(), films.size());
        return film;
    }

    private void filmValidationTest(Film film) {
        final LocalDate FIRST_FILM_RELEASE_DATE = LocalDate.of(1895, 3, 22);

        if (film.getName() == null || film.getName().isBlank()) {
            log.info("Ошибка названия фильма. Название фильма: {}", film.getName());
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            log.info("Ошибка описания фильма. Текущая длина описания: {}",
                    film.getDescription().length());
            throw new ValidationException("Превышена максимально допустимая длина описания фильма");
        }
        if (film.getReleaseDate().isBefore(FIRST_FILM_RELEASE_DATE)) {
            log.info("Ошибка даты релиза фильма. Указанная дата релиза: {}",
                    film.getReleaseDate());
            throw new ValidationException("Ошибка даты релиза фильма");
        }
        if (film.getDuration() <= 0) {
            log.info("Ошибка продолжительности фильма. Указана продолжительность: {}",
                    film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }

    private boolean isIdExists(Film film) {
        for (Film filmInList : findAll()) {
            if (film.getId() == filmInList.getId()) {
                return true;
            }
        }
        return false;
    }
}

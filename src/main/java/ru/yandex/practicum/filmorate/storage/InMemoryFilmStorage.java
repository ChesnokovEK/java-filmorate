package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.DoesNotExistsException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements Storage<Film> {
    private final Map<Integer, Film> films = new HashMap<>();
    private int filmNextId = 1;

    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    public Film findById(int id) {
        if (films.containsKey(id)) {
            return films.get(id);
        }
        throw new DoesNotExistsException("Фильм не найден");
    }

    public Film create(Film film) {
        log.info("Попытка добавить новый фильм. Текущее количество фильмов в базе: {}", films.size());
        if (isIdExists(film)) {
            log.info("Добавление прервано. фильму был присвоен id {} до его добавления. " +
                    "Текущее количество фильмов в базе: {}", film.getId(), films.size());
            throw new AlreadyExistsException("Фильм с таким id уже существует");
        }

        filmValidationTest(film);

        if (film.getId() == null) {
            film.setId(filmNextId);
            filmNextId++;
        }

        films.put(film.getId(), film);
        log.info("Фильм с названием {} и id {} был успешно добавлен. Текущее количество фильмов в базе: {}",
                film.getName(), film.getId(), films.size());
        return film;
    }

    public Film update(Film film) {
        log.info("Попытка обновить фильм. Текущее количество фильмов в базе: {}", films.size());
        if (!isIdExists(film)) {
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
        validateName(film);
        validateDescription(film);
        validateReleaseDate(film);
        validateDuration(film);
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

    private boolean isIdExists(Film film) {
        return films.containsKey(film.getId());
    }
}
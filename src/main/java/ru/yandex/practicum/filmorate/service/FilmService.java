package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.dao.MpaDbStorage;
import ru.yandex.practicum.filmorate.exception.DoesNotExistsException;
import ru.yandex.practicum.filmorate.exception.UnexpectedException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    private final Storage<Film> filmStorage;
    private final Storage<User> userStorage;
    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;


    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Integer id) {
         if (id == null) {
            throw new UnexpectedException("Случилась непредвиденная ошибка - передан null");
        }

        return filmStorage.findById(id);
    }

    public Film create(Film film) {
        filmValidationTest(film);

        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            film.setGenres(new HashSet<>());
            return filmStorage.create(film);
        } else {
            validateGenre(film);
        }

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (film.getId() == 0) {
            throw new DoesNotExistsException("Запрошенного фильма не существует");
        }

        findById(film.getId());


        if (film.getGenres() != null) {
            film.setGenres(new TreeSet<>(film.getGenres()));
        }

        return filmStorage.update(film);
    }

    public void add(Integer filmId, Integer userId) {
        checkFilmAndUserId(filmId, userId);
        filmStorage.add(filmId, userId);
    }

    public void remove(Integer filmId, Integer userId) {
        checkFilmAndUserId(filmId, userId);
        filmStorage.remove(filmId, userId);
    }

    public List<Film> findTopLiked(int count) {
        if (filmStorage.findAll().isEmpty()) {
            throw new DoesNotExistsException("Фильмов в базе пока нет");
        }

        return filmStorage.findAll().stream()
                .sorted((f0, f1) -> (f1.getLikes().size() - f0.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private void checkFilmAndUserId(Integer filmId, Integer userId) {
        if (filmStorage.findById(filmId) == null) {
            throw new DoesNotExistsException("Введен не существующий идентификатор фильма");
        }

        if (userStorage.findById(userId) == null) {
            throw new DoesNotExistsException("Введен не существующий идентификатор пользователя");
        }
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
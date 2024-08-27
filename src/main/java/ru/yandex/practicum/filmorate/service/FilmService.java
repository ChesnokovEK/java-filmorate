package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DoesNotExistsException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final Storage<Film> filmStorage;
    private final Storage<User> userStorage;

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(int id) {
        return filmStorage.findById(id);
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
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
}
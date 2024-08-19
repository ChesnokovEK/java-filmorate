package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DoesNotExistsException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage;

    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

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

    public void addLike(Integer filmId, Integer userId) {
        checkFilmAndUserId(filmId, userId);
        Film film = filmStorage.findById(filmId);
        film.getLikes().add(userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        checkFilmAndUserId(filmId, userId);
        Film film = filmStorage.findById(filmId);
        film.getLikes().remove(userId);
    }

    public List<Film> findTopLiked(int count) {
        if (filmStorage.findAll().isEmpty()) {
            throw new DoesNotExistsException("Фильмов в базе пока нет");
        }
        return filmStorage.findAll().stream()
                .sorted((f0, f1) -> -1 * (f0.getLikes().size() - f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private void checkFilmAndUserId(Integer filmId, Integer userId) {
        if (filmId == null || userId == null) {
            throw new DoesNotExistsException("Введен не существующий идентификатор фильма либо пользователя");
        }

        if (filmId < 1 || userId < 1) {
            throw new DoesNotExistsException("Введен не существующий идентификатор фильма либо пользователя");
        }
    }
}
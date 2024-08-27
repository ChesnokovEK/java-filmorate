package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreDbStorage {
    List<Genre> findAll();

    Genre findById(int id);

    List<Genre> findByFilmId(int filmId);

    void addGenreToTheFilm(int filmId, int genreId);

    void updateFilmGenre(Film film);
}
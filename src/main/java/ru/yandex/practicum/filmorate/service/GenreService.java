package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GenreService {
    private final GenreDbStorage genreDbStorage;

    public List<Genre> findAll() {
        return genreDbStorage.findAll();
    }

    public Genre findById(int id) {
        return genreDbStorage.findById(id);
    }
}

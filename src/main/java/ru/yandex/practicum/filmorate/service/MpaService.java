package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.MpaDbStorage;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MpaService {
    private final MpaDbStorage mpaDbStorage;

    public List<Mpa> findAll() {
        return mpaDbStorage.findAll();
    }

    public Mpa findById(int id) {
        return mpaDbStorage.findById(id);
    }
}

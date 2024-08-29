package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.MpaDbStorage;
import ru.yandex.practicum.filmorate.exception.DoesNotExistsException;
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
        if (!findAll().stream().map(Mpa::getId).toList().contains(id)) {
            throw new DoesNotExistsException("Возрастной рейтинг с идентификатором " + id + " не найден");
        }

        return mpaDbStorage.findById(id);
    }
}

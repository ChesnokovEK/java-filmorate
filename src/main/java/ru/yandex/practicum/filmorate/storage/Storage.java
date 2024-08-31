package ru.yandex.practicum.filmorate.storage;

import java.util.List;

public interface Storage<T> {

    List<T> findAll();

    T create(T obj);

    T update(T obj);

    T findById(int id);

    T add(int toId, int whichId);

    T remove(int fromId, int whichId);

}

package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.DoesNotExistsException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {
    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController(new FilmService(new InMemoryFilmStorage(), new InMemoryUserStorage()));
    }

    @Test
    void createFilmTest() {
        Film film = filmController.createFilm(new Film(0, "TestFilm", "TestFilmDescription",
                LocalDate.of(2000, 1, 1), 120));
        Film film1 = new Film(0, "TestFilm", "TestFilmDescription",
                LocalDate.of(2000, 1, 1), 120);

        assertThrows(AlreadyExistsException.class, () -> filmController.createFilm(film1), "Должен выбросить исключение");

        assertEquals(1, filmController.findAll().size());
        assertEquals(film1, new LinkedList<>(filmController.findAll()).getFirst());
    }

    @Test
    void filmValidationTest() {
        final String moreThan200Chars = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                "Donec eu dolor in turpis semper sollicitudin. Sed sollicitudin magna sed metus eleifend tempor. " +
                "Praesent mollis arcu in mollis aliquet. Nunc ligula.";
        Film film1 = new Film(0, "Test", moreThan200Chars,
                LocalDate.of(2000, 1, 1), 120);
        Film film2 = new Film(0, null, "TestFilm2Description",
                LocalDate.of(2000, 1, 1), 120);
        Film film3 = new Film(0, "TestFilm3", "TestFilm3Description",
                LocalDate.of(1000, 1, 1), 120);
        Film film4 = new Film(0, "TestFilm4", "TestFilm4Description",
                LocalDate.of(2000, 1, 1), 0);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film1), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> filmController.createFilm(film2), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> filmController.createFilm(film3), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> filmController.createFilm(film4), "Должен выбросить исключение");
    }

    @Test
    void updateFilmTest() {
        filmController.createFilm(new Film(0, "TestFilm", "TestFilmDescription",
                LocalDate.of(2000, 1, 1), 120));
        Film nonUpdatable = new Film(Integer.MAX_VALUE, "Non Updatable", "TestFilmDescription",
                LocalDate.of(2000, 1, 1), 120);

        assertThrows(DoesNotExistsException.class, () -> filmController.updateFilm(nonUpdatable), "Должен выбросить исключение");

        Film updatedFilm = filmController.updateFilm(new Film(0, "updatedTestFilm", "updatedTestFilmDescription",
                LocalDate.of(2001, 1, 1), 110));

        assertEquals(1, filmController.findAll().size(), "Должен быть только 1 фильм");
        assertEquals(updatedFilm, filmController.findAll().get(0));
    }

    @Test
    void findAllTest() {
        List<Film> emptyFilmList = filmController.findAll();

        assertTrue(emptyFilmList.isEmpty(), "Фильмы пока не были добавлены");

        Film film1 = filmController.createFilm(new Film(0, "TestFilm", "TestFilmDescription",
                LocalDate.of(2000, 1, 1), 120));
        assertEquals(1, filmController.findAll().size(), "Неверный размер списка фильмов");
        assertEquals(film1, filmController.findAll().get(0), "Сохранен не тот фильм");
    }
}

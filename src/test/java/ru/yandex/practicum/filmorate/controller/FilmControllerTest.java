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
        Film film = Film.builder()
                .id(0)
                .name("TestFilm")
                .description("TestFilmDescription")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
        filmController.createFilm(film);

        Film film1 = Film.builder()
                .id(0)
                .name("TestFilm")
                .description("TestFilmDescription")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        assertThrows(AlreadyExistsException.class, () -> filmController.createFilm(film1), "Должен выбросить исключение");

        assertEquals(1, filmController.findAll().size());
        assertEquals(film1, new LinkedList<>(filmController.findAll()).getFirst());
    }

    @Test
    void filmValidationTest() {
        final String moreThan200Chars = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                "Donec eu dolor in turpis semper sollicitudin. Sed sollicitudin magna sed metus eleifend tempor. " +
                "Praesent mollis arcu in mollis aliquet. Nunc ligula.";
        Film film1 = Film.builder()
                .id(0)
                .name("TestFilm1")
                .description(moreThan200Chars)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
        Film film2 = Film.builder()
                .id(0)
                .name(null)
                .description("TestFilm2Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
        Film film3 = Film.builder()
                .id(0)
                .name("TestFilm3")
                .description("TestFilm3Description")
                .releaseDate(LocalDate.of(1000, 1, 1))
                .duration(120)
                .build();
        Film film4 = Film.builder()
                .id(0)
                .name("TestFilm4")
                .description("TestFilm4Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(0)
                .build();

        assertThrows(ValidationException.class, () -> filmController.createFilm(film1), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> filmController.createFilm(film2), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> filmController.createFilm(film3), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> filmController.createFilm(film4), "Должен выбросить исключение");
    }

    @Test
    void updateFilmTest() {
        Film updatable = Film.builder()
                .id(0)
                .name("TestFilm")
                .description("TestFilmDescription")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
        filmController.createFilm(updatable);
        Film nonUpdatable = Film.builder()
                .id(Integer.MAX_VALUE)
                .name("Non Updatable")
                .description("TestFilmDescription")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        assertThrows(DoesNotExistsException.class, () -> filmController.updateFilm(nonUpdatable), "Должен выбросить исключение");

        Film updatedFilm = filmController.updateFilm(
                Film.builder()
                        .id(0)
                        .name("updatedTestFilm")
                        .description("updatedTestFilmDescription")
                        .releaseDate(LocalDate.of(2000, 1, 1))
                        .duration(110)
                        .build()
        );

        assertEquals(1, filmController.findAll().size(), "Должен быть только 1 фильм");
        assertEquals(updatedFilm, filmController.findAll().get(0));
    }

    @Test
    void findAllTest() {
        List<Film> emptyFilmList = filmController.findAll();

        assertTrue(emptyFilmList.isEmpty(), "Фильмы пока не были добавлены");

        Film film1 = Film.builder()
                .id(0)
                .name("TestFilm")
                .description("TestFilmDescription")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        filmController.createFilm(film1);

        assertEquals(1, filmController.findAll().size(), "Неверный размер списка фильмов");
        assertEquals(film1, filmController.findAll().get(0), "Сохранен не тот фильм");
    }
}

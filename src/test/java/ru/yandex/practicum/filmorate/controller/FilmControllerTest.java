package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dao.Impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.exception.DoesNotExistsException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase
@JdbcTest
@ComponentScan("ru.yandex.practicum.filmorate")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(value = "/clear.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Sql(value = {"/schema.sql","/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class FilmControllerTest {
    @Autowired
    private FilmController filmController;
    @Autowired
    private FilmDbStorage filmDbStorage;

    @Test
    void createFilmTest() {
        Film film = new Film(
                0,
                "TestFilm",
                "TestFilmDescription",
                LocalDate.of(2000, 1, 1),
                120,
                new Mpa(1, "G"),
                new HashSet<>(),
                new HashSet<>()
        );

        Film film1 = new Film(
                1,
                "TestFilm",
                "TestFilmDescription",
                LocalDate.of(2000, 1, 1),
                120,
                new Mpa(1, "G"),
                new HashSet<>(),
                new HashSet<>()
        );

        filmController.createFilm(film);

        assertEquals(1, filmController.findAll().size());
        assertEquals(film1, new LinkedList<>(filmController.findAll()).getFirst());
    }

    @Test
    void filmValidationTest() {
        final String moreThan200Chars = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                "Donec eu dolor in turpis semper sollicitudin. Sed sollicitudin magna sed metus eleifend tempor. " +
                "Praesent mollis arcu in mollis aliquet. Nunc ligula.";
        Film film1 = new Film(
                0,
                "TestFilm",
                moreThan200Chars,
                LocalDate.of(2000, 1, 1),
                120,
                new Mpa(1, "G"),
                new HashSet<>(),
                new HashSet<>()
        );

        Film film2 = new Film(
                0,
                null,
                "TestFilm2Description",
                LocalDate.of(2000, 1, 1),
                120,
                new Mpa(1, "G"),
                new HashSet<>(),
                new HashSet<>()
        );
        Film film3 = new Film(
                0,
                "TestFilm3",
                "TestFilm3Description",
                LocalDate.of(1000, 1, 1),
                120,
                new Mpa(1, "G"),
                new HashSet<>(),
                new HashSet<>()
        );
        Film film4 = new Film(
                0,
                "TestFilm4",
                "TestFilm4Description",
                LocalDate.of(1000, 1, 1),
                120,
                new Mpa(1, "G"),
                new HashSet<>(),
                new HashSet<>()
        );

        assertThrows(ValidationException.class, () -> filmController.createFilm(film1), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> filmController.createFilm(film2), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> filmController.createFilm(film3), "Должен выбросить исключение");
        assertThrows(ValidationException.class, () -> filmController.createFilm(film4), "Должен выбросить исключение");
    }

    @Test
    void updateFilmTest() {
        Film updatable = new Film(
                0,
                "TestFilm",
                "TestFilmDescription",
                LocalDate.of(2000, 1, 1),
                120,
                new Mpa(1, "G"),
                new HashSet<>(),
                new HashSet<>()
        );
        filmController.createFilm(updatable);
        Film nonUpdatable = new Film(
                Integer.MAX_VALUE,
                "Non Updatable",
                "TestFilmDescription",
                LocalDate.of(2000, 1, 1),
                120,
                new Mpa(1, "G"),
                new HashSet<>(),
                new HashSet<>()
        );

        assertThrows(DoesNotExistsException.class, () -> filmController.updateFilm(nonUpdatable), "Должен выбросить исключение");

        updatable.setDuration(110);
        Film updatedFilm = filmController.updateFilm(updatable);

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
                .mpa(new Mpa(1, "G"))
                .likes(new HashSet<>())
                .genres(new HashSet<>())
                .build();

        filmController.createFilm(film1);

        assertEquals(1, filmController.findAll().size(), "Неверный размер списка фильмов");
        assertEquals(film1, filmController.findAll().get(0), "Сохранен не тот фильм");
    }
}

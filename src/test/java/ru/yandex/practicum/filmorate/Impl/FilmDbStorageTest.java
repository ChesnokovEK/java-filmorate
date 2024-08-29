package ru.yandex.practicum.filmorate.Impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dao.Impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@AutoConfigureTestDatabase
@JdbcTest
@ComponentScan("ru.yandex.practicum.filmorate")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(value = {"/schema.sql", "/testing.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/clear.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;

    @Test
    public void testFindFilmById() {
        Film film = filmStorage.findById(1);

        assertThat(film)
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("likes", new HashSet<>(List.of(1, 2, 3, 4, 5)));
    }

    @Test
    public void testFindAllFilms() {
        List<Film> films = filmStorage.findAll();

        assertThat(films).size().isEqualTo(5);
    }

    @Test
    public void testFilmCreation() {
        Film film = Film.builder()
                .name("TestFilm6")
                .description("TestFilm6Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .mpa(new Mpa(1, "G"))
                .genres(new HashSet<>())
                .duration(120)
                .build();

        film = filmStorage.create(film);
        assertThat(film).hasFieldOrPropertyWithValue("id", 6);
        Film filmFromDb = filmStorage.findById(film.getId());
        assertThat(filmFromDb).hasFieldOrPropertyWithValue("id", 6);
    }

    @Test
    public void testFilmUpdate() {
        Film film = Film.builder()
                .id(2)
                .name("TestFilm7")
                .description("TestFilm7Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .mpa(new Mpa(1, "G"))
                .genres(new HashSet<>(List.of(new Genre(1, "Комедия"))))
                .duration(120)
                .build();

        Film updatedFilm = filmStorage.update(film);
        assertThat(updatedFilm).hasFieldOrPropertyWithValue("id", 2);
    }

    @Test
    public void testAddLike() {
        filmStorage.add(4, 4);

        Film film = filmStorage.findById(4);
        List<Integer> likes = film.getLikes().stream().toList();
        assertThat(likes).size().isEqualTo(2);
        assertThat(likes.get(likes.size() - 1)).isEqualTo(4);
    }

    @Test
    public void testsRemoveLike() {
        int likesNumberBeforeRemoval = filmStorage.findById(1).getLikes().size();
        filmStorage.remove(1, 1);

        assertThat(filmStorage.findById(1).getLikes()).size().isEqualTo(likesNumberBeforeRemoval - 1);
        assertThat(filmStorage.findById(1).getLikes()).doesNotContain(1);
    }
}

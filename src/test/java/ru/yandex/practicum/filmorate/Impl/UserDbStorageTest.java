package ru.yandex.practicum.filmorate.Impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dao.Impl.UserDbStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase
@JdbcTest
@ComponentScan("ru.yandex.practicum.filmorate")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(value = {"/schema.sql", "/testing.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/clear.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UserDbStorageTest {
    private final UserDbStorage userStorage;

    @Test
    public void testFindUserById() {
        User user = userStorage.findById(1);

        assertThat(user).hasFieldOrPropertyWithValue("id", 1);
    }

    @Test
    public void testFindAllUsers() {
        List<User> users = userStorage.findAll();

        assertThat(users).size().isEqualTo(5);
    }

    @Test
    public void testCreateUser() {
        User user = User.builder()
                .email("test@testmail.test")
                .login("testUserLogin")
                .name("Foo Bar")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User newUser = userStorage.create(user);
        assertThat(newUser).hasFieldOrPropertyWithValue("id", 6);

        User userFromDb = userStorage.findById(6);
        assertThat(userFromDb).hasFieldOrPropertyWithValue("id", 6);
    }

    @Test
    public void testUpdateUser() {
        User user = User.builder()
                .id(5)
                .email("test@testmail.test")
                .login("testUserLogin")
                .name("Foo Bar")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User userFromDb = userStorage.update(user);

        assertThat(userFromDb.getId()).isEqualTo(5);
    }

    @Test
    public void testAddFriend() {
        User user = userStorage.findById(4);
        System.out.println(user);

        userStorage.add(3, 4);
        User user1 = userStorage.findById(3);
        user = userStorage.findById(4);

        assertThat(user1.getFriends()).contains(4);
        assertThat(user.getFriends()).doesNotContain(3);
    }

    @Test
    public void testFindAllFriends() {
        Set<Integer> friends = userStorage.findById(2).getFriends();

        assertThat(friends.stream().toList()).contains(3, 4);
    }

    @Test
    public void testDeleteFriend() {
        User user = userStorage.findById(1);
        assertThat(user.getFriends()).contains(3);

        userStorage.remove(1, 3);
        User user1 = userStorage.findById(1);
        assertThat(user1.getFriends()).doesNotContain(3);
    }
}

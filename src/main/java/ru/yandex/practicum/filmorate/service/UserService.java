package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.DoesNotExistsException;
import ru.yandex.practicum.filmorate.exception.UnexpectedException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final Storage<User> userStorage;

    public List<User> findAll() {
        return new ArrayList<>(userStorage.findAll());
    }

    public User findById(int id) {
        try {
            User user = userStorage.findById(id);

            if (user == null) {
                throw new UnexpectedException("Произошла непредвиденная ошибка при создании пользователя");
            }

            return user;

        } catch (EmptyResultDataAccessException e) {
            throw new DoesNotExistsException("Пользователь с id " + id + " не найден");
        }
    }

    public List<User> findUserFriends(int userId) {
        List<User> users = findAll();
        Set<Integer> friendsId = new HashSet<>();
        List<User> friends;

        validateUserExists(users, userId);

        for (User user : users) {
            if (user.getId() == userId) {
                friendsId.addAll(user.getFriends());
            }
        }

        friends = users.stream().filter(user -> friendsId.contains(user.getId())).toList();

        return friends;
    }

    public User createUser(User user) {
        if (user.getId() != null && user.getId() > 0) {
            throw new AlreadyExistsException("Пользователь уже существует");
        }
        userValidationTest(user);

        return userStorage.create(user);
    }

    public User updateUser(User user) {
        List<User> users = findAll();
        validateUserExists(users, user.getId());
        userValidationTest(user);
        return userStorage.update(user);
    }

    public void addFriend(int userId, int friendId) {
        List<User> users = findAll();
        validateUserExists(users, userId);
        validateUserExists(users, friendId);

        userStorage.add(userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        List<User> users = findAll();
        validateUserExists(users, userId);
        validateUserExists(users, friendId);

        userStorage.remove(userId, friendId);
    }

    public List<User> findMutualFriends(int user1Id, int user2Id) {
        Set<Integer> user1Friends = userStorage.findById(user1Id).getFriends();
        Set<Integer> user2Friends = userStorage.findById(user2Id).getFriends();

        if (user1Friends.isEmpty() || user2Friends.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> mutualFriendsId = user1Friends.stream()
                .filter(user2Friends::contains)
                .collect(Collectors.toList());

        List<User> mutualFriends = new ArrayList<>();
        for (int id : mutualFriendsId) {
            mutualFriends.add(userStorage.findById(id));
        }
        return mutualFriends;
    }

    private void userValidationTest(User user) {
        emailValidation(user);
        loginValidation(user);
        birthdayValidation(user);
        setLoginAsNameIfBlankOrNull(user);
    }

    private void emailValidation(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.info("Выполнение метода прервано: некорректный email. Указанный email: {}", user.getEmail());
            throw new ValidationException("Некорректный адрес электронной почты");
        }
    }

    private void loginValidation(User user) {
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.info("Выполнение метода прервано: некорректный логин. Указанный логин: {}", user.getLogin());
            throw new ValidationException("Логин не может быть пустым или содержать пробелы");
        }
    }

    private void birthdayValidation(User user) {
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.info("Выполнение метода прервано: некорректная дата рождения пользователя. Указанная дата: {}", user.getBirthday());
            throw new ValidationException("Указана неправильная дата рождения");
        }
    }

    private void setLoginAsNameIfBlankOrNull(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Пользователь не указал имя. Имени присвоено значение логина {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }

    private void validateUserExists(List<User> userList, int userId) {
        if (!userList.stream().map(User::getId).toList().contains(userId)) {
            throw new DoesNotExistsException("Пользователь не существует");
        }
    }
}
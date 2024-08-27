package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final Storage<User> userStorage;

    public List<User> findAll() {
        return new ArrayList<>(userStorage.findAll());
    }

    public User findById(int id) {
        return userStorage.findById(id);
    }

    public List<User> findUserFriends(int userId) {
        List<User> friends = new ArrayList<>();

        for (int id : userStorage.findById(userId).getFriends()) {
            friends.add(userStorage.findById(id));
        }

        return friends;
    }

    public User createUser(User user) {
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        return userStorage.update(user);
    }

    public void addFriend(int user1Id, int user2Id) {
        userStorage.add(user1Id, user2Id);
    }

    public void removeFriend(Integer user1Id, Integer user2Id) {
        userStorage.remove(user1Id, user2Id);
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
}
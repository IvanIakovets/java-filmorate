package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User createUser(User user) {
        log.info("Сервис: запрос на создание пользователя с email: {}", user.getEmail());

        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Сервис: имя не задано, будет использован логин");
            user.setName(user.getLogin());
        }

        User createdUser = userStorage.createUser(user);
        log.info("Сервис: пользователь создан с id: {}", createdUser.getId());
        return createdUser;
    }

    public boolean deleteUser(Long userId) {
        log.info("Сервис: запрос на удаление пользователя с id: {}", userId);
        return userStorage.deleteUser(userId);
    }

    public User updateUser(User user) {
        log.info("Сервис: запрос на изменение данных пользователя с id: {}", user.getId());
        return userStorage.updateUser(user);
    }

    public Collection<User> findAllUsers() {
        log.info("Сервис: запрос на получение списка пользователей.");
        return userStorage.findAllUsers();
    }

    public User findUserById(Long userId) {
        log.info("Сервис: запрос на получение данных пользователя списка по id: {}", userId);
        return userStorage.findUserById(userId);
    }

    public boolean addFriend(Long userId, Long friendId) {
        log.info("Друг добавлен в список друзей пользователя");
        return userStorage.addFriend(userId, friendId);
    }

    public boolean deleteFriend(Long userId, Long friendId) {
        log.info("Друг удален из списка друзей пользователя");
        return userStorage.deleteFriend(userId, friendId);
    }

    public Collection<User> getFriendsList(Long userId) {
        User user = userStorage.findUserById(userId);
        if (user.getUserFriends() == null || user.getUserFriends().isEmpty()) {
            log.info("Вернулся пустой список друзей пользователя.");
            return new HashSet<>();
        }
        log.info("Вернулся список друзей пользователя.");
        Set<User> friendList = new HashSet<>();
        for (Long id : user.getUserFriends()) {
            friendList.add(userStorage.findUserById(id));
        }
        return friendList;
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        User user = userStorage.findUserById(userId);
        User otherUser = userStorage.findUserById(otherId);

        Set<Long> userFriends = user.getUserFriends();
        Set<Long> otherUserFriends = otherUser.getUserFriends();

        Set<User> friendList = new HashSet<>();
        Set<Long> commonFriends = new HashSet<>(userFriends);
        commonFriends.retainAll(otherUserFriends);

        for (Long regId : commonFriends) {
            friendList.add(userStorage.findUserById(regId));
        }

        return friendList;
    }
}

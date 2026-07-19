package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

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

    public boolean checkUserId(Long userId) {
        log.info("Сервис: проверка пользователя по id: {}", userId);
        return userStorage.checkUserId(userId);
    }

    public boolean addFriend(Long userId, Long friendId) {
        checkUserId(userId);
        checkUserId(friendId);
        log.info("Друг добавлен в список друзей пользователя");
        return userStorage.addFriend(userId, friendId);
    }

    public boolean deleteFriend(Long userId, Long friendId) {
        checkUserId(userId);
        checkUserId(friendId);
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
        for (Long id : userStorage.findUserById(userId).getUserFriends()) {
            friendList.add(userStorage.findUserById(id));
        }
        return friendList;
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        User user = userStorage.findUserById(id);
        User otherUser = userStorage.findUserById(otherId);

        Set<Long> userFriends = user.getUserFriends() != null
                ? user.getUserFriends()
                : Collections.emptySet();
        Set<Long> otherUserFriends = otherUser.getUserFriends() != null
                ? otherUser.getUserFriends()
                : Collections.emptySet();

        Set<User> friendList = new HashSet<>();
        Set<Long> commonFriends = new HashSet<>(userFriends);
        commonFriends.retainAll(otherUserFriends);

        for (Long i : commonFriends) {
            friendList.add(userStorage.findUserById(i));
        }

        return friendList;
    }
}

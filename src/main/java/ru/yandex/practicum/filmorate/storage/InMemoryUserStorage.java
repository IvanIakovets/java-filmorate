package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeptions.DuplicateDataException;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Repository
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User createUser(User user) {
        for (User us : users.values()) {
            if (us.getEmail().equals(user.getEmail())) {
                log.error("Попытка добавить пользователя с дублирующим email {}", user.getEmail());
                throw new DuplicateDataException("Пользователь с email " + user.getEmail() + " уже существует");
            }
            if (us.getLogin().equals(user.getLogin())) {
                log.error("Попытка добавить пользователя с дублирующим логином {}", user.getLogin());
                throw new DuplicateDataException("Пользователь с логином " + user.getEmail() + " уже существует");
            }
        }
        user.setId(getNextId());
        log.info("Пользователю выдан id: {}", user.getId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public boolean deleteUser(Long userId) {
        if (users.containsKey(userId)) {
            users.remove(userId);
            return true;
        } else {
            log.error("Пользователь не найден. id: {}", userId);
            throw new NotFoundException("Пользователь с данным ID: " + userId + " не найден");
        }
    }

    @Override
    public User updateUser(User user) {
        Long userToChangeId = user.getId(); // ID - пользователя которого хотим изменить
        log.info("Запрос на изменение данных пользователя: id {}", userToChangeId);
        if (!users.containsKey(userToChangeId)) {
            log.error("Пользователь не найден. id: {}", userToChangeId);
            throw new NotFoundException("Пользователь с данным ID: " + userToChangeId + " не обнаружен");
        }
        for (User us : users.values()) {
            if (!us.getId().equals(userToChangeId) &&
                    us.getEmail().equals(user.getEmail())) {
                log.error("Попытка добавить пользователя с дублирующим email {}", user.getEmail());
                throw new DuplicateDataException("Пользователь с email " + user.getEmail() + " уже существует");
            }
            if (!us.getId().equals(userToChangeId) &&
                    us.getLogin().equals(user.getLogin())) {
                log.error("Попытка добавить пользователя с дублирующим логином {}", user.getLogin());
                throw new DuplicateDataException("Пользователь с логином " + user.getLogin() + " уже существует");
            }
        }
        User oldUser = users.get(userToChangeId);
        log.info("Старт замены данных пользователя");
        if (user.getName() != null) {
            oldUser.setName(user.getName());
            log.info("Имя пользователя успешно изменено");
        }
        if (user.getEmail() != null) {
            oldUser.setEmail(user.getEmail());
            log.info("Email пользователя успешно изменен");
        }
        if (user.getLogin() != null) {
            oldUser.setLogin(user.getLogin());
            log.info("Логин пользователя успешно изменен");
        }
        if (user.getBirthday() != null) {
            oldUser.setBirthday(user.getBirthday());
            log.info("День рожденье пользователя успешно изменено");
        }
        log.info("Данные пользователя успешно обновлены");
        return oldUser;
    }

    @Override
    public Collection<User> findAllUsers() {
        if (users.isEmpty()) {
            log.error("Пользователи не найдены.");
            return new HashSet<>();
        }
        return users.values();
    }

    @Override
    public User findUserById(Long id) {
        if (!users.containsKey(id)) {
            log.error("Пользователь не найден. id: {}", id);
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
        return users.get(id);
    }

    @Override
    public boolean checkUserId(Long userId) {
        if (users.containsKey(userId)) {
            return true;
        } else {
            log.error("Пользователь не найден. id: {}", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }

    @Override
    public boolean addFriend(Long userId, Long friendId) {
        checkUserId(userId);
        checkUserId(friendId);
        Set<Long> userFriends =  users.get(userId).getUserFriends();
        if (userFriends == null) {
            userFriends = new HashSet<>();
        }
        userFriends.add(friendId);
        Set<Long> friendFriends = users.get(friendId).getUserFriends();
        if (friendFriends == null) {
            friendFriends = new HashSet<>();
        }
        friendFriends.add(userId);
        users.get(userId).setUserFriends(userFriends);
        users.get(friendId).setUserFriends(friendFriends);
        return true;
    }

    @Override
    public boolean deleteFriend(Long userId, Long friendId) {
        checkUserId(userId);
        checkUserId(friendId);

        Set<Long> userFriends = Optional.ofNullable(users.get(userId).getUserFriends())
                .orElse(new HashSet<>());
        userFriends.remove(friendId);
        Set<Long> friendFriends = Optional.ofNullable(users.get(friendId).getUserFriends())
                .orElse(new HashSet<>());
        friendFriends.remove(userId);
        users.get(userId).setUserFriends(userFriends);
        users.get(friendId).setUserFriends(friendFriends);
        return true;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}

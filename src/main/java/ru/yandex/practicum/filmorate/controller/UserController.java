package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.DuplicateDataException;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validations.ValidationGroups;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAllUsers() {
        log.info("Добавлен новый пользователь. Всего пользователей {}", users.size());
        return users.values();
    }

    @GetMapping("/{id}")
    public User findUserById(@PathVariable Long id) {
        log.info("Запрос на получение пользователя по ID: {}", id);
        if (!users.containsKey(id)) {
            log.error("Пользователь не найден. id: {}", id);
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
        return users.get(id);
    }

    @PostMapping
    public User createUser(@Validated(ValidationGroups.Create.class) @RequestBody User user) {
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
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Логин пользователя используется в качестви имени");
            user.setName(user.getLogin());
        }
        user.setId(getNextId());
        log.info("Пользователю выдан id: {}", user.getId());
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User modifyUser(@Validated(ValidationGroups.Update.class) @RequestBody User user) {
        Long userToChangeId = user.getId(); // ID - пользователя которого хотим изменить
        log.info("Запрос на изменение данных существующего фильма: id {} title {}",
                userToChangeId, users.get(userToChangeId).getName());
        /*if (user.getId() == null) {
            log.error("Не указан id пользователя");
            throw new ConditionsNotMetException("Id должен быть указан");
        }*/
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
                throw new DuplicateDataException("Пользователь с логином " + user.getEmail() + " уже существует");
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

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}

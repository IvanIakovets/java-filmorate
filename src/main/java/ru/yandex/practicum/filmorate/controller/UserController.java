package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exeptions.DuplicateDataException;
import ru.yandex.practicum.filmorate.model.User;

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

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
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
    public User modifyUser(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            log.error("Не указан id пользователя");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (!users.containsKey(newUser.getId())) {
            log.error("Пользователь не найден. id: {}", newUser.getId());
            throw new ConditionsNotMetException("Пользователь с данным ID: " + newUser.getId() + " не обнаружен");
        }
        for (User us : users.values()) {
            if (!us.getId().equals(newUser.getId()) &&
                    us.getEmail().equals(newUser.getEmail())) {
                log.error("Попытка добавить пользователя с дублирующим email {}", newUser.getEmail());
                throw new DuplicateDataException("Пользователь с email " + newUser.getEmail() + " уже существует");
            }
            if (!us.getId().equals(newUser.getId()) &&
                    us.getLogin().equals(newUser.getLogin())) {
                log.error("Попытка добавить пользователя с дублирующим логином {}", newUser.getLogin());
                throw new DuplicateDataException("Пользователь с логином " + newUser.getEmail() + " уже существует");
            }
        }
        User oldUser = users.get(newUser.getId());
        log.info("Старт замены данных пользователя");

        oldUser.setName(newUser.getName());
        oldUser.setEmail(newUser.getEmail());
        oldUser.setLogin(newUser.getLogin());
        oldUser.setBirthday(newUser.getBirthday());
        log.info("Данные пользователя успешно изменены");

        if (newUser.getName() == null || newUser.getName().isBlank()) {
            log.info("Логин пользователя используется в качестви имени");
            oldUser.setName(oldUser.getLogin());
        }
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

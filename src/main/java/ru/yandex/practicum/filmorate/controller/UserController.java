package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.validations.ValidationGroups;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<User> findAllUsers() {
        log.info("Получен запрос на отправку списка всех пользователей. Всего пользователей {}", userService.findAllUsers().size());
        return userService.findAllUsers();
    }

    @GetMapping("/{id}")
    public User findUserById(@PathVariable("id") Long userId) {
        log.info("Запрос на получение пользователя по ID: {}", userId);
        return userService.findUserById(userId);
    }

    @PostMapping
    public User createUser(@Validated(ValidationGroups.Create.class) @RequestBody User user) {
        log.info("Запрос на добавление пользователя.");
        return userService.createUser(user);
    }

    @PutMapping
    public User modifyUser(@Validated(ValidationGroups.Update.class) @RequestBody User user) {
        log.info("Запрос на изменение пользователя.");
        return userService.updateUser(user);
    }

    @DeleteMapping("/{id}")
    public boolean deleteUser(@PathVariable("id") Long userId) {
        log.info("Запрос на удаление пользователя.");
        return userService.deleteUser(userId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public boolean addFriend(@PathVariable("id") Long userId, @PathVariable Long friendId) {
        log.info("Запрос на добавление друга.");
        return userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public boolean deleteFriend(@PathVariable("id") Long userId, @PathVariable Long friendId) {
        log.info("Запрос на удаление друга.");
        return userService.deleteFriend(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriendsList(@PathVariable("id") Long userId) {
        log.info("Запрос на получение списка друзей пользователя {}", userId);
        return userService.getFriendsList(userId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriendsList(@PathVariable("id") Long userId, @PathVariable Long otherId) {
        log.info("Запрос на получение списка общих друзей пользователя {} с пользователем {}", userId, otherId);
        return userService.getCommonFriends(userId,otherId);
    }
}

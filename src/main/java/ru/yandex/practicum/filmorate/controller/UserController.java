package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.UserResponse;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.validations.ValidationGroups;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<UserResponse> findAllUsers() {
        log.info("Получен запрос на отправку списка всех пользователей. Всего пользователей {}", userService.findAllUsers().size());
        return userService.findAllUsers().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
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
    public void deleteUser(@PathVariable("id") Long userId) {
        log.info("Запрос на удаление пользователя.");
        userService.deleteUser(userId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable("id") Long userId, @PathVariable Long friendId) {
        log.info("Запрос на добавление друга.");
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable("id") Long userId, @PathVariable Long friendId) {
        log.info("Запрос на удаление друга.");
        userService.deleteFriend(userId, friendId);
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

    // Получить входящие заявки
    @GetMapping("/{id}/friends/requests/incoming")
    public List<Friendship> getIncomingRequests(@PathVariable("id") Long userId) {
        log.info("Запрос на получение входящих заявок для пользователя {}", userId);
        return userService.getIncomingRequests(userId);
    }

    // Получить исходящие заявки
    @GetMapping("/{id}/friends/requests/outgoing")
    public List<Friendship> getOutgoingRequests(@PathVariable("id") Long userId) {
        log.info("Запрос на получение исходящих заявок от пользователя {}", userId);
        return userService.getOutgoingRequests(userId);
    }

    // Подтвердить заявку
    @PutMapping("/{id}/friends/requests/{friendshipId}/confirm")
    public void confirmFriend(@PathVariable("id") Long userId,
                                 @PathVariable Long friendshipId) {
        log.info("Запрос на подтверждение заявки {} от пользователя {}", friendshipId, userId);
        userService.confirmFriend(friendshipId, userId);
    }

    // Отклонить заявку
    @DeleteMapping("/{id}/friends/requests/{friendshipId}/decline")
    public void declineFriend(@PathVariable("id") Long userId,
                              @PathVariable Long friendshipId) {
        log.info("Запрос на отклонение заявки {} от пользователя {}", friendshipId, userId);
        userService.declineFriend(friendshipId, userId);
    }

    // Отозвать заявку
    @DeleteMapping("/{id}/friends/requests/{friendshipId}/cancel")
    public void cancelFriendRequest(@PathVariable("id") Long userId,
                                    @PathVariable Long friendshipId) {
        log.info("Запрос на отзыв заявки {} от пользователя {}", friendshipId, userId);
        userService.cancelFriendRequest(friendshipId, userId);
    }

    private UserResponse convertToResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName())
                .birthday(user.getBirthday())
                .friends(user.getUserFriends())
                .build();
    }
}

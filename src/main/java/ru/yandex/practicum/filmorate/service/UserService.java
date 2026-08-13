package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserResponse;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipService friendshipService;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       FriendshipService friendshipService) {
        this.userStorage = userStorage;
        this.friendshipService = friendshipService;
    }

    public UserResponse createUser(User user) {
        log.info("Сервис: запрос на создание пользователя с email: {}", user.getEmail());

        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Сервис: имя не задано, будет использован логин");
            user.setName(user.getLogin());
        }

        User createdUser = userStorage.createUser(user);
        log.info("Сервис: пользователь создан с id: {}", createdUser.getId());
        return convertToResponse(createdUser);
    }

    public void deleteUser(Long userId) {
        log.info("Сервис: запрос на удаление пользователя с id: {}", userId);
        userStorage.deleteUser(userId);
    }

    public UserResponse updateUser(User user) {
        log.info("Сервис: запрос на изменение данных пользователя с id: {}", user.getId());
        User updatedUser = userStorage.updateUser(user);
        return convertToResponse(updatedUser);
    }

    public Collection<UserResponse> findAllUsers() {
        log.info("Сервис: запрос на получение списка пользователей.");
        return userStorage.findAllUsers().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse findUserById(Long userId) {
        log.info("Сервис: запрос на получение данных пользователя списка по id: {}", userId);
        User user = userStorage.findUserById(userId);
        return convertToResponse(user);
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("UserService: добавление друга {} пользователю {}", friendId, userId);
        friendshipService.sendFriendRequest(userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        log.info("UserService: удаление друга {} у пользователя {}", friendId, userId);
        friendshipService.removeFriendship(userId, friendId);
    }

    public Collection<UserResponse> getFriendsList(Long userId) {
        User user = userStorage.findUserById(userId);
        if (user.getUserFriends() == null || user.getUserFriends().isEmpty()) {
            log.info("Вернулся пустой список друзей пользователя.");
            return new HashSet<>();
        }
        log.info("Вернулся список друзей пользователя.");
        Set<UserResponse> friendList = new HashSet<>();
        for (Long id : user.getUserFriends()) {
            User userFriend = userStorage.findUserById(id);
            friendList.add(convertToResponse(userFriend));
        }
        return friendList;
    }

    public Collection<UserResponse> getCommonFriends(Long userId, Long otherId) {
        User user = userStorage.findUserById(userId);
        User otherUser = userStorage.findUserById(otherId);

        Set<Long> userFriends = user.getUserFriends();
        Set<Long> otherUserFriends = otherUser.getUserFriends();

        Set<UserResponse> friendList = new HashSet<>();
        Set<Long> commonFriends = new HashSet<>(userFriends);
        commonFriends.retainAll(otherUserFriends);

        for (Long regId : commonFriends) {
            User userFriend = userStorage.findUserById(regId);
            friendList.add(convertToResponse(userFriend));
        }

        return friendList;
    }

    public void confirmFriend(Long friendshipId, Long userId) {
        friendshipService.confirmFriendship(friendshipId, userId);
    }

    public void declineFriend(Long friendshipId, Long userId) {
        friendshipService.declineFriendship(friendshipId, userId);
    }

    public void cancelFriendRequest(Long friendshipId, Long userId) {
        friendshipService.cancelFriendRequest(friendshipId, userId);
    }

    public List<Friendship> getIncomingRequests(Long userId) {
        return friendshipService.getIncomingRequests(userId);
    }

    public List<Friendship> getOutgoingRequests(Long userId) {
        return friendshipService.getOutgoingRequests(userId);
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

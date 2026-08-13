package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

//методы добавления, удаления и модификации объектов.
public interface UserStorage {

    User createUser(User user);

    void deleteUser(Long userId);

    User updateUser(User user);

    Collection<User> findAllUsers();

    User findUserById(Long userId);

    void addFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    default void saveFriends(User user) {
    }
}

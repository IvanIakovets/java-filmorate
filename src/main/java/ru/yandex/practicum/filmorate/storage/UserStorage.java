package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

//методы добавления, удаления и модификации объектов.
public interface UserStorage {

    User createUser(User user);

    boolean deleteUser(Long userId);

    User updateUser(User user);

    Collection<User> findAllUsers();

    User findUserById(Long userId);

    boolean checkUserId(Long userId);

    boolean addFriend(Long userId, Long friendId);

    boolean deleteFriend(Long userId, Long friendId);
}

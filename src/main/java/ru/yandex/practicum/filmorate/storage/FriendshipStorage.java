package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Friendship;

import java.util.List;
import java.util.Optional;

public interface FriendshipStorage {
    // Создать запись
    Friendship create(Friendship friendship);

    // Найти по ID
    Optional<Friendship> findById(Long id);

    // Найти между двумя пользователями (в любом направлении)
    Optional<Friendship> findByUsers(Long userId1, Long userId2);

    // Обновить запись
    Friendship update(Friendship friendship);

    // Удалить по ID
    void delete(Long id);

    // Удалить все записи между двумя пользователями
    void deleteByUsers(Long userId1, Long userId2);

    // Найти все PENDING заявки для пользователя (входящие)
    List<Friendship> findPendingRequests(Long addresseeId);

    // Найти все PENDING заявки от пользователя (исходящие)
    List<Friendship> findSentRequests(Long requesterId);
}

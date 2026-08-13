package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FriendshipService {
    private final FriendshipStorage friendshipStorage;
    private final UserStorage userStorage;

    public FriendshipService(@Qualifier("friendshipDbStorage") FriendshipStorage friendshipStorage,
                             @Qualifier("userDbStorage") UserStorage userStorage) {
        this.friendshipStorage = friendshipStorage;
        this.userStorage = userStorage;
    }

    @Transactional
    public void sendFriendRequest(Long requesterId, Long addresseeId) {
        log.info("Отправка заявки в друзья: от {} к {}", requesterId, addresseeId);

        validateUserExist(requesterId, addresseeId);
        validateNotSameUser(requesterId, addresseeId);

        Optional<Friendship> existing = friendshipStorage.findByUsers(requesterId, addresseeId);

        if (existing.isPresent()) {
            Friendship friendship = existing.get();

            if (friendship.getStatus() == Friendship.FriendshipStatus.CONFIRMED) {
                log.error("Пользователи уже друзья: {} и {}", requesterId, addresseeId);
                throw new ConditionsNotMetException("Пользователи уже являются друзьями");
            }

            if (friendship.getStatus() == Friendship.FriendshipStatus.PENDING) {
                // Если заявка от текущего пользователя
                if (friendship.getRequesterId().equals(requesterId)) {
                    log.error("Заявка уже отправлена от {} к {}", requesterId, addresseeId);
                    throw new ConditionsNotMetException("Заявка уже отправлена");
                }

                // Если встречная заявка — автоподтверждение
                if (friendship.getRequesterId().equals(addresseeId)) {
                    log.info("Обнаружена встречная заявка, выполняем автоподтверждение");
                    confirmFriendship(friendship.getId(), addresseeId);
                }
            }
        }

        // Создаем новую заявку
        Friendship newFriendship = Friendship.builder()
                .requesterId(requesterId)
                .addresseeId(addresseeId)
                .status(Friendship.FriendshipStatus.PENDING)
                .build();

        Friendship created = friendshipStorage.create(newFriendship);
        log.info("Создана заявка с id {}", created.getId());

        // ОТПРАВИТЕЛЬ СРАЗУ ДОБАВЛЯЕТ АДРЕСАТА В СВОЙ СПИСК ДРУЗЕЙ
        addFriendToUser(requesterId, addresseeId);
    }

    public Friendship confirmFriendship(Long friendshipId, Long userId) {
        log.debug("Подтверждение заявки: friendshipId={}, userId={}", friendshipId, userId);

        Friendship friendship = friendshipStorage.findById(friendshipId)
                .orElseThrow(() -> {
                    log.error("Заявка с id {} не найдена", friendshipId);
                    return new NotFoundException("Заявка с id " + friendshipId + " не найдена");
                });

        // Проверяем, что подтверждает получатель
        if (!friendship.getAddresseeId().equals(userId)) {
            log.warn("Попытка подтвердить заявку не от получателя: userId={}", userId);
            throw new ConditionsNotMetException("Только получатель может подтвердить заявку");
        }

        if (friendship.getStatus() != Friendship.FriendshipStatus.PENDING) {
            log.warn("Заявка уже обработана: status={}", friendship.getStatus());
            throw new ConditionsNotMetException("Заявка уже обработана");
        }

        // Обновляем статус
        friendship.setStatus(Friendship.FriendshipStatus.CONFIRMED);
        Friendship confirmed = friendshipStorage.update(friendship);
        log.info("Заявка подтверждена: friendshipId={}", friendshipId);

        // ПОЛУЧАТЕЛЬ ДОБАВЛЯЕТ ОТПРАВИТЕЛЯ В СВОЙ СПИСОК ДРУЗЕЙ
        // (отправитель уже добавил получателя ранее при отправке заявки)
        addFriendToUser(userId, friendship.getRequesterId());

        return confirmed;
    }

    public void declineFriendship(Long friendshipId, Long userId) {
        log.debug("Отклонение заявки: friendshipId={}, userId={}", friendshipId, userId);

        Friendship friendship = friendshipStorage.findById(friendshipId)
                .orElseThrow(() -> new NotFoundException("Заявка не найдена"));

        if (!friendship.getAddresseeId().equals(userId)) {
            throw new ConditionsNotMetException("Только получатель может отклонить заявку");
        }

        if (friendship.getStatus() != Friendship.FriendshipStatus.PENDING) {
            throw new ConditionsNotMetException("Заявка уже обработана");
        }

        // Удаляем заявку
        friendshipStorage.delete(friendshipId);

        // УДАЛЯЕМ АДРЕСАТА ИЗ СПИСКА ДРУЗЕЙ ОТПРАВИТЕЛЯ
        removeFriendFromUser(friendship.getRequesterId(), friendship.getAddresseeId());

        log.info("Заявка отклонена: friendshipId={}", friendshipId);
    }

    public void cancelFriendRequest(Long friendshipId, Long userId) {
        log.debug("Отзыв заявки: friendshipId={}, userId={}", friendshipId, userId);

        Friendship friendship = friendshipStorage.findById(friendshipId)
                .orElseThrow(() -> new NotFoundException("Заявка не найдена"));

        if (!friendship.getRequesterId().equals(userId)) {
            throw new ConditionsNotMetException("Только отправитель может отозвать заявку");
        }

        if (friendship.getStatus() != Friendship.FriendshipStatus.PENDING) {
            throw new ConditionsNotMetException("Заявка уже обработана");
        }

        // Удаляем заявку
        friendshipStorage.delete(friendshipId);

        // УДАЛЯЕМ АДРЕСАТА ИЗ СПИСКА ДРУЗЕЙ ОТПРАВИТЕЛЯ
        removeFriendFromUser(friendship.getRequesterId(), friendship.getAddresseeId());

        log.info("Заявка отозвана: friendshipId={}", friendshipId);
    }

    public void removeFriendship(Long userId, Long friendId) {
        log.info("Удаление из друзей: {} и {}", userId, friendId);

        validateUserExist(userId, friendId);
        validateNotSameUser(userId, friendId);

        // Проверяем, что они друзья (есть подтвержденная заявка)
        Optional<Friendship> friendship = friendshipStorage.findByUsers(userId, friendId);

        if (friendship.isEmpty()) {
            log.info("Дружба не найдена");
            return;
        }

        if (!userStorage.findUserById(userId).getUserFriends().contains(friendId)) {
            log.info("Пользователи не являются друзьями");
            return;
        }

        // Удаляем запись о дружбе
        friendshipStorage.delete(friendship.get().getId());
        userStorage.deleteFriend(userId, friendId);

        // УДАЛЯЕМ ДРУГА ИЗ СПИСКА ТОЛЬКО У ИНИЦИАТОРА
        removeFriendFromUser(userId, friendId);

        // Проверяем, есть ли встречная заявка (от friendId к userId)
        // Если есть — она тоже удаляется, но НЕ ВЛИЯЕТ на списки друзей
        Optional<Friendship> reverse = friendshipStorage.findByUsers(friendId, userId);
        reverse.ifPresent(f -> {
            friendshipStorage.delete(f.getId());
            log.debug("Удалена встречная заявка от {} к {}", friendId, userId);
        });

        log.info("Пользователь {} удалил {} из друзей", userId, friendId);
    }

    public List<Friendship> getIncomingRequests(Long userId) {
        userStorage.findUserById(userId);
        return friendshipStorage.findPendingRequests(userId);
    }

    // Получить исходящие заявки (которые я отправил)
    public List<Friendship> getOutgoingRequests(Long userId) {
        userStorage.findUserById(userId);
        return friendshipStorage.findSentRequests(userId);
    }

    private void validateUserExist(Long userId1, Long userId2) {
        userStorage.findUserById(userId1);
        userStorage.findUserById(userId2);
    }

    private void validateNotSameUser(Long userId1, Long userId2) {
        if (userId1.equals(userId2)) {
            log.error("Попытка выполнить операцию с самим собой: userId={}", userId1);
            throw new ConditionsNotMetException("Нельзя выполнить операцию с самим собой");
        }
    }

    private void addFriendToUser(Long userId, Long friendId) {
        log.debug("Добавление друга {} пользователю {}", friendId, userId);

        User user = userStorage.findUserById(userId);
        user.getUserFriends().add(friendId);
        userStorage.saveFriends(user);

        log.debug("Пользователь {} добавил {} в друзья", userId, friendId);
    }

    private void removeFriendFromUser(Long userId, Long friendId) {
        log.debug("Удаление друга {} у пользователя {}", friendId, userId);

        User user = userStorage.findUserById(userId);
        user.getUserFriends().remove(friendId);
        userStorage.saveFriends(user);

        log.debug("Пользователь {} удалил {} из друзей", userId, friendId);
    }
}

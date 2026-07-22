package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendshipService {
    private final FriendshipStorage friendshipStorage;
    private final UserStorage userStorage;

    //Отправка заявки в друзья
    public Friendship sendFriendRequest(Long requesterId, Long addresseeId) {
        log.info("Отправка заявки в друзья: от пользователя {} пользователю {}", requesterId, addresseeId);

        validateUserExist(requesterId, addresseeId);
        validateNotSameUser(addresseeId, requesterId);

        Optional<Friendship> existing = friendshipStorage.findByUsers(requesterId, addresseeId);

        if (existing.isPresent()) {
            Friendship friendship = existing.get();

            if (friendship.getStatus() == Friendship.FriendshipStatus.CONFIRMED) {
                log.error("Попытка отправить заявку, но пользователи уже друзья: {} и {}", requesterId, addresseeId);
                throw new ConditionsNotMetException("Пользователи уже являются друзьями");
            }

            if (friendship.getStatus() == Friendship.FriendshipStatus.PENDING) {
                //если заявка от текущего пользователя
                if (friendship.getRequesterId().equals(requesterId)) {
                    log.error("Попытка отправить повторную заявку от пользователя {} пользователю {}", requesterId, addresseeId);
                    throw new ConditionsNotMetException("Заявка уже отправлена");
                }

                //если втречная заявка подтверждаем
                log.info("Обнаружена встречная заявка между {} и {}, выполняем автоподтверждение", requesterId, addresseeId);
                return confirmFriendship(friendship.getId(), addresseeId);
            }
        }

        Friendship newFriendship = Friendship.builder()
                .requesterId(requesterId)
                .addresseeId(addresseeId)
                .status(Friendship.FriendshipStatus.PENDING)
                .build();

        Friendship created  = friendshipStorage.create(newFriendship);
        log.info("Создана новая заявка в друзья: id={}, от {} к {}", created.getId(), requesterId, addresseeId);

        return created;
    }

    //подтвердить дружбу
    public Friendship confirmFriendship(Long friendshipId, Long userId) {
        log.debug("Подтверждение заявки: friendshipId={}, userId={}", friendshipId, userId);

        //находим заявку
        Friendship friendship = friendshipStorage.findById(friendshipId)
                .orElseThrow(() -> {
                    log.error("Заявка с id {} не найдена", friendshipId);
                    return new NotFoundException("Заявка с id " + friendshipId + " не найдена");
                });

        // проверяем права (только получатель может подтвердить)
        if (!friendship.getAddresseeId().equals(userId)) {
            log.warn("Попытка подтвердить заявку не от получателя: friendshipId={}, userId={}", friendshipId, userId);
            throw new ConditionsNotMetException("Только получатель может подтвердить заявку");
        }

        // проверяем статус
        if (friendship.getStatus() != Friendship.FriendshipStatus.PENDING) {
            log.warn("Попытка подтвердить уже обработанную заявку: friendshipId={}, status={}", friendshipId, friendship.getStatus());
            throw new ConditionsNotMetException("Заявка уже обработана");
        }

        // обновляем статус
        friendship.setStatus(Friendship.FriendshipStatus.CONFIRMED);
        Friendship confirmed = friendshipStorage.update(friendship);
        log.info("Заявка подтверждена: friendshipId={}, пользователи {} и {} теперь друзья",
                friendshipId, friendship.getRequesterId(), friendship.getAddresseeId());

        // добавляем друг друга в списки друзей
        addFriendToBothUsers(friendship.getRequesterId(), friendship.getAddresseeId());

        return confirmed;
    }

    //отклонить заявку
    public void declineFriendship(Long friendshipId, Long userId) {
        Friendship friendship = friendshipStorage.findById(friendshipId)
                .orElseThrow(() -> new NotFoundException("Заявка не найдена"));

        if (!friendship.getAddresseeId().equals(userId)) {
            throw new ConditionsNotMetException("Только получатель может отклонить заявку");
        }

        if (friendship.getStatus() != Friendship.FriendshipStatus.PENDING) {
            throw new ConditionsNotMetException("Заявка уже обработана");
        }

        friendshipStorage.delete(friendshipId);
    }

    //отозвать заявку
    public void cancelFriendRequest(Long friendshipId, Long userId) {
        Friendship friendship = friendshipStorage.findById(friendshipId)
                .orElseThrow(() -> new NotFoundException("Заявка не найдена"));

        if (!friendship.getRequesterId().equals(userId)) {
            throw new ConditionsNotMetException("Только отправитель может отозвать заявку");
        }

        if (friendship.getStatus() != Friendship.FriendshipStatus.PENDING) {
            throw new ConditionsNotMetException("Заявка уже обработана");
        }

        friendshipStorage.delete(friendshipId);
    }

    //удалить из друзей
    public void removeFriendship(Long userId, Long friendId) {
        validateUserExist(userId, friendId);
        validateNotSameUser(userId, friendId);

        if (!areFriends(userId, friendId)) {
            throw new ConditionsNotMetException("Пользователи не являются друзьями");
        }

        // Удаляем запись о дружбе
        friendshipStorage.deleteByUsers(userId, friendId);
    }

    //впомогательный клас являются ли друзьями
    public boolean areFriends(Long userId1, Long userId2) {
        User user1 = userStorage.findUserById(userId1);
        User user2 = userStorage.findUserById(userId2);
        return (user1.getUserFriends() != null && user1.getUserFriends().contains(userId2) &&
                user2.getUserFriends() != null && user2.getUserFriends().contains(userId1));
    }

    //Получить входящие заявки
    public List<Friendship> getIncomingRequests(Long userId) {
        userStorage.findUserById(userId); // проверяем существование
        return friendshipStorage.findPendingRequests(userId);
    }

    //Получить исходящие заявки
    public List<Friendship> getOutgoingRequests(Long userId) {
        userStorage.findUserById(userId); // проверяем существование
        return friendshipStorage.findSentRequests(userId);
    }

    private void validateUserExist(Long requesterId, Long addresseeId) {
        userStorage.findUserById(requesterId);
        userStorage.findUserById(addresseeId);
    }

    private void validateNotSameUser(Long requesterId, Long addresseeId) {
        if (requesterId.equals(addresseeId)) {
            log.error("Попытка выполнить операцию с самим собой: userId={}", requesterId);
            throw new ConditionsNotMetException("Нельзя выполнить операцию с самим собой");
        }
    }

    private void addFriendToBothUsers(Long userId1, Long userId2) {
        log.debug("Добавление пользователей {} и {} в друзья друг другу", userId1, userId2);

        User user1 = userStorage.findUserById(userId1);
        User user2 = userStorage.findUserById(userId2);

        user1.getUserFriends().add(userId2);
        user2.getUserFriends().add(userId1);

        log.debug("Пользователи {} и {} добавлены в друзья друг другу", userId1, userId2);
    }
}

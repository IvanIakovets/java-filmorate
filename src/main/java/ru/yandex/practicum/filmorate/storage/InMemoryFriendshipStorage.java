package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Friendship;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class InMemoryFriendshipStorage implements FriendshipStorage {
    private final Map<Long, Friendship> friendships = new HashMap<>();

    @Override
    public Friendship create(Friendship friendship) {
        friendship.setId(getNextId());
        friendships.put(friendship.getId(), friendship);
        return friendship;
    }

    @Override
    public Optional<Friendship> findById(Long friendshipId) {
        return Optional.ofNullable(friendships.get(friendshipId));
    }

    @Override
    public Optional<Friendship> findByUsers(Long userId1, Long userId2) {
        return friendships.values().stream()
                .filter(friendship -> (
                        friendship.getRequesterId().equals(userId1) &&
                                friendship.getAddresseeId().equals(userId2)) ||
                        (friendship.getRequesterId().equals(userId2) &&
                                friendship.getAddresseeId().equals(userId1))
                        )
                .findFirst();
    }

    @Override
    public Friendship update(Friendship friendship) {
        if (!friendships.containsKey(friendship.getId())) {
            throw new NotFoundException("Заявка с id " + friendship.getId() + " не найдена");
        }

        // Обновляем запись
        friendships.put(friendship.getId(), friendship);
        return friendship;
    }

    @Override
    public void delete(Long friendshipId) {
        friendships.remove(friendshipId);
    }

    @Override
    public void deleteByUsers(Long userId1, Long userId2) {
        friendships.values().removeIf(friendship ->
                (friendship.getRequesterId().equals(userId1) && friendship.getAddresseeId().equals(userId2)) ||
                        (friendship.getRequesterId().equals(userId2) && friendship.getAddresseeId().equals(userId1))
        );
    }

    @Override
    public List<Friendship> findPendingRequests(Long addresseeId) {
        return friendships.values().stream()
                .filter(friendship ->
                        friendship.getAddresseeId().equals(addresseeId) &&
                                friendship.getStatus() == Friendship.FriendshipStatus.PENDING
                )
                .collect(Collectors.toList());
    }

    @Override
    public List<Friendship> findSentRequests(Long requesterId) {
        return friendships.values().stream()
                .filter(friendship ->
                        friendship.getRequesterId().equals(requesterId) &&
                                friendship.getStatus() == Friendship.FriendshipStatus.PENDING
                )
                .collect(Collectors.toList());
    }

    private long getNextId() {
        long currentMaxId = friendships.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}

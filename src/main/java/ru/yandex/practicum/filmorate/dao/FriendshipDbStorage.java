package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.FriendshipRowMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class FriendshipDbStorage implements FriendshipStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FriendshipRowMapper friendshipRowMapper;

    public FriendshipDbStorage(JdbcTemplate jdbcTemplate, FriendshipRowMapper friendshipRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.friendshipRowMapper = friendshipRowMapper;
    }

    @Override
    public Friendship create(Friendship friendship) {
        log.info("FriendshipDbStorage: создание заявки от {} к {}",
                friendship.getRequesterId(), friendship.getAddresseeId());

        String sql = "INSERT INTO friendships (requester_id, addressee_id, status) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, friendship.getRequesterId());
            ps.setLong(2, friendship.getAddresseeId());
            ps.setString(3, friendship.getStatus().name());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        friendship.setId(id);

        log.info("Заявка создана с id {}", id);
        return friendship;
    }

    @Override
    public Optional<Friendship> findById(Long id) {
        log.debug("FriendshipDbStorage: поиск заявки по id {}", id);

        String sql = "SELECT * FROM friendships WHERE id = ?";

        try {
            Friendship friendship = jdbcTemplate.queryForObject(sql, friendshipRowMapper, id);
            return Optional.of(friendship);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Friendship> findByUsers(Long userId1, Long userId2) {
        log.debug("FriendshipDbStorage: поиск заявки между {} и {}", userId1, userId2);

        String sql = "SELECT * FROM friendships " +
                "WHERE (requester_id = ? AND addressee_id = ?) " +
                "OR (requester_id = ? AND addressee_id = ?)";

        try {
            Friendship friendship = jdbcTemplate.queryForObject(sql, friendshipRowMapper,
                    userId1, userId2, userId2, userId1);
            return Optional.of(friendship);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Friendship update(Friendship friendship) {
        log.info("FriendshipDbStorage: обновление заявки {}", friendship.getId());

        if (friendship.getId() == null) {
            throw new NotFoundException("ID заявки не может быть null");
        }

        String sql = "UPDATE friendships SET status = ? WHERE id = ?";

        int rows = jdbcTemplate.update(sql,
                friendship.getStatus().name(),
                friendship.getId()
        );

        if (rows == 0) {
            throw new NotFoundException("Заявка с id " + friendship.getId() + " не найдена");
        }

        return friendship;
    }

    @Override
    public void delete(Long id) {
        log.info("FriendshipDbStorage: удаление заявки {}", id);

        String sql = "DELETE FROM friendships WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void deleteByUsers(Long userId1, Long userId2) {
        log.info("FriendshipDbStorage: удаление заявок между {} и {}", userId1, userId2);

        String sql = "DELETE FROM friendships " +
                "WHERE (requester_id = ? AND addressee_id = ?) " +
                "OR (requester_id = ? AND addressee_id = ?)";

        jdbcTemplate.update(sql, userId1, userId2, userId2, userId1);
    }

    @Override
    public List<Friendship> findPendingRequests(Long addresseeId) {
        log.debug("FriendshipDbStorage: поиск входящих заявок для пользователя {}", addresseeId);

        String sql = "SELECT * FROM friendships " +
                "WHERE addressee_id = ? AND status = 'PENDING' " +
                "ORDER BY id";

        return jdbcTemplate.query(sql, friendshipRowMapper, addresseeId);
    }

    @Override
    public List<Friendship> findSentRequests(Long requesterId) {
        log.debug("FriendshipDbStorage: поиск исходящих заявок от пользователя {}", requesterId);

        String sql = "SELECT * FROM friendships " +
                "WHERE requester_id = ? AND status = 'PENDING' " +
                "ORDER BY id";

        return jdbcTemplate.query(sql, friendshipRowMapper, requesterId);
    }
}
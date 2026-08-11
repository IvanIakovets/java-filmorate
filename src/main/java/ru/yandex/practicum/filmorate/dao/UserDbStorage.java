package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exceptions.DuplicateDataException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

@Slf4j
@Repository
@Primary
@Qualifier("db")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    public UserDbStorage(JdbcTemplate jdbcTemplate, UserRowMapper userRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRowMapper = userRowMapper;
    }

    @Transactional
    @Override
    public User createUser(User user) {
        log.info("UserDbStorage: создание пользователя {}", user.getEmail());

        checkDuplicateUser(user);

        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, user.getEmail());
                ps.setString(2, user.getLogin());
                ps.setString(3, user.getName());
                ps.setDate(4, Date.valueOf(user.getBirthday()));
                return ps;
            }, keyHolder);
        } catch (DuplicateKeyException e) {
            log.error("Ошибка дубликата при создании пользователя: {}", e.getMessage());
            throw new DuplicateDataException("Пользователь с таким email или логином уже существует");
        }

        Long userId = keyHolder.getKey().longValue();
        user.setId(userId);

        log.info("Пользователь создан с id {}", userId);
        return findUserById(userId);
    }

    @Override
    public boolean deleteUser(Long userId) {
        log.info("UserDbStorage: удаление пользователя {}", userId);

        findUserById(userId);

        String sql = "DELETE FROM users WHERE id = ?";
        int rows = jdbcTemplate.update(sql, userId);

        if (rows == 0) {
            log.error("Пользователь с id {} не найден для удаления", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        log.info("Пользователь {} успешно удален", userId);
        return true;
    }

    @Transactional
    @Override
    public User updateUser(User user) {
        log.info("UserDbStorage: обновление пользователя {}", user.getId());

        findUserById(user.getId());
        checkDuplicateUserForUpdate(user);

        List<String> updateFields = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (user.getEmail() != null) {
            updateFields.add("email = ?");
            params.add(user.getEmail());
        }
        if (user.getLogin() != null) {
            updateFields.add("login = ?");
            params.add(user.getLogin());
        }
        if (user.getName() != null) {
            updateFields.add("name = ?");
            params.add(user.getName());
        }
        if (user.getBirthday() != null) {
            updateFields.add("birthday = ?");
            params.add(Date.valueOf(user.getBirthday()));
        }

        if (updateFields.isEmpty()) {
            log.info("Нет полей для обновления пользователя {}", user.getId());
            return findUserById(user.getId());
        }

        params.add(user.getId());
        String sql = "UPDATE users SET " + String.join(", ", updateFields) + " WHERE id = ?";

        try {
            jdbcTemplate.update(sql, params.toArray());
        } catch (DuplicateKeyException e) {
            log.error("Ошибка дубликата при обновлении пользователя: {}", e.getMessage());
            throw new DuplicateDataException("Пользователь с таким email или логином уже существует");
        }

        log.info("Пользователь {} успешно обновлен", user.getId());
        return findUserById(user.getId());
    }

    @Override
    public Collection<User> findAllUsers() {
        log.info("UserDbStorage: получение всех пользователей");

        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, userRowMapper);

        users.forEach(this::loadFriends);

        log.info("Найдено {} пользователей", users.size());
        return users;
    }

    @Override
    public User findUserById(Long userId) {
        log.info("UserDbStorage: получение пользователя по id {}", userId);

        String sql = "SELECT * FROM users WHERE id = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, userId);
            loadFriends(user);
            return user;
        } catch (EmptyResultDataAccessException e) {
            log.error("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }

    // ==================== МЕТОДЫ ДЛЯ РАБОТЫ С ДРУЗЬЯМИ ====================

    @Override
    public boolean addFriend(Long userId, Long friendId) {
        log.info("UserDbStorage: добавление друга {} пользователю {}", friendId, userId);

        // Проверяем, что пользователи существуют
        findUserById(userId);
        findUserById(friendId);

        // Проверяем, не пытаемся ли добавить себя
        if (userId.equals(friendId)) {
            log.warn("Попытка добавить себя в друзья");
            return false;
        }

        String sql = "INSERT INTO user_friends (user_id, friend_id) VALUES (?, ?)";

        try {
            jdbcTemplate.update(sql, userId, friendId);
            log.info("Друг {} добавлен пользователю {}", friendId, userId);
            return true;
        } catch (DuplicateKeyException e) {
            log.warn("Связь уже существует: user={}, friend={}", userId, friendId);
            return false;
        }
    }

    @Override
    public boolean deleteFriend(Long userId, Long friendId) {
        log.info("UserDbStorage: удаление друга {} у пользователя {}", friendId, userId);

        String sql = "DELETE FROM user_friends WHERE user_id = ? AND friend_id = ?";
        int rows = jdbcTemplate.update(sql, userId, friendId);

        if (rows > 0) {
            log.info("Друг {} удален у пользователя {}", friendId, userId);
            return true;
        } else {
            log.warn("Связь не найдена: user={}, friend={}", userId, friendId);
            return false;
        }
    }

    private void loadFriends(User user) {
        String sql = "SELECT friend_id FROM user_friends WHERE user_id = ?";

        List<Long> friendIds = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong("friend_id"),
                user.getId()
        );

        user.setUserFriends(new HashSet<>(friendIds));
        log.debug("Загружено {} друзей для пользователя {}", friendIds.size(), user.getId());
    }

    @Transactional
    @Override
    public void saveFriends(User user) {
        if (user.getId() == null) {
            log.warn("Нельзя сохранить друзей для пользователя без ID");
            return;
        }

        log.debug("Сохранение списка друзей для пользователя {}", user.getId());

        // Удаляем все старые связи
        String deleteSql = "DELETE FROM user_friends WHERE user_id = ?";
        jdbcTemplate.update(deleteSql, user.getId());

        // Вставляем новые связи
        if (user.getUserFriends() != null && !user.getUserFriends().isEmpty()) {
            String insertSql = "INSERT INTO user_friends (user_id, friend_id) VALUES (?, ?)";
            for (Long friendId : user.getUserFriends()) {
                jdbcTemplate.update(insertSql, user.getId(), friendId);
            }
            log.debug("Сохранено {} друзей для пользователя {}", user.getUserFriends().size(), user.getId());
        }
    }

    private void checkDuplicateUser(User user) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? OR login = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
                user.getEmail(), user.getLogin());

        if (count != null && count > 0) {
            log.error("Пользователь с email {} или логином {} уже существует",
                    user.getEmail(), user.getLogin());
            throw new DuplicateDataException("Пользователь с таким email или логином уже существует");
        }
    }

    private void checkDuplicateUserForUpdate(User user) {
        String sql = "SELECT COUNT(*) FROM users WHERE (email = ? OR login = ?) AND id != ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
                user.getEmail(), user.getLogin(), user.getId());

        if (count != null && count > 0) {
            log.error("Пользователь с email {} или логином {} уже существует",
                    user.getEmail(), user.getLogin());
            throw new DuplicateDataException("Пользователь с таким email или логином уже существует");
        }
    }
}

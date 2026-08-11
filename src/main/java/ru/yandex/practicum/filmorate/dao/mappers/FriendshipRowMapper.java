package ru.yandex.practicum.filmorate.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FriendshipRowMapper implements RowMapper<Friendship> {

    @Override
    public Friendship mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Friendship.builder()
                .id(rs.getLong("id"))
                .requesterId(rs.getLong("requester_id"))
                .addresseeId(rs.getLong("addressee_id"))
                .status(Friendship.FriendshipStatus.valueOf(rs.getString("status")))
                .build();
    }
}

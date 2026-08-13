package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Friendship {
    private Long id;
    private Long requesterId;
    private Long addresseeId;
    private FriendshipStatus status;

    public enum FriendshipStatus {
        PENDING,   // неподтвержденная - заявка
        CONFIRMED  // подтвержденная
    }
}

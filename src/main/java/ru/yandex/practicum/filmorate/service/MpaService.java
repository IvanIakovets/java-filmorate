package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.MpaResponse;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class MpaService {
    private final MpaStorage mpaStorage;

    public List<MpaResponse> getAllMpa() {
        log.info("MpaService: получение всех рейтингов");
        return mpaStorage.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public MpaResponse getMpaById(Integer id) {
        log.info("MpaService: получение рейтинга по id {}", id);
        MpaRating mpaRating = mpaStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id " + id + " не найден"));

        return convertToResponse(mpaRating);
    }

    private MpaResponse convertToResponse(MpaRating mpa) {
        if (mpa == null) {
            return null;
        }
        return MpaResponse.builder()
                .id(mpa.getId())
                .name(mpa.getName())
                .build();
    }
}

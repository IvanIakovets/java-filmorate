package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class MpaService {
    private final MpaStorage mpaStorage;

    public List<MpaRating> getAllMpa() {
        log.info("MpaService: получение всех рейтингов");
        return mpaStorage.findAll();
    }

    public MpaRating getMpaById(Integer id) {
        log.info("MpaService: получение рейтинга по id {}", id);
        return mpaStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id " + id + " не найден"));
    }
}

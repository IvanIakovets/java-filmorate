package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.MpaResponse;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mpa")
@Slf4j
public class MpaController {

    private final MpaService mpaService;

    public MpaController(MpaService mpaService) {
        this.mpaService = mpaService;
    }

    @GetMapping
    public Collection<MpaResponse> getAllMpa() {
        log.info("Запрос на получение всех рейтингов MPA");
        return mpaService.getAllMpa().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public MpaResponse getMpaById(@PathVariable Integer id) {
        log.info("Запрос на получение рейтинга MPA по id {}", id);
        MpaRating mpa = mpaService.getMpaById(id);
        return convertToResponse(mpa);
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
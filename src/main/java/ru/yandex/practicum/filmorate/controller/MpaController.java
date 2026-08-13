package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.MpaResponse;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@Slf4j
public class MpaController {

    private final MpaService mpaService;

    public MpaController(MpaService mpaService) {
        this.mpaService = mpaService;
    }

    // получить все рейтинги МПА
    @GetMapping
    public Collection<MpaResponse> getAllMpa() {
        log.info("Запрос на получение всех рейтингов MPA");
        return mpaService.getAllMpa();
    }

    // получить МПА рейтинг по Id
    @GetMapping("/{id}")
    public MpaResponse getMpaById(@PathVariable Integer id) {
        log.info("Запрос на получение рейтинга MPA по id {}", id);
        return mpaService.getMpaById(id);
    }
}
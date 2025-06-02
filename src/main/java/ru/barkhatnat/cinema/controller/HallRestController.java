package ru.barkhatnat.cinema.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.barkhatnat.cinema.dto.regular.HallDto;
import ru.barkhatnat.cinema.dto.setup.HallSetupDto;
import ru.barkhatnat.cinema.dto.update.HallUpdateDto;
import ru.barkhatnat.cinema.service.HallService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rest")
@RequiredArgsConstructor
@Tag(name = "Hall Management", description = "API для управления залами кинотеатра")
public class HallRestController {

    private final HallService hallService;

    @DeleteMapping("/admin/halls")
    @Operation(
            summary = "Удалить зал по идентификатору",
            description = "Удаляет зал кинотеатра по уникальному идентификатору.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Зал успешно удален"),
            })
    public ResponseEntity<Void> deleteById(@RequestBody @Parameter(description = "UUID идентификатор зала") HallId id) {
        hallService.deleteById(id.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/halls")
    @Operation(
            summary = "Получить список всех залов",
            description = "Возвращает список всех залов в кинотеатре.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список залов получен")
            })
    public ResponseEntity<List<HallDto>> findAll() {
        List<HallDto> halls = hallService.findAll();
        if (halls.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(halls);
    }

    @GetMapping("/halls/{id}")
    @Operation(
            summary = "Получить зал по идентификатору",
            description = "Возвращает зал по его уникальному идентификатору.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Зал найден"),
                    @ApiResponse(responseCode = "404", description = "Зал не найден")
            })
    public ResponseEntity<HallDto> findById(@PathVariable UUID id) {
        HallDto hallDto = hallService.findById(id);
        return ResponseEntity.ok(hallDto);
    }

    @PutMapping("/admin/halls/{id}")
    @Operation(
            summary = "Обновить данные зала",
            description = "Обновляет информацию о зале по уникальному идентификатору.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Зал успешно обновлен"),
                    @ApiResponse(responseCode = "404", description = "Зал не найден"),
            })
    public ResponseEntity<HallDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody @Parameter(description = "Обновленные данные зала") HallSetupDto hallSetupDto) {
        HallDto hallDto = hallService.update(id, hallSetupDto);
        return ResponseEntity.ok(hallDto);
    }

    @PostMapping("/admin/halls")
    @Operation(
            summary = "Создать новый зал",
            description = "Создает новый зал в кинотеатре.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Зал успешно создан"),
            })
    public ResponseEntity<HallDto> save(
            @RequestBody @Valid @Parameter(description = "Данные для создания нового зала") HallSetupDto hallCreateDto) {
        HallDto createdHall = hallService.createHall(hallCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdHall);
    }

    public static class HallId {
        private UUID id;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }
    }
}

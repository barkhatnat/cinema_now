package ru.barkhatnat.cinema.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import ru.barkhatnat.cinema.domain.Hall;
import ru.barkhatnat.cinema.domain.Row;
import ru.barkhatnat.cinema.domain.Seat;
import ru.barkhatnat.cinema.dto.create.HallCreateDto;
import ru.barkhatnat.cinema.dto.regular.HallDto;
import ru.barkhatnat.cinema.dto.setup.HallSetupDto;
import ru.barkhatnat.cinema.dto.setup.RowSetupDto;
import ru.barkhatnat.cinema.dto.setup.SeatSetupDto;
import ru.barkhatnat.cinema.dto.update.HallUpdateDto;
import ru.barkhatnat.cinema.mapper.HallMapper;
import ru.barkhatnat.cinema.repository.HallRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HallService {

    private final HallRepository hallRepository;
    private final HallMapper hallMapper;

    public void deleteById(UUID id) {
        hallRepository.deleteById(id);
    }

    public List<HallDto> findAll() {
        List<Hall> halls = hallRepository.findAll();
        return halls.stream()
                .map(hallMapper::toHallDto)
                .toList();
    }

    public HallDto findById(UUID id) {
        Hall hall = hallRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
        return hallMapper.toHallDto(hall);
    }


    public HallDto update(UUID id, @Valid HallSetupDto hallSetupDto) {
        // Получаем зал по ID
        Hall hall = hallRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));

        // Обновляем название зала
        hall.setName(hallSetupDto.name());

        // Сохраняем новый список строк
        int totalSeats = 0;

        // Существующие строки зала
        List<Row> existingRows = hall.getRows();

        // Собираем новые строки и сиденья
        for (RowSetupDto rowSetup : hallSetupDto.rows()) {
            // Найти строку по номеру, если существует, иначе создать новую
            Row row = existingRows.stream()
                    .filter(existingRow -> existingRow.getNumber().equals(rowSetup.number()))
                    .findFirst()
                    .orElseGet(() -> {
                        Row newRow = new Row();
                        newRow.setHall(hall);
                        existingRows.add(newRow);
                        return newRow;
                    });

            // Обновляем строку
            row.setNumber(rowSetup.number());

            // Обновляем сиденья строки
            List<Seat> existingSeats = row.getSeats();
            for (SeatSetupDto seatSetup : rowSetup.seats()) {
                Seat seat = existingSeats.stream()
                        .filter(existingSeat -> existingSeat.getNumber().equals(seatSetup.number()))
                        .findFirst()
                        .orElseGet(() -> {
                            Seat newSeat = new Seat();
                            newSeat.setRow(row);
                            existingSeats.add(newSeat);
                            return newSeat;
                        });

                // Обновляем данные сиденья
                seat.setNumber(seatSetup.number());
                seat.setType(seatSetup.type());
            }

            // Удаляем сиденья, которых нет в обновленных данных
            existingSeats.removeIf(existingSeat ->
                    rowSetup.seats().stream()
                            .noneMatch(seatSetup -> seatSetup.number().equals(existingSeat.getNumber())));

            totalSeats += existingSeats.size();
        }

        // Удаляем строки, которых нет в обновленных данных
        existingRows.removeIf(existingRow ->
                hallSetupDto.rows().stream()
                        .noneMatch(rowSetup -> rowSetup.number().equals(existingRow.getNumber())));

        // Обновляем вместимость зала
        hall.setCapacity(totalSeats);

        // Сохраняем обновленный зал
        Hall resultHall = hallRepository.save(hall);

        // Возвращаем DTO обновленного зала
        return hallMapper.toHallDto(resultHall);
    }





    public HallDto save(@Valid HallCreateDto hallCreateDto) {
        Hall hall = hallMapper.toEntity(hallCreateDto);
        Hall resultHall = hallRepository.save(hall);
        return hallMapper.toHallDto(resultHall);
    }

    @Transactional
    public HallDto createHall(@Valid HallSetupDto hallSetupDto) {
        Hall hall = Hall.builder()
                .name(hallSetupDto.name())
                .capacity(0)
                .build();

        List<Row> rows = new ArrayList<>();
        int totalSeats = 0;

        for (RowSetupDto rowSetup : hallSetupDto.rows()) {
            Row row = Row.builder()
                    .number(rowSetup.number())
                    .hall(hall)
                    .build();
            List<Seat> seats = new ArrayList<>();
            for (SeatSetupDto seatSetup : rowSetup.seats()) {
                Seat seat = Seat.builder()
                        .number(seatSetup.number())
                        .type(seatSetup.type())
                        .row(row)
                        .build();
                seats.add(seat);
            }
            row.setSeats(seats);
            rows.add(row);

            totalSeats += seats.size();
        }
        hall.setRows(rows);
        hall.setCapacity(totalSeats);
        hallRepository.save(hall);
        return hallMapper.toHallDto(hall);
    }
}

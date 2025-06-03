package ru.barkhatnat.cinema.mapper;

import org.mapstruct.*;
import ru.barkhatnat.cinema.domain.Row;
import ru.barkhatnat.cinema.domain.Seat;
import ru.barkhatnat.cinema.dto.create.SeatCreateDto;
import ru.barkhatnat.cinema.dto.regular.SeatDto;
import ru.barkhatnat.cinema.dto.update.SeatUpdateDto;
import ru.barkhatnat.cinema.repository.RowRepository;

import java.util.UUID;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SeatMapper {
    @Mapping(target = "row", ignore = true)
    Seat toEntity(SeatDto seatDto);

    SeatDto toSeatDto(Seat seat);

    @Mapping(target = "row", source = "rowId", qualifiedByName = "mapRow")
    Seat toEntity(SeatCreateDto seatCreateDto, @Context RowRepository rowRepository);

    SeatUpdateDto toSeatUpdateDto(Seat seat);

    @Mapping(target = "row", source = "rowId", qualifiedByName = "mapRow")
    Seat updateWithNull(SeatUpdateDto seatUpdateDto, @MappingTarget Seat seat, @Context RowRepository rowRepository);

    @Named("mapRow")
    default Row mapRow(UUID rowId, @Context RowRepository rowRepository) {
        return rowRepository.findById(rowId)
                .orElseThrow(() -> new RuntimeException("Row not found for ID: " + rowId));
    }
}
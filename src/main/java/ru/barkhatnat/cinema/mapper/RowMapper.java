package ru.barkhatnat.cinema.mapper;

import org.mapstruct.*;
import ru.barkhatnat.cinema.domain.Hall;
import ru.barkhatnat.cinema.domain.Row;
import ru.barkhatnat.cinema.dto.create.RowCreateDto;
import ru.barkhatnat.cinema.dto.regular.RowDto;
import ru.barkhatnat.cinema.dto.update.RowUpdateDto;
import ru.barkhatnat.cinema.repository.HallRepository;

import java.util.UUID;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {SeatMapper.class} // Use SeatMapper for nested mapping
)
public interface RowMapper {

    @Mapping(target = "hall", ignore = true)
    Row toEntity(RowDto rowDto);

    @Mapping(target = "seats", source = "seats")
    RowDto toRowDto(Row row);

    @Mapping(target = "hall", source = "hallId", qualifiedByName = "mapHall")
    Row toEntity(RowCreateDto rowCreateDto, @Context HallRepository hallRepository);

    RowUpdateDto toRowUpdateDto(Row row);

    @Mapping(target = "hall", source = "hallId", qualifiedByName = "mapHall")
    Row updateWithNull(RowUpdateDto rowUpdateDto, @MappingTarget Row row, @Context HallRepository hallRepository);

    @Named("mapHall")
    default Hall mapHall(UUID hallId, @Context HallRepository hallRepository) {
        return hallRepository.findById(hallId)
                .orElseThrow(() -> new RuntimeException("Hall not found for ID: " + hallId));
    }
}
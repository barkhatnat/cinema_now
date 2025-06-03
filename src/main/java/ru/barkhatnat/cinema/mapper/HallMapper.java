package ru.barkhatnat.cinema.mapper;

import org.mapstruct.*;
import ru.barkhatnat.cinema.domain.Hall;
import ru.barkhatnat.cinema.dto.create.HallCreateDto;
import ru.barkhatnat.cinema.dto.regular.HallDto;
import ru.barkhatnat.cinema.dto.update.HallUpdateDto;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {RowMapper.class}
)
public interface HallMapper {

    @Mapping(target = "capacity", ignore = true)
    @Mapping(target = "rows", ignore = true)
    Hall toEntity(HallDto hallDto);

    @Mapping(target = "rows", source = "rows")
    HallDto toHallDto(Hall hall);

    @Mapping(target = "capacity", ignore = true)
    Hall toEntity(HallCreateDto hallCreateDto);

    HallUpdateDto toHallUpdateDto(Hall hall);

    Hall updateWithNull(HallUpdateDto hallUpdateDto, @MappingTarget Hall hall);

    Hall updateWithNull(HallDto hallDto, @MappingTarget Hall hall);

    default int calculateCapacity(Hall hall) {
        return hall.getRows().stream()
                .mapToInt(row -> row.getSeats().size())
                .sum();
    }

    @AfterMapping
    default void setCapacity(Hall hall) {
        hall.setCapacity(calculateCapacity(hall));
    }
}


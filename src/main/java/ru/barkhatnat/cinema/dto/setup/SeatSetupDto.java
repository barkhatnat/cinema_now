package ru.barkhatnat.cinema.dto.setup;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import ru.barkhatnat.cinema.domain.enums.SeatType;

public record SeatSetupDto(
        @Min(message = "Seat number must be greater than or equal to 1", value = 1)
        Integer number,

        @NotNull(message = "Seat type cannot be null")
        SeatType type
) {
}
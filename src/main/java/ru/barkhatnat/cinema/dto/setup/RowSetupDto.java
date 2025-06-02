package ru.barkhatnat.cinema.dto.setup;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RowSetupDto(
        @Min(message = "Row number must be greater than or equal to 1", value = 1)
        Integer number,

        @NotEmpty(message = "Seats cannot be empty")
        @Valid
        List<SeatSetupDto> seats
) {
}
package ru.barkhatnat.cinema.dto.setup;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record HallSetupDto(
        @Size(message = "Hall name should not exceed 32 characters", max = 32)
        @NotBlank(message = "Hall name cannot be blank")
        String name,

        @NotEmpty(message = "Rows cannot be empty")
        @Valid
        List<RowSetupDto> rows
) {
}

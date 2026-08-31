package com.ires.project.dto;

import com.ires.project.entity.ProjectStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProjectUpdateRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 5000) String description,
        ProjectStatus status,
        LocalDate startDate,
        LocalDate endDate
) {

    @AssertTrue(message = "endDate cannot be before startDate")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }
}

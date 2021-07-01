package com.example.countrymanager.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@SuperBuilder
public class CountryGetDto {

    @NonNull
    private String id;

    @NonNull
    private List<ProviderDto> providers;

    @NonNull
    private String name;

    @NonNull
    private String abbreviation;

    @NonNull
    private LocalDateTime createdAt;

    @NonNull
    private LocalDateTime updatedAt;

}

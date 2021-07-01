package com.example.countrymanager.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
public class CountriesResponseDto {
    List<CountryGetDto> countries;

    Integer currentPage;

    Long totalCountries;

    Integer totalPages;

}

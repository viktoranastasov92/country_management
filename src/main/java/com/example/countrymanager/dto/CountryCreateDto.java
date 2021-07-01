package com.example.countrymanager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class CountryCreateDto {

    @JsonProperty("id_provider")
    private int idProvider;

    @Min(1)
    @JsonProperty("id_country_at_provider")
    private int idCountryAtProvider;

    @NonNull
    private String name;

    @NonNull
    @Size(min = 3, max = 3)
    private String abbreviation;

}

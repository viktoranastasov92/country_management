package com.example.countrymanager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@SuperBuilder
public class CountryUpdateDto {

    @JsonProperty("id_provider")
    private Integer idProvider;

    @Min(1)
    @JsonProperty("id_country_at_provider")
    private Integer idCountryAtProvider;

    private String name;

    @Size(min = 3, max = 3)
    private String abbreviation;

}

package com.example.countrymanager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProviderDto {

    private int idProvider;

    @JsonProperty("id_country_in_provider")
    private int idCountryInProvider;

}

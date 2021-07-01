package com.example.countrymanager.service;

import com.example.countrymanager.dto.CountryCreateDto;
import com.example.countrymanager.dto.CountryUpdateDto;
import com.example.countrymanager.model.Country;

public interface CountryValidatorService {

    void validateCreateDto(CountryCreateDto countryCreateDto);

    Country validateUpdateParameters(String id, CountryUpdateDto countryUpdateDto);

    Country validateUpdateParameters(int idProvider, int idCountryAtProvider, CountryUpdateDto countryUpdateDto);

    Country validateDeleteParameters(String id);

    Country validateDeleteParameters(int idProvider, int idCountryAtProvider);

}

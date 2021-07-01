package com.example.countrymanager.service;

import com.example.countrymanager.dto.CountryUpdateDto;
import com.example.countrymanager.model.Country;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CountryService {

    /**
     * @return list of countries with a pageable information - total countries for that search criteria, current page and total pages
     */
    Page<Country> getCountriesResponse(List<String> ids, String name, String abbreviation, Integer idProvider, Integer idCountryAtProvider, int page, int size);

    Country createCountry(Country country);

    Country updateCountry(Country existingCountry, CountryUpdateDto countryUpdateDto);

    void deleteCountry(Country existingCountry);

}

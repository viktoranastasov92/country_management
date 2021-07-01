package com.example.countrymanager.controller;

import com.example.countrymanager.dto.CountriesResponseDto;
import com.example.countrymanager.dto.CountryCreateDto;
import com.example.countrymanager.dto.CountryGetDto;
import com.example.countrymanager.dto.CountryUpdateDto;
import com.example.countrymanager.model.Country;
import com.example.countrymanager.service.CountryService;
import com.example.countrymanager.service.CountryValidatorService;
import com.example.countrymanager.util.EntityConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/countries")
@RequiredArgsConstructor
public class CountryController {

    private static final String PROVIDER_DATA_URI = "/provider_data";

    @Autowired
    private final CountryService countryService;

    @Autowired
    private final EntityConverter entityConverter;

    @Autowired
    private final CountryValidatorService countryValidatorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public CountryGetDto createCountry(@Valid @RequestBody CountryCreateDto countryCreateDto) throws IllegalArgumentException {
        try {
            countryValidatorService.validateCreateDto(countryCreateDto);
            Country country = entityConverter.convertCreateDtoToEntity(countryCreateDto);
            Country createdCountry = countryService.createCountry(country);
            return entityConverter.convertEntityToGetDto(createdCountry);
        } catch(IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public CountryGetDto updateCountryById(@RequestParam(value = "id") String id,
            @Valid @RequestBody CountryUpdateDto countryUpdateDto) {
        try {
            Country existingCountry = countryValidatorService.validateUpdateParameters(id, countryUpdateDto);
            Country updatedCountry = countryService.updateCountry(existingCountry, countryUpdateDto);
            return entityConverter.convertEntityToGetDto(updatedCountry);
        } catch(IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PutMapping(PROVIDER_DATA_URI)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public CountryGetDto updateCountryByProviderData(@RequestParam(value = "id_provider") int idProvider,
                                                     @RequestParam(value = "id_country_at_provider") int idCountryAtProvider,
                                                     @Valid @RequestBody CountryUpdateDto countryUpdateDto) {
        try {
            Country existingCountry = countryValidatorService.validateUpdateParameters(idProvider, idCountryAtProvider, countryUpdateDto);
            Country updatedCountry = countryService.updateCountry(existingCountry, countryUpdateDto);
            return entityConverter.convertEntityToGetDto(updatedCountry);
        } catch(IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void deleteCountryById(@RequestParam(value = "id") String id) {
        Country existingCountry = countryValidatorService.validateDeleteParameters(id);
        countryService.deleteCountry(existingCountry);
    }

    @DeleteMapping(PROVIDER_DATA_URI)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void deleteCountryByProviderData(@RequestParam(value = "id_provider") int idProvider,
                                            @RequestParam(value = "id_country_at_provider") int idCountryAtProvider) {
        Country existingCountry = countryValidatorService.validateDeleteParameters(idProvider, idCountryAtProvider);
        countryService.deleteCountry(existingCountry);
    }

    @GetMapping
    @ResponseBody
    public CountriesResponseDto getCountries(@RequestParam(required = false) List<String> ids,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String abbreviation,
            @RequestParam(required = false, value = "id_provider") Integer idProvider,
            @RequestParam(required = false, value = "id_country_at_provider") Integer idCountryAtProvider,
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer page,
            @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(100) Integer size) {
        Page<Country> pagedResponseCountries = countryService.getCountriesResponse(ids, name, abbreviation,
                idProvider, idCountryAtProvider, page, size);
        CountriesResponseDto countriesResponseDto = new CountriesResponseDto();
        countriesResponseDto.setCountries(pagedResponseCountries.getContent().stream().map(entityConverter::convertEntityToGetDto).collect(Collectors.toList()));
        countriesResponseDto.setCurrentPage(pagedResponseCountries.getNumber());
        countriesResponseDto.setTotalCountries(pagedResponseCountries.getTotalElements());
        countriesResponseDto.setTotalPages(pagedResponseCountries.getTotalPages());
        return countriesResponseDto;
    }

}

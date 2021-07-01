package com.example.countrymanager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.countrymanager.dto.*;
import com.example.countrymanager.model.Country;
import com.example.countrymanager.service.CountryService;
import com.example.countrymanager.service.CountryValidatorService;
import com.example.countrymanager.util.EntityConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class CountryControllerTest {

    @Mock
    private CountryService countryService;

    @Mock
    private EntityConverter entityConverter;

    @Mock
    private CountryValidatorService countryValidatorService;

    @InjectMocks
    private CountryController countryController;

    @Test
    void testCreateCountry() {
        CountryCreateDto countryCreateDto = new CountryCreateDto();
        Country country = new Country();
        when(entityConverter.convertCreateDtoToEntity(countryCreateDto)).thenReturn(country);
        when(countryService.createCountry(any())).then(returnsFirstArg());

        countryController.createCountry(countryCreateDto);

        verify(countryValidatorService, times(1)).validateCreateDto(countryCreateDto);
        verify(countryService, times(1)).createCountry(country);
        verify(entityConverter, times(1)).convertEntityToGetDto(country);
    }

    @Test
    void testUpdateCountryById() {
        String id = "test_id";
        CountryUpdateDto countryUpdateDto = new CountryUpdateDto();
        Country country = new Country();
        when(countryValidatorService.validateUpdateParameters(id, countryUpdateDto)).thenReturn(country);
        when(countryService.updateCountry(any(), any())).then(returnsFirstArg());

        countryController.updateCountryById(id, countryUpdateDto);

        verify(countryValidatorService, times(1)).validateUpdateParameters(id, countryUpdateDto);
        verify(countryService, times(1)).updateCountry(country, countryUpdateDto);
        verify(entityConverter, times(1)).convertEntityToGetDto(country);
    }

    @Test
    void testUpdateCountryByProviderData() {
        int idProvider = 1;
        int idCountryAtProvider = 10;
        CountryUpdateDto countryUpdateDto = new CountryUpdateDto();
        Country country = new Country();
        when(countryValidatorService.validateUpdateParameters(idProvider, idCountryAtProvider, countryUpdateDto)).thenReturn(country);
        when(countryService.updateCountry(any(), any())).then(returnsFirstArg());

        countryController.updateCountryByProviderData(idProvider, idCountryAtProvider, countryUpdateDto);

        verify(countryValidatorService, times(1)).validateUpdateParameters(idProvider, idCountryAtProvider, countryUpdateDto);
        verify(countryService, times(1)).updateCountry(country, countryUpdateDto);
        verify(entityConverter, times(1)).convertEntityToGetDto(country);
    }

    @Test
    void testDeleteCountryById() {
        String id = "test_id";
        Country country = new Country();
        when(countryValidatorService.validateDeleteParameters(id)).thenReturn(country);

        countryController.deleteCountryById(id);

        verify(countryValidatorService, times(1)).validateDeleteParameters(id);
        verify(countryService, times(1)).deleteCountry(country);
    }

    @Test
    void testDeleteCountryByProviderData() {
        int idProvider = 1;
        int idCountryAtProvider = 10;
        Country country = new Country();
        when(countryValidatorService.validateDeleteParameters(idProvider, idCountryAtProvider)).thenReturn(country);

        countryController.deleteCountryByProviderData(idProvider, idCountryAtProvider);

        verify(countryValidatorService, times(1)).validateDeleteParameters(idProvider, idCountryAtProvider);
        verify(countryService, times(1)).deleteCountry(country);
    }

    @Test
    void testGetCountries() {
        List<String> ids = Arrays.asList("test_id1", "test_id2");
        String name = "test_name";
        String abbreviation = "abb";
        Integer idProvider = 1;
        Integer idCountryAtProvider = 10;
        Integer page = 0;
        Integer size = 100;
        Page<Country> countryPageable = mock(Page.class);
        when(countryService.getCountriesResponse(ids, name, abbreviation, idProvider, idCountryAtProvider, page, size)).thenReturn(countryPageable);

        Country country = new Country();
        when(countryPageable.getContent()).thenReturn(Arrays.asList(country));
        when(countryPageable.getNumber()).thenReturn(0);
        when(countryPageable.getTotalElements()).thenReturn(1l);
        when(countryPageable.getTotalPages()).thenReturn(1);

        CountryGetDto countryGetDto = new CountryGetDto();
        CountriesResponseDto expectedCountriesResponseDto = CountriesResponseDto.builder()
                .countries(Arrays.asList(countryGetDto))
                .currentPage(0)
                .totalCountries(1l)
                .totalPages(1)
                .build();
        when(entityConverter.convertEntityToGetDto(country)).thenReturn(countryGetDto);

        CountriesResponseDto actualCountriesResponseDto = countryController.getCountries(ids, name, abbreviation, idProvider, idCountryAtProvider, page, size);

        verify(countryService, times(1)).getCountriesResponse(ids, name, abbreviation,
                idProvider, idCountryAtProvider, page, size);
        assertEquals(expectedCountriesResponseDto, actualCountriesResponseDto);
    }

}

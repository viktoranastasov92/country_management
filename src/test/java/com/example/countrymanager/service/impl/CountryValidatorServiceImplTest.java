package com.example.countrymanager.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.countrymanager.dto.CountryCreateDto;
import com.example.countrymanager.dto.CountryUpdateDto;
import com.example.countrymanager.model.Country;
import com.example.countrymanager.model.CountryProviderRelations;
import com.example.countrymanager.model.Provider;
import com.example.countrymanager.repository.CountryProviderRelationsRepository;
import com.example.countrymanager.repository.CountryRepository;
import com.example.countrymanager.repository.ProviderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountryValidatorServiceImplTest {

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private CountryProviderRelationsRepository countryProviderRelationsRepository;

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private CountryValidatorServiceImpl countryValidatorService;

    @Test
    void testInvalidProviderInCreateDto() {
        CountryCreateDto countryCreateDto = new CountryCreateDto();
        countryCreateDto.setIdProvider(333);
        when(providerRepository.findById(countryCreateDto.getIdProvider())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> countryValidatorService.validateCreateDto(countryCreateDto), "Expected IllegalArgumentException");
        assertEquals(String.format("Invalid provider id: %d", countryCreateDto.getIdProvider()), exception.getMessage());
    }

    @Test
    void testCountryPresentWithNameAndAbbreviationInCreateDto() {
        CountryCreateDto countryCreateDto = new CountryCreateDto();
        countryCreateDto.setIdProvider(400);
        countryCreateDto.setName("test_name");
        countryCreateDto.setAbbreviation("abbr");
        when(providerRepository.findById(countryCreateDto.getIdProvider())).thenReturn(Optional.of(new Provider()));
        when(countryRepository.findByNameAndAbbreviation(countryCreateDto.getName(), countryCreateDto.getAbbreviation()))
                .thenReturn(Optional.of(new Country()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> countryValidatorService.validateCreateDto(countryCreateDto), "Expected IllegalArgumentException");
        assertEquals(String.format("There is already a country with this name (%s) and this abbreviation (%s)",
                countryCreateDto.getName(), countryCreateDto.getAbbreviation()), exception.getMessage());
    }

    @Test
    void testCountryProviderRelationsInCreateDtoPresentForAnotherCountry() {
        CountryCreateDto countryCreateDto = new CountryCreateDto();
        countryCreateDto.setName("test_name");
        countryCreateDto.setAbbreviation("abbr");
        countryCreateDto.setIdProvider(555);
        countryCreateDto.setIdCountryAtProvider(1234);
        when(providerRepository.findById(countryCreateDto.getIdProvider())).thenReturn(Optional.of(new Provider()));
        when(countryRepository.findByNameAndAbbreviation(countryCreateDto.getName(), countryCreateDto.getAbbreviation())).thenReturn(Optional.empty());
        when(countryProviderRelationsRepository.findByIdProviderAndIdCountryAtProvider(
                countryCreateDto.getIdProvider(), countryCreateDto.getIdCountryAtProvider())).thenReturn(Optional.of(new CountryProviderRelations()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> countryValidatorService.validateCreateDto(countryCreateDto), "Expected IllegalArgumentException");
        assertTrue(exception.getMessage().equals(String.format(
                "This combination of provider id (%d) and country id in provider (%d) is already present for another country",
                countryCreateDto.getIdProvider(), countryCreateDto.getIdCountryAtProvider())));
    }

    @Test
    void testExceptionIfUpdateOfNonExistingCountry() {
        String idNotExistingCountry = "country_id";
        CountryUpdateDto countryUpdateDto = new CountryUpdateDto();
        when(countryRepository.findById(idNotExistingCountry)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> countryValidatorService.validateUpdateParameters(idNotExistingCountry, countryUpdateDto), "Expected ResourceNotFoundException");
        assertTrue(exception.getMessage().equals(String.format("Country with id: %s was not found", idNotExistingCountry)));
    }

    @Test
    void testInvalidProviderDataMissingIdCountryAtProviderWhenUpdate() {
        String idNotExistingCountry = "country_id";
        CountryUpdateDto countryUpdateDto = new CountryUpdateDto();
        countryUpdateDto.setIdProvider(25);
        countryUpdateDto.setIdCountryAtProvider(null);
        when(countryRepository.findById(idNotExistingCountry)).thenReturn(Optional.of(new Country()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> countryValidatorService.validateUpdateParameters(idNotExistingCountry, countryUpdateDto), "Expected IllegalArgumentException");
        assertEquals("Either send both provider id and country id in provider or do not send any of them", exception.getMessage());
    }

    @Test
    void testInvalidProviderDataMissingIdProviderWhenUpdate() {
        String idExistingCountry = "country_id";
        CountryUpdateDto countryUpdateDto = new CountryUpdateDto();
        countryUpdateDto.setIdProvider(null);
        countryUpdateDto.setIdCountryAtProvider(342);
        when(countryRepository.findById(idExistingCountry)).thenReturn(Optional.of(new Country()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> countryValidatorService.validateUpdateParameters(idExistingCountry, countryUpdateDto), "Expected IllegalArgumentException");
        assertEquals("Either send both provider id and country id in provider or do not send any of them", exception.getMessage());
    }

    @Test
    void testExceptionWhenProviderDataPresentForAnotherCountryWhenUpdate() {
        String idExistingCountry = "country_id";
        int existingIdProvider = 55;
        int existingIdCountryAtProvider = 342;
        CountryUpdateDto countryUpdateDto = new CountryUpdateDto();
        countryUpdateDto.setIdProvider(existingIdProvider);
        countryUpdateDto.setIdCountryAtProvider(existingIdCountryAtProvider);
        when(providerRepository.findById(existingIdProvider)).thenReturn(Optional.of(new Provider()));
        when(countryRepository.findById(idExistingCountry)).thenReturn(Optional.of(new Country()));
        when(countryProviderRelationsRepository.findByIdProviderAndIdCountryAtProvider(
                countryUpdateDto.getIdProvider(), countryUpdateDto.getIdCountryAtProvider())).thenReturn(Optional.of(new CountryProviderRelations()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> countryValidatorService.validateUpdateParameters(idExistingCountry, countryUpdateDto), "Expected IllegalArgumentException");
        assertEquals(String.format("This combination of provider id (%d) and country id in provider (%d) is already present for another country",
                countryUpdateDto.getIdProvider(), countryUpdateDto.getIdCountryAtProvider()),exception.getMessage());
    }

    @Test
    void testExceptionAnotherCountryWithSameNameAndAbbreviationExists() {
        String idExistingCountry = "country_id";
        CountryUpdateDto countryUpdateDto = new CountryUpdateDto();
        countryUpdateDto.setName("changed_name");
        countryUpdateDto.setAbbreviation("changed_abbreviation");
        Country existingCountry = Country.builder().id(idExistingCountry).name("name").abbreviation("abbreviation").build();
        when(countryRepository.findById(idExistingCountry)).thenReturn(Optional.of(existingCountry));
        when(countryRepository.findByNameAndAbbreviation(countryUpdateDto.getName(), countryUpdateDto.getAbbreviation())).thenReturn(Optional.of(new Country()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> countryValidatorService.validateUpdateParameters(idExistingCountry, countryUpdateDto), "Expected IllegalArgumentException");
        assertEquals(String.format("There is a different country with this name (%s) and this abbreviation (%s)",
                countryUpdateDto.getName(), countryUpdateDto.getAbbreviation()), exception.getMessage());
    }

}

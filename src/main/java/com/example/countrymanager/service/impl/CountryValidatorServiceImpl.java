package com.example.countrymanager.service.impl;

import com.example.countrymanager.dto.CountryCreateDto;
import com.example.countrymanager.dto.CountryUpdateDto;
import com.example.countrymanager.model.Country;
import com.example.countrymanager.model.CountryProviderRelations;
import com.example.countrymanager.model.Provider;
import com.example.countrymanager.repository.CountryProviderRelationsRepository;
import com.example.countrymanager.repository.CountryRepository;
import com.example.countrymanager.repository.ProviderRepository;
import com.example.countrymanager.service.CountryValidatorService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
@Service
public class CountryValidatorServiceImpl implements CountryValidatorService {

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private CountryProviderRelationsRepository countryProviderRelationsRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Override
    public void validateCreateDto(CountryCreateDto countryCreateDto) {
        validateProviderId(countryCreateDto.getIdProvider());

        Optional<Country> countryOptional = countryRepository.findByNameAndAbbreviation(countryCreateDto.getName(), countryCreateDto.getAbbreviation());
        if (countryOptional.isPresent()) {
            throw new IllegalArgumentException(String.format("There is already a country with this name (%s) and this abbreviation (%s)",
                    countryCreateDto.getName(), countryCreateDto.getAbbreviation()));
        }

        Optional<CountryProviderRelations> existingCountryProviderRelations =
                countryProviderRelationsRepository.findByIdProviderAndIdCountryAtProvider(
                        countryCreateDto.getIdProvider(), countryCreateDto.getIdCountryAtProvider());
        if (existingCountryProviderRelations.isPresent()) {
            throw new IllegalArgumentException(String.format("This combination of provider id (%d) and country id in provider (%d) is already present for another country",
                    countryCreateDto.getIdProvider(), countryCreateDto.getIdCountryAtProvider()));
        }
    }

    @Override
    public Country validateUpdateParameters(String id, CountryUpdateDto countryUpdateDto) {
        Country existingCountry = countryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                String.format("Country with id: %s was not found", id)));

        validateUpdateDto(countryUpdateDto, existingCountry);

        return existingCountry;
    }

    @Override
    public Country validateUpdateParameters(int idProvider, int idCountryAtProvider, CountryUpdateDto countryUpdateDto) {
        Optional<CountryProviderRelations> existingCountryProviderRelations =
                countryProviderRelationsRepository.findByIdProviderAndIdCountryAtProvider(idProvider, idCountryAtProvider);
        if (!existingCountryProviderRelations.isPresent()) {
            throw new ResourceNotFoundException(String.format("This combination of provider id (%d) and country id in provider (%d) is invalid",
                    idProvider, idCountryAtProvider));
        }
        Country existingCountry = countryRepository.findById(existingCountryProviderRelations.get().getCountryId()).get();
        validateUpdateDto(countryUpdateDto, existingCountry);
        return existingCountry;
    }

    @Override
    public Country validateDeleteParameters(String id) {
        return countryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                String.format("Country with id: %s was not found", id)));
    }

    @Override
    public Country validateDeleteParameters(int idProvider, int idCountryAtProvider) {
        Optional<CountryProviderRelations> existingCountryProviderRelations =
                countryProviderRelationsRepository.findByIdProviderAndIdCountryAtProvider(idProvider, idCountryAtProvider);
        if (!existingCountryProviderRelations.isPresent()) {
            throw new ResourceNotFoundException(String.format("This combination of provider id (%d) and country id in provider (%d) is invalid",
                    idProvider, idCountryAtProvider));
        }
        return countryRepository.findById(existingCountryProviderRelations.get().getCountryId()).get();
    }

    private void validateProviderDataUpdate(CountryUpdateDto countryUpdateDto, Country existingCountry) {
        validateProviderId(countryUpdateDto.getIdProvider());
        boolean isUpdatedCountryProviderRelationsPartOfExistingCountry = false;
        if (!CollectionUtils.isEmpty(existingCountry.getCountryProviderRelationsList())) {
            isUpdatedCountryProviderRelationsPartOfExistingCountry = existingCountry.getCountryProviderRelationsList().stream()
                    .anyMatch(cpr -> Objects.equals(cpr.getIdProvider(), countryUpdateDto.getIdProvider())
                            && Objects.equals(cpr.getIdCountryAtProvider(), countryUpdateDto.getIdCountryAtProvider())
                    );
        }
        if (!isUpdatedCountryProviderRelationsPartOfExistingCountry) {
            Optional<CountryProviderRelations> existingCountryProviderRelations =
                    countryProviderRelationsRepository.findByIdProviderAndIdCountryAtProvider(
                            countryUpdateDto.getIdProvider(), countryUpdateDto.getIdCountryAtProvider());
            if (existingCountryProviderRelations.isPresent()) {
                throw new IllegalArgumentException(String.format(
                        "This combination of provider id (%d) and country id in provider (%d) is already present for another country",
                        countryUpdateDto.getIdProvider(), countryUpdateDto.getIdCountryAtProvider()));
            }
        }
    }

    private void validateProviderId(int idProvider) {
        Optional<Provider> providerOptional = providerRepository.findById(idProvider);
        if (!providerOptional.isPresent()) {
            throw new IllegalArgumentException(String.format("Invalid provider id: %d", idProvider));
        }
    }

    private void validateUpdateDto(CountryUpdateDto countryUpdateDto, Country existingCountry) {
        if (
                (Objects.isNull(countryUpdateDto.getIdProvider()) && !Objects.isNull(countryUpdateDto.getIdCountryAtProvider())) ||
                        (!Objects.isNull(countryUpdateDto.getIdProvider()) && Objects.isNull(countryUpdateDto.getIdCountryAtProvider()))) {
            throw new IllegalArgumentException("Either send both provider id and country id in provider or do not send any of them");
        }

        if (!Objects.isNull(countryUpdateDto.getIdProvider())) {
            validateProviderDataUpdate(countryUpdateDto, existingCountry);
        }

        if (!Objects.isNull(countryUpdateDto.getName()) && !Objects.isNull(countryUpdateDto.getAbbreviation())
                && !existingCountry.getName().equalsIgnoreCase(countryUpdateDto.getName())
                && !existingCountry.getAbbreviation().equalsIgnoreCase(countryUpdateDto.getAbbreviation()) ) {
            Optional<Country> anotherCountryOptional = countryRepository.findByNameAndAbbreviation(countryUpdateDto.getName(), countryUpdateDto.getAbbreviation());
            if (anotherCountryOptional.isPresent()) {
                throw new IllegalArgumentException(String.format("There is a different country with this name (%s) and this abbreviation (%s)",
                        countryUpdateDto.getName(), countryUpdateDto.getAbbreviation()));
            }
        }
    }

}

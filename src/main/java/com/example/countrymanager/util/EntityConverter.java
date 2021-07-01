package com.example.countrymanager.util;

import com.example.countrymanager.dto.CountryCreateDto;
import com.example.countrymanager.dto.CountryGetDto;
import com.example.countrymanager.dto.ProviderDto;
import com.example.countrymanager.model.Country;
import com.example.countrymanager.model.CountryProviderRelations;
import org.springframework.util.StringUtils;

import java.util.*;

public class EntityConverter {

    private static final int NUMERAL_ZERO_LEFT_LIMIT = 48; // numeral '0'
    private static final int LETTER_Z_RIGHT_LIMIT = 122; // letter 'z'
    private static final int TARGET_STRING_LENGTH = 5;
    private static final String ID_COUNTRY_PREFIX="fz:country:";

    private Random random = new Random();

    public static String getFormattedName(String name) {
        return StringUtils.capitalize(name.toLowerCase());
    }

    public static String getFormattedAbbreviation(String abbreviation) {
        return abbreviation.toUpperCase();
    }

    public Country convertCreateDtoToEntity(CountryCreateDto countryCreateDto) {
        Country country = new Country();
        country.setId(generateIdForCountry());
        country.setName(getFormattedName(countryCreateDto.getName()));
        country.setAbbreviation(getFormattedAbbreviation(countryCreateDto.getAbbreviation()));
        List<CountryProviderRelations> countryProviderRelationsList = new ArrayList<>();
        CountryProviderRelations countryProviderRelations = new CountryProviderRelations();
        countryProviderRelations.setCountryId(country.getId());
        countryProviderRelations.setIdProvider(countryCreateDto.getIdProvider());
        countryProviderRelations.setIdCountryAtProvider(countryCreateDto.getIdCountryAtProvider());
        countryProviderRelationsList.add(countryProviderRelations);
        country.setCountryProviderRelationsList(countryProviderRelationsList);

        return country;
    }

    public CountryGetDto convertEntityToGetDto(Country country) {
        CountryGetDto countryDto = new CountryGetDto();
        countryDto.setId(country.getId());
        List<ProviderDto> providers = new ArrayList<>();
        for (CountryProviderRelations cpr : country.getCountryProviderRelationsList()) {
            providers.add(new ProviderDto(cpr.getIdProvider(), cpr.getIdCountryAtProvider()));
        }
        countryDto.setProviders(providers);
        countryDto.setName(country.getName());
        countryDto.setAbbreviation(country.getAbbreviation());
        countryDto.setCreatedAt(country.getCreatedAt());
        countryDto.setUpdatedAt(country.getUpdatedAt());
        return countryDto;
    }

    private String generateIdForCountry() {
        String generatedString = random.ints(NUMERAL_ZERO_LEFT_LIMIT, LETTER_Z_RIGHT_LIMIT + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(TARGET_STRING_LENGTH)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return String.format("%s%s", ID_COUNTRY_PREFIX, generatedString);
    }

}

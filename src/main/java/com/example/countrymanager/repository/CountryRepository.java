package com.example.countrymanager.repository;

import com.example.countrymanager.model.Country;
import com.example.countrymanager.model.CountryProviderRelations;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CountryRepository extends MongoRepository<Country, String> {

    Optional<Country> findByNameAndAbbreviation(String name, String abbreviation);

    // id_provider_and_id_country_at_provider_index
    Optional<Country> findByCountryProviderRelationsList(CountryProviderRelations countryProviderRelations);
    // CountryProviderRelations

}

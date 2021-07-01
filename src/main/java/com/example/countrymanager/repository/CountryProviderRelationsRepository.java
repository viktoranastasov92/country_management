package com.example.countrymanager.repository;

import com.example.countrymanager.model.CountryProviderRelations;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface CountryProviderRelationsRepository extends MongoRepository<CountryProviderRelations, UUID> {

    Optional<CountryProviderRelations> findByIdProviderAndIdCountryAtProvider(int idProvider, int idCountryAtProvider);

}

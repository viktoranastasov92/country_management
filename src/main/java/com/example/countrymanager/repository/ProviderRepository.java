package com.example.countrymanager.repository;

import com.example.countrymanager.model.Provider;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProviderRepository extends MongoRepository<Provider, Integer> {
}

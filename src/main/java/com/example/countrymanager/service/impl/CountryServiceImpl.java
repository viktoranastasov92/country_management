package com.example.countrymanager.service.impl;

import static com.example.countrymanager.util.EntityConverter.*;
import static com.example.countrymanager.model.Country.*;

import com.example.countrymanager.dto.CountryUpdateDto;
import com.example.countrymanager.model.Country;
import com.example.countrymanager.model.CountryProviderRelations;
import com.example.countrymanager.repository.CountryProviderRelationsRepository;
import com.example.countrymanager.repository.CountryRepository;
import com.example.countrymanager.service.CountryService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class CountryServiceImpl implements CountryService {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CountryProviderRelationsRepository countryProviderRelationsRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<Country> getCountriesResponse(List<String> ids, String name, String abbreviation, Integer idProvider,
                                              Integer idCountryAtProvider, int page, int size) {
        Pageable paging = PageRequest.of(page, size);
        return generateCountriesPagedResponse(ids, name, abbreviation, idProvider, idCountryAtProvider, paging);
    }

    @Override
    @Transactional
    public Country createCountry(Country country) throws IllegalArgumentException {
        country.setCountryProviderRelationsList(countryProviderRelationsRepository.saveAll(country.getCountryProviderRelationsList()));
        return countryRepository.save(country);
    }

    @Override
    @Transactional
    public Country updateCountry(Country existingCountry, CountryUpdateDto countryUpdateDto) {
        if (!Objects.isNull(countryUpdateDto.getIdProvider()) && !Objects.isNull(countryUpdateDto.getIdCountryAtProvider())) {
            updateCountryDataProvider(existingCountry, countryUpdateDto);
        }

        if (StringUtils.hasLength(countryUpdateDto.getName())) {
            existingCountry.setName(getFormattedName(countryUpdateDto.getName()));
        }
        if (StringUtils.hasLength(countryUpdateDto.getAbbreviation())) {
            existingCountry.setAbbreviation(getFormattedAbbreviation(countryUpdateDto.getAbbreviation()));
        }

        return countryRepository.save(existingCountry);
    }

    @Override
    @Transactional
    public void deleteCountry(Country existingCountry) {
        countryProviderRelationsRepository.deleteAll(existingCountry.getCountryProviderRelationsList());
        countryRepository.delete(existingCountry);
    }

    private void updateCountryDataProvider(Country existingCountry, CountryUpdateDto countryUpdateDto) {
        boolean isProviderDataPresent = false;
        if (!CollectionUtils.isEmpty(existingCountry.getCountryProviderRelationsList())) {
            for (CountryProviderRelations countryProviderRelations : existingCountry.getCountryProviderRelationsList()) {
                if (Objects.equals(countryProviderRelations.getIdProvider(), countryUpdateDto.getIdProvider())) {
                    countryProviderRelations.setIdCountryAtProvider(countryUpdateDto.getIdCountryAtProvider());
                    isProviderDataPresent = true;
                    break;
                }
            }
        }
        if (!isProviderDataPresent) {
            CountryProviderRelations countryProviderRelations = CountryProviderRelations.builder()
                    .countryId(existingCountry.getId())
                    .idProvider(countryUpdateDto.getIdProvider())
                    .idCountryAtProvider(countryUpdateDto.getIdCountryAtProvider())
                    .build();
            if (CollectionUtils.isEmpty(existingCountry.getCountryProviderRelationsList())) {
                existingCountry.setCountryProviderRelationsList(Arrays.asList(countryProviderRelations));
            } else {
                existingCountry.getCountryProviderRelationsList().add(countryProviderRelations);
            }
        }
        existingCountry.setCountryProviderRelationsList(countryProviderRelationsRepository.saveAll(existingCountry.getCountryProviderRelationsList()));
    }

    private Page<Country> generateCountriesPagedResponse(List<String> ids, String name, String abbreviation,
                                                         Integer idProvider, Integer idCountryAtProvider, Pageable paging) {
        final Query query = new Query().with(paging);
        final List<Criteria> criteria = new ArrayList<>();

        if (!CollectionUtils.isEmpty(ids)) {
            criteria.add(Criteria.where(ID_COLUMN).in(ids));
        }
        if (StringUtils.hasLength(name)) {
            criteria.add(Criteria.where(NAME_COLUMN).regex(String.format(".*%s.*", name), "i"));
        }
        if (StringUtils.hasLength(abbreviation)) {
            criteria.add(Criteria.where(ABBREVIATION_COLUMN).regex(String.format(".*%s.*", abbreviation), "i"));
        }
        if (!Objects.isNull(idProvider) && !Objects.isNull(idCountryAtProvider)) {
            Optional<CountryProviderRelations> countryProviderRelationsOptional =
                    countryProviderRelationsRepository.findByIdProviderAndIdCountryAtProvider(idProvider, idCountryAtProvider);
            if (countryProviderRelationsOptional.isPresent()) {
                criteria.add(Criteria.where("country_provider_data.$id").is(new ObjectId(countryProviderRelationsOptional.get().getObjectId())));
            } else {
                return PageableExecutionUtils.getPage(new ArrayList<>(), paging, () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Country.class));
            }
        }

        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));
        }

        List<Country> countries = mongoTemplate.find(query, Country.class);
        return PageableExecutionUtils.getPage(countries, paging, () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Country.class));
    }

}

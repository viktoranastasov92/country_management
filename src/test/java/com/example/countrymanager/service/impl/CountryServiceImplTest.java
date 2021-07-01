package com.example.countrymanager.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.example.countrymanager.model.Country.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;

import com.example.countrymanager.dto.CountryUpdateDto;
import com.example.countrymanager.model.Country;
import com.example.countrymanager.model.CountryProviderRelations;
import com.example.countrymanager.repository.CountryProviderRelationsRepository;
import com.example.countrymanager.repository.CountryRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class CountryServiceImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private CountryProviderRelationsRepository countryProviderRelationsRepository;

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private CountryServiceImpl countryService;

    @Test
    void testGetCountriesResponseWithoutProviderData() {
        List<String> ids = Arrays.asList("test_id_1", "test_id_2");
        String name = "test_name";
        String abbreviation = "abbr";
        Integer idProvider = 1;
        Integer idCountryAtProvider = 10;
        int page = 0;
        int size = 10;
        Query query = new Query().with(PageRequest.of(page, size));
        when(countryProviderRelationsRepository.findByIdProviderAndIdCountryAtProvider(idProvider, idCountryAtProvider)).thenReturn(Optional.empty());
        Page<Country> expectedCountryPageable = PageableExecutionUtils.getPage(new ArrayList<>(), PageRequest.of(page, size), () -> 0);

        Page<Country> actualCountryPageable = countryService.getCountriesResponse(ids, name, abbreviation, idProvider, idCountryAtProvider, page, size);

        verify(mongoTemplate, times(0)).find(query, Country.class);
        assertEquals(expectedCountryPageable, actualCountryPageable);
    }

    @Test
    void testGetCountriesResponseWithProviderData() {
        List<String> ids = Arrays.asList("test_id_1", "test_id_2");
        String name = "test_name";
        String abbreviation = "abbr";
        Integer idProvider = 1;
        Integer idCountryAtProvider = 10;
        int page = 0;
        int size = 10;
        CountryProviderRelations countryProviderRelations = CountryProviderRelations.builder()
                .objectId("507f1f77bcf86cd799439011")
                .countryId("country_id")
                .idProvider(idProvider)
                .idCountryAtProvider(idCountryAtProvider)
                .build();
        Query query = new Query().with(PageRequest.of(page, size));
        List<Criteria> criteriaList = Arrays.asList(Criteria.where(ID_COLUMN).in(ids),
                Criteria.where(NAME_COLUMN).regex(String.format(".*%s.*", name), "i"),
                Criteria.where(ABBREVIATION_COLUMN).regex(String.format(".*%s.*", abbreviation), "i"),
                Criteria.where("country_provider_data.$id").is(new ObjectId(countryProviderRelations.getObjectId())));
        query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()])));
        when(countryProviderRelationsRepository.findByIdProviderAndIdCountryAtProvider(idProvider, idCountryAtProvider)).thenReturn(Optional.of(countryProviderRelations));
        List<Country> countryList = Arrays.asList(new Country(), new Country());
        when(mongoTemplate.find(query, Country.class)).thenReturn(countryList);
        Page<Country> expectedCountryPageable = PageableExecutionUtils.getPage(countryList, PageRequest.of(page, size), () -> 2);

        Page<Country> actualCountryPageable = countryService.getCountriesResponse(ids, name, abbreviation, idProvider, idCountryAtProvider, page, size);

        verify(mongoTemplate, times(1)).find(query, Country.class);
        assertEquals(expectedCountryPageable, actualCountryPageable);
    }

    @Test
    void testCreateCountry() {
        final String idCountry = "id_country_1";
        Country country = Country.builder().id(idCountry).name("test_name").abbreviation("abbr").build();
        country.setCountryProviderRelationsList(Arrays.asList(
                CountryProviderRelations.builder()
                        .countryId(idCountry)
                        .idProvider(1)
                        .idCountryAtProvider(10)
                        .build()));
        doAnswer(i -> i.getArguments()[0]).when(countryProviderRelationsRepository).saveAll(anyList());
        when(countryRepository.save(any())).then(returnsFirstArg());

        Country actualCountry = countryService.createCountry(country);

        verify(countryProviderRelationsRepository, times(1)).saveAll(country.getCountryProviderRelationsList());
        verify(countryRepository, times(1)).save(country);
        assertEquals(country, actualCountry);
    }

    @Test
    void testUpdateCountryNameAndAbbreviation() {
        String incomingName = "changed_name";
        String capitalizedName = "Changed_name";
        String incomingAbbreviation = "changed_abbr";
        String upperCasedAbbreviation = "CHANGED_ABBR";
        CountryUpdateDto countryUpdateDto = CountryUpdateDto.builder().name(incomingName).abbreviation(incomingAbbreviation).build();
        when(countryRepository.save(any())).then(returnsFirstArg());

        Country updatedCountry = countryService.updateCountry(new Country(), countryUpdateDto);

        verify(countryProviderRelationsRepository, times(0)).saveAll(any());
        verify(countryRepository, times(1)).save(
                Country.builder().id(null).name(capitalizedName).abbreviation(upperCasedAbbreviation).build());
        assertEquals(capitalizedName, updatedCountry.getName());
        assertEquals(upperCasedAbbreviation, updatedCountry.getAbbreviation());
    }

    @Test
    void testUpdateCountryExistingProviderData() {
        CountryUpdateDto countryUpdateDto = CountryUpdateDto.builder()
                .idProvider(1)
                .idCountryAtProvider(10)
                .build();
        List<CountryProviderRelations> existingCountryProviderRelationsList = Arrays.asList(
                CountryProviderRelations.builder()
                        .countryId("")
                        .idProvider(1)
                        .idCountryAtProvider(20)
                        .build());
        List<CountryProviderRelations> updatedCountryProviderRelationsList = Arrays.asList(
                CountryProviderRelations.builder()
                        .countryId("")
                        .idProvider(1)
                        .idCountryAtProvider(10)
                        .build());
        Country existingCountry = new Country();
        existingCountry.setCountryProviderRelationsList(existingCountryProviderRelationsList);
        Country expectedCountry = new Country();
        expectedCountry.setCountryProviderRelationsList(updatedCountryProviderRelationsList);
        doAnswer(i -> i.getArguments()[0]).when(countryProviderRelationsRepository).saveAll(anyList());
        when(countryRepository.save(any())).then(returnsFirstArg());

        Country updatedCountry = countryService.updateCountry(existingCountry, countryUpdateDto);

        assertEquals(expectedCountry, updatedCountry);
        verify(countryProviderRelationsRepository, times(1)).saveAll(updatedCountryProviderRelationsList);
        verify(countryRepository, times(1)).save(expectedCountry);
    }

    @Test
    void testUpdateCountryAddNewProviderData() {
        final String idCountry = "country_id";
        CountryUpdateDto countryUpdateDto = CountryUpdateDto.builder()
                .idProvider(1)
                .idCountryAtProvider(10)
                .build();
        List<CountryProviderRelations> updatedCountryProviderRelationsList = Arrays.asList(
                CountryProviderRelations.builder()
                        .countryId(idCountry)
                        .idProvider(1)
                        .idCountryAtProvider(10)
                        .build());
        Country existingCountry = new Country();
        existingCountry.setId(idCountry);
        Country expectedCountry = new Country();
        expectedCountry.setId(idCountry);
        expectedCountry.setCountryProviderRelationsList(updatedCountryProviderRelationsList);
        doAnswer(i -> i.getArguments()[0]).when(countryProviderRelationsRepository).saveAll(anyList());
        when(countryRepository.save(any())).then(returnsFirstArg());

        Country updatedCountry = countryService.updateCountry(existingCountry, countryUpdateDto);

        assertEquals(expectedCountry, updatedCountry);
        verify(countryProviderRelationsRepository, times(1)).saveAll(updatedCountryProviderRelationsList);
        verify(countryRepository, times(1)).save(expectedCountry);
    }

    @Test
    void testUpdateCountryAddAnotherProviderData() {
        final String idCountry = "country_id";
        CountryUpdateDto countryUpdateDto = CountryUpdateDto.builder()
                .idProvider(1)
                .idCountryAtProvider(10)
                .build();
        CountryProviderRelations existingCountryProviderRelations = CountryProviderRelations.builder()
                .countryId(idCountry)
                .idProvider(3)
                .idCountryAtProvider(45)
                .build();
        CountryProviderRelations incomingCountryProviderRelations = CountryProviderRelations.builder()
                .countryId(idCountry)
                .idProvider(1)
                .idCountryAtProvider(10)
                .build();
        List<CountryProviderRelations> updatedCountryProviderRelationsList = Arrays.asList(
                existingCountryProviderRelations, incomingCountryProviderRelations);
        Country existingCountry = new Country();
        existingCountry.setId(idCountry);
        existingCountry.setCountryProviderRelationsList(new ArrayList<>());
        existingCountry.getCountryProviderRelationsList().add(existingCountryProviderRelations);

        Country expectedCountry = new Country();
        expectedCountry.setId(idCountry);
        expectedCountry.setCountryProviderRelationsList(Arrays.asList(
                existingCountry.getCountryProviderRelationsList().get(0), updatedCountryProviderRelationsList.get(1)));
        doAnswer(i -> i.getArguments()[0]).when(countryProviderRelationsRepository).saveAll(anyList());
        when(countryRepository.save(any())).then(returnsFirstArg());

        Country updatedCountry = countryService.updateCountry(existingCountry, countryUpdateDto);

        assertEquals(expectedCountry, updatedCountry);
        verify(countryProviderRelationsRepository, times(1)).saveAll(updatedCountryProviderRelationsList);
        verify(countryRepository, times(1)).save(expectedCountry);
    }

    @Test
    void testDeleteCountry() {
        Country country = new Country();
        List<CountryProviderRelations> countryProviderRelationsList = Arrays.asList(CountryProviderRelations.builder()
                .countryId("idCountry")
                .idProvider(1)
                .idCountryAtProvider(10)
                .build());
        country.setCountryProviderRelationsList(countryProviderRelationsList);

        countryService.deleteCountry(country);

        verify(countryProviderRelationsRepository, times(1)).deleteAll(countryProviderRelationsList);
        verify(countryRepository, times(1)).delete(country);
    }

}

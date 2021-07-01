package com.example.countrymanager.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "country_provider_relations")
@CompoundIndex(name = "id_provider_and_id_country_at_provider_index", unique = true, def = "{'id_provider' : 1, 'id_country_at_provider': 1}")
@Data
@SuperBuilder
@NoArgsConstructor
public class CountryProviderRelations {

    @Id
    String objectId;

    @NonNull
    private String countryId;

    @Field(name = "id_provider")
    @NonNull
    private int idProvider;

    @Field(name = "id_country_at_provider")
    @NonNull
    Integer idCountryAtProvider;

}

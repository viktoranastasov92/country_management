package com.example.countrymanager.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "country", collation = "{'locale':'en', 'strength':2}")
@CompoundIndex(name = "country_name_and_abbreviation_index", unique = true, def = "{'name' : 1, 'abbreviation': 1}")
@Data
@SuperBuilder
@NoArgsConstructor
public class Country {

    public static final String ID_COLUMN = "id";
    public static final String NAME_COLUMN = "name";
    public static final String ABBREVIATION_COLUMN = "abbreviation";

    @Id
    private String id;

    @NonNull
    private String name;

    @NonNull
    @Indexed
    private String abbreviation;

    @Field("country_provider_data")
    @DBRef
    List<CountryProviderRelations> countryProviderRelationsList;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;

}

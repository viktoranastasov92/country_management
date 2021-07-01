package com.example.countrymanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "provider")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Provider {

    @Id
    int id;

    String name;

}

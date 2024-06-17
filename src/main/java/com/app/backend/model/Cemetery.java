package com.app.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Cemetery {
    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    private String address;
    private String city;

}

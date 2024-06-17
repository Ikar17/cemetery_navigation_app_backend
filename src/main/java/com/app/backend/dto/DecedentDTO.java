package com.app.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecedentDTO {
    private Integer id;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private LocalDate deathDate;
    private String description;
    private Float latitude;
    private Float longitude;
    private Integer cemeteryId;
    private Integer userId;
    private String city;
    private String imageBase64;
}

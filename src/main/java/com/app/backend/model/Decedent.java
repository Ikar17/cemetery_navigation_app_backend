package com.app.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_decedent")
public class Decedent {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String surname;

    private LocalDate birthDate;
    private LocalDate deathDate;

    private String description;

    private float latitude;
    private float longitude;

    @ManyToOne
    @JoinColumn(name = "cemetery_id")
    private Cemetery cemetery;

    @Lob
    @Column(name = "tombstone_image")
    private byte[] tombstoneImage;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public int calculateAge() {
        if (birthDate != null && deathDate != null) {
            return Period.between(birthDate, deathDate).getYears();
        } else {
            return 0;
        }
    }

}

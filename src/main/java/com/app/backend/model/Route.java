package com.app.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {
    @Id
    @GeneratedValue
    private Integer id;
    @ManyToOne
    private Decedent decedent;
    @ManyToOne
    private User user;
    private String videoName;
}

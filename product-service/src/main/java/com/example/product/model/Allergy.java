package com.example.product.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "allergy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Allergy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allergy_id")
    private Integer allergyId;

    @Column(name = "allergy_name")
    private String allergyName;
}

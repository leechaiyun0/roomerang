package com.roomerang.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class FavoritePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)



}

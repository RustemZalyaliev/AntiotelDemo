package com.example.antioteldemo.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Entity
@Table(name = "guests")
public class Guest {

    @Id
    private Long chatId;
    private String firstName;
    private String lastName;
    private String username;
    private String registeredAt;
    private Boolean isAdmin;

}

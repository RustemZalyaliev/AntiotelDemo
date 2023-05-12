package com.example.antioteldemo.entities.roombookings;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
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
@Table(name = "shabash_bookings")
public class ShabashBooking {

    @Id
    @GeneratedValue
    private Long bookingId;
    private String guestName;
    private Long chatId;
    private String timeFrom;
    private String timeTo;
    private Integer hours;
    private Integer profit;
    private Boolean prepay;
    private String startOrderTime;

}

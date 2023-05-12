package com.example.antioteldemo.pojo;

import lombok.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Component
@Scope("prototype")
public class CurrentOrder {

    private Long chatId;
    private String roomName;
    private int monthOfBooking;
    private LocalDate dateFrom;
    private Boolean firstTimeMenu;
    private int hourPrice;
    private int firstDayMessageId;
    private int secondDayMessageId;
    private int calcPanelMessageId;
    private LocalDateTime timeFrom;
    private LocalDateTime timeTo;
    private Integer hours;
    private Integer profit;
    private Boolean prepay;
    private LocalDateTime startOrderTime;

}

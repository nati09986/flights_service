package com.example.flights_service.models;

import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Flight {
    private String id;
    private Date arrival;
    private Date departure;
    private String success;
}


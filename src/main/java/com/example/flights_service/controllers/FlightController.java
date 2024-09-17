package com.example.flights_service.controllers;

import com.example.flights_service.models.Flight;
import com.example.flights_service.services.FlightService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/flights")
public class FlightController {
    private static final Logger logger = LoggerFactory.getLogger(FlightController.class);

    @Autowired
    private FlightService flightService;

    @GetMapping("/{id}")
    public ResponseEntity<Flight> getFlight(@PathVariable String id) {
        logger.info("Fetching flight details for ID: {}", id);
        try {
            Flight flight = flightService.getFlightById(id);
            if (flight != null) {
                return ResponseEntity.ok(flight);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException | ParseException e) {
            logger.error("Error retrieving flight with ID: {}", id, e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateFlights(@RequestBody List<Flight> flights) {
        logger.info("Updating flights with {} entries", flights.size());
        try {
            flightService.updateFlights(flights);
            return ResponseEntity.ok("CSV file updated successfully");
        } catch (IOException | ParseException e) {
            logger.error("Error updating the CSV file", e);
            return ResponseEntity.status(500).body("Error updating the CSV file");
        }
    }
}

package com.example.flights_service.services;

import com.example.flights_service.models.Flight;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FlightService {

    private static final String INPUT_FILE_PATH = "flights.csv";
    private static final String OUTPUT_FILE_PATH = "flights_with_success.csv";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm");
    private static final int MAX_SUCCESSFUL_FLIGHTS = 20;

    @PostConstruct
    public void initialize() {
        try {
            processFlights();
            log.info("The file flights_with_success.csv was successfully created upon server startup.");
        } catch (IOException | ParseException e) {
            log.error("Error creating the processed file upon server startup: {}", e.getMessage());
        }
    }

    public void updateFlights(List<Flight> flights) throws IOException, ParseException {
        writeFlightsToCSV(flights, INPUT_FILE_PATH);
        processFlights();
    }

    private void processFlights() throws IOException, ParseException {
        List<Flight> flights = readFlightsFromCSV(INPUT_FILE_PATH);
        List<Flight> processedFlights = processFlightSuccess(flights);
        writeFlightsToCSV(processedFlights, OUTPUT_FILE_PATH);
    }

    private List<Flight> readFlightsFromCSV(String filePath) throws IOException, ParseException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            return reader.lines()
                    .skip(1)
                    .filter(line -> !line.trim().isEmpty())
                    .map(this::parseFlight)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(Flight::getArrival))
                    .collect(Collectors.toList());
        }
    }

    private Flight parseFlight(String line) {
        try {
            String[] values = line.split(",");
            if (values.length < 4) {
                log.warn("Skipping malformed line: {}", line);
                return null;
            }
            return new Flight(
                    values[0].trim(),
                    DATE_FORMAT.parse(values[1].trim()),
                    DATE_FORMAT.parse(values[2].trim()),
                    values[3].trim()
            );
        } catch (ParseException e) {
            log.error("Error parsing flight line: {}", line, e);
            return null;
        }
    }

    private List<Flight> processFlightSuccess(List<Flight> flights) {
        List<Flight> successfulFlights = new ArrayList<>();
        int successCount = 0;

        for (Flight flight : flights) {
            if (isSuccessful(flight) && successCount < MAX_SUCCESSFUL_FLIGHTS) {
                flight.setSuccess("success");
                successCount++;
            } else {
                flight.setSuccess("fail");
            }
            successfulFlights.add(flight);
        }

        return successfulFlights;
    }

    private boolean isSuccessful(Flight flight) {
        long duration = (flight.getDeparture().getTime() - flight.getArrival().getTime()) / (60 * 1000);
        return duration >= 180;
    }

    private void writeFlightsToCSV(List<Flight> flights, String filePath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
            writer.write("flight ID, Arrival, Departure, success\n");
            for (Flight flight : flights) {
                writer.write(formatFlight(flight));
            }
        }
    }

    private String formatFlight(Flight flight) {
        return String.format("%s, %s, %s, %s\n",
                flight.getId(),
                DATE_FORMAT.format(flight.getArrival()),
                DATE_FORMAT.format(flight.getDeparture()),
                flight.getSuccess());
    }

    public Flight getFlightById(String id) throws IOException, ParseException {
        List<Flight> flights = readFlightsFromCSV(OUTPUT_FILE_PATH);
        return flights.stream()
                .filter(flight -> flight.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}

package com.cimhub.tripservice.service.trip;

import com.cimhub.tripservice.dto.response.TripResponse;
import com.cimhub.tripservice.mapper.TripMapper;
import com.cimhub.tripservice.model.TripStatus;
import com.cimhub.tripservice.repository.TripsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripSearchService {

    private final TripsRepository tripsRepository;
    private final TripMapper tripMapper;

    public List<TripResponse> searchAvailableTrips(String userTimezone) {
        return tripsRepository.findAll().stream()
                .filter(trip -> trip.getTripStatus() == TripStatus.CREATED
                        || trip.getTripStatus() == TripStatus.AVAILABLE)
                .map(trip -> tripMapper.toTripResponse(trip, userTimezone))
                .collect(Collectors.toList());
    }
}

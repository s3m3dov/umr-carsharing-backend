package com.cimhub.tripservice.service.trip;

import com.cimhub.common.enums.TripStatus;
import com.cimhub.tripservice.dto.response.TripResponse;
import com.cimhub.tripservice.mapper.TripMapper;
import com.cimhub.tripservice.repository.TripsRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripSearchService {

    private final TripsRepository tripsRepository;
    private final TripMapper tripMapper;

    public List<TripResponse> searchAvailableTrips(String userTimezone) {
        return tripsRepository.findAll().stream()
                .filter(trip ->
                        trip.getTripStatus() == TripStatus.CREATED || trip.getTripStatus() == TripStatus.AVAILABLE)
                .map(trip -> tripMapper.toTripResponse(trip, userTimezone))
                .collect(Collectors.toList());
    }
}

package com.cimhub.tripservice.service;

import com.cimhub.tripservice.dto.OfferRideRequestDTO;
import com.cimhub.tripservice.dto.OfferRideResponseDTO;

/**
 * Service for offering new trips
 */
public interface OfferRideService {

    /**
     * Create a new trip offer
     * 
     * @param offerRideRequest Trip details
     * @return Trip creation response with trip ID and details
     */
    OfferRideResponseDTO offerRide(OfferRideRequestDTO offerRideRequest);
}

package com.cimhub.tripservice.service;

import com.cimhub.common.dto.RequestDTO;
import com.cimhub.common.dto.ResponseDTO;
import com.cimhub.tripservice.dto.OfferRideRequestDTO;
import com.cimhub.tripservice.dto.OfferRideResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface OfferRideService {

    public ResponseDTO<OfferRideResponseDTO> offerRide(RequestDTO<OfferRideRequestDTO> offerRideRequest);
}

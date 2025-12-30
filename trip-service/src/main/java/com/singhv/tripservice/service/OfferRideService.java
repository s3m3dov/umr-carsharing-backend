package com.singhv.tripservice.service;

import com.singhv.common.dto.RequestDTO;
import com.singhv.common.dto.ResponseDTO;
import com.singhv.tripservice.dto.OfferRideRequestDTO;
import com.singhv.tripservice.dto.OfferRideResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface OfferRideService {

    public ResponseDTO<OfferRideResponseDTO> offerRide(RequestDTO<OfferRideRequestDTO> offerRideRequest);
}

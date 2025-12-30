package com.singhv.tripservice.dto.OSRM;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Waypoint {
    private String hint;
    private List<Double> location; // [lon, lat]
    private String name;
    private double distance;
}

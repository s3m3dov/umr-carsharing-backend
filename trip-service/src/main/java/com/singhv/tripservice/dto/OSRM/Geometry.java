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
public class Geometry {
    private String type; // "LineString"
    private List<List<Double>> coordinates; // list of [lon, lat]
}

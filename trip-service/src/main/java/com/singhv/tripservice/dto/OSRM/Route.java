package com.singhv.tripservice.dto.OSRM;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import com.singhv.tripservice.dto.OSRM.Legs;
import com.singhv.tripservice.dto.OSRM.Geometry;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Route {

    private List<Legs> legs;
    private String weight_name;
    private Geometry geometry;
    private double weight;
    private double duration;
    private double distance;

}

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
public class Legs {
    private List<Object> steps;
    private double weight;
    private String summary;
    private double duration;
    private double distance;
}

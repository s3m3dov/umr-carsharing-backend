package com.cimhub.common.model;

import lombok.Data;

@Data
public class Points {
    private double latitude;
    private double longitude;
    private String placeId = "";
    private String placeAddress = null;
}

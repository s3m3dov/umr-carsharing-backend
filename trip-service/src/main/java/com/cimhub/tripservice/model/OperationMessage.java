package com.cimhub.tripservice.model;

public enum OperationMessage {
    TRIP_STARTED_SUCCESS("Trip started successfully"),
    RIDE_COMPLETED_SUCCESS("Ride completed successfully"),
    TRIP_COMPLETED_SUCCESS("Trip completed successfully. %d remaining ride(s) were auto-completed");

    private final String template;

    OperationMessage(String template) {
        this.template = template;
    }

    public String text() {
        return template;
    }

    public String format(Object... args) {
        return String.format(template, args);
    }
}

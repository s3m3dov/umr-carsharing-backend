package com.cimhub.userservice.service;

import com.cimhub.userservice.dto.request.CreatePassengerProfileRequest;
import com.cimhub.userservice.dto.request.UpdatePassengerRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProfileUpdaterTest {

    @Test
    void applyCommonProfileFields_shouldSetAllFields() {
        CreatePassengerProfileRequest request = new CreatePassengerProfileRequest("John", "Doe", "+123", 30);

        Holder holder = new Holder();
        ProfileUpdater.applyCommonProfileFields(
                request,
                value -> holder.firstName = value,
                value -> holder.lastName = value,
                value -> holder.phoneNumber = value,
                value -> holder.age = value);

        assertEquals("John", holder.firstName);
        assertEquals("Doe", holder.lastName);
        assertEquals("+123", holder.phoneNumber);
        assertEquals(30, holder.age);
    }

    @Test
    void applyCommonProfileUpdates_shouldOnlySetNonNullFields() {
        UpdatePassengerRequest request = new UpdatePassengerRequest(null, "Smith", null, 35);

        Holder holder = new Holder();
        holder.firstName = "Jane";
        holder.lastName = "Doe";
        holder.phoneNumber = "+111";
        holder.age = 20;

        ProfileUpdater.applyCommonProfileUpdates(
                request,
                value -> holder.firstName = value,
                value -> holder.lastName = value,
                value -> holder.phoneNumber = value,
                value -> holder.age = value);

        assertEquals("Jane", holder.firstName);
        assertEquals("Smith", holder.lastName);
        assertEquals("+111", holder.phoneNumber);
        assertEquals(35, holder.age);
    }

    private static final class Holder {
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private Integer age;
    }
}

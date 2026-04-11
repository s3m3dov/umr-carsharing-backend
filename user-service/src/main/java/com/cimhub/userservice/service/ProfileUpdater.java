package com.cimhub.userservice.service;

import com.cimhub.userservice.dto.request.CreateProfileRequest;
import com.cimhub.userservice.dto.request.UpdateProfileRequest;

import java.util.function.Consumer;

public final class ProfileUpdater {

    private ProfileUpdater() {
    }

    public static void applyCommonProfileFields(CreateProfileRequest request,
                                                Consumer<String> firstNameSetter,
                                                Consumer<String> lastNameSetter,
                                                Consumer<String> phoneNumberSetter,
                                                Consumer<Integer> ageSetter) {
        firstNameSetter.accept(request.firstName());
        lastNameSetter.accept(request.lastName());
        phoneNumberSetter.accept(request.phoneNumber());
        ageSetter.accept(request.age());
    }

    public static void applyCommonProfileUpdates(UpdateProfileRequest request,
                                                 Consumer<String> firstNameSetter,
                                                 Consumer<String> lastNameSetter,
                                                 Consumer<String> phoneNumberSetter,
                                                 Consumer<Integer> ageSetter) {
        if (request.firstName() != null) {
            firstNameSetter.accept(request.firstName());
        }
        if (request.lastName() != null) {
            lastNameSetter.accept(request.lastName());
        }
        if (request.phoneNumber() != null) {
            phoneNumberSetter.accept(request.phoneNumber());
        }
        if (request.age() != null) {
            ageSetter.accept(request.age());
        }
    }
}

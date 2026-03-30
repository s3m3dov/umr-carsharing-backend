package com.cimhub.tripservice.service;

import com.cimhub.tripservice.dto.response.BookingSummaryReport;
import com.cimhub.tripservice.dto.response.DriverPerformanceReport;
import com.cimhub.tripservice.dto.response.PassengerActivityReport;
import com.cimhub.tripservice.dto.response.RevenueReport;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.BookingStatus;
import com.cimhub.tripservice.model.TripStatus;
import com.cimhub.tripservice.repository.BookingRepository;
import com.cimhub.tripservice.repository.TripsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportsService {

    private final TripsRepository tripsRepository;
    private final BookingRepository bookingRepository;

    public DriverPerformanceReport getDriverPerformanceReport() {
        long completed = tripsRepository.countByTripStatus(TripStatus.COMPLETED);
        long cancelled = tripsRepository.countByTripStatus(TripStatus.CANCELLED);
        long inProgress = tripsRepository.countByTripStatus(TripStatus.IN_PROGRESS);
        long available = tripsRepository.countByTripStatus(TripStatus.AVAILABLE);
        long totalDrivers = tripsRepository.findAll().stream()
                .map(t -> t.getDriverId())
                .distinct()
                .count();

        return DriverPerformanceReport.builder()
                .totalDrivers(totalDrivers)
                .totalTripsCompleted(completed)
                .totalTripsCancelled(cancelled)
                .totalTripsInProgress(inProgress)
                .totalTripsAvailable(available)
                .build();
    }

    public BookingSummaryReport getBookingSummaryReport() {
        long total = bookingRepository.count();
        long confirmed = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        long cancelled = bookingRepository.countByStatus(BookingStatus.CANCELLED);
        long requested = bookingRepository.countByStatus(BookingStatus.REQUESTED);
        long completed = bookingRepository.countByStatus(BookingStatus.COMPLETED);
        long rejected = bookingRepository.countByStatus(BookingStatus.REJECTED);

        return BookingSummaryReport.builder()
                .totalBookings(total)
                .confirmedBookings(confirmed)
                .cancelledBookings(cancelled)
                .requestedBookings(requested)
                .completedBookings(completed)
                .rejectedBookings(rejected)
                .build();
    }

    public RevenueReport getRevenueReport() {
        List<Booking> completedBookings = bookingRepository.findByStatus(BookingStatus.COMPLETED);
        double totalRevenue = completedBookings.stream()
                .mapToDouble(Booking::getEstimatedPrice)
                .sum();
        double avgRevenue = completedBookings.isEmpty() ? 0.0 : totalRevenue / completedBookings.size();

        return RevenueReport.builder()
                .totalCompletedBookings(completedBookings.size())
                .totalRevenue(totalRevenue)
                .averageRevenuePerBooking(avgRevenue)
                .build();
    }

    public PassengerActivityReport getPassengerActivityReport() {
        long total = bookingRepository.count();
        long active = bookingRepository.countByStatus(BookingStatus.CONFIRMED)
                + bookingRepository.countByStatus(BookingStatus.IN_PROGRESS);
        long completed = bookingRepository.countByStatus(BookingStatus.COMPLETED);
        long cancelled = bookingRepository.countByStatus(BookingStatus.CANCELLED);

        return PassengerActivityReport.builder()
                .totalBookings(total)
                .activeBookings(active)
                .completedBookings(completed)
                .cancelledBookings(cancelled)
                .build();
    }
}

package com.giap.Giaphotel.service.interfac;

import com.giap.Giaphotel.dto.Response;
import com.giap.Giaphotel.entity.Booking;

public interface IBookingService {
    Response saveBooking(Long roomId, Long userId, Booking bookingRequest);

    Response findBookingByConfirmationCode(String confirmationCode);

    Response getAllBookings();

    Response cancelBooking(Long bookingId);
}

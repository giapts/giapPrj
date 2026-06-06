package com.giap.Giaphotel.controller;

import com.giap.Giaphotel.dto.Response;
import com.giap.Giaphotel.entity.Booking;
import com.giap.Giaphotel.service.interfac.IBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")

public class BookingController {
    @Autowired
    private IBookingService iBookingService;

    @PostMapping("/book-room/{roomId}/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<Response> saveBooking(@PathVariable("roomId") Long roomId,
                                                @PathVariable("userId") Long userId,
                                                @RequestBody Booking bookingRequest){
        Response response=iBookingService.saveBooking(roomId, userId, bookingRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-by-confirmation-code/{confirmationCode}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> findBookingByConfirmationCode(@PathVariable("confirmationCode") String confirmationCode){
        Response response=iBookingService.findBookingByConfirmationCode(confirmationCode);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response>  getAllBookings(){
        Response response=iBookingService.getAllBookings();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/cancel/{bookingId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<Response>  cancelBooking(@PathVariable("bookingId") Long bookingId){
        Response response=iBookingService.cancelBooking(bookingId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}

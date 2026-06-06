package com.giap.Giaphotel.service.impl;

import com.giap.Giaphotel.dto.BookingDTO;
import com.giap.Giaphotel.dto.Response;
import com.giap.Giaphotel.entity.Booking;
import com.giap.Giaphotel.entity.Room;
import com.giap.Giaphotel.entity.User;
import com.giap.Giaphotel.exception.OurException;
import com.giap.Giaphotel.repo.BookingRepository;
import com.giap.Giaphotel.repo.RoomRepository;
import com.giap.Giaphotel.repo.UserRepository;
import com.giap.Giaphotel.service.interfac.IBookingService;
import com.giap.Giaphotel.service.interfac.IRoomService;
import com.giap.Giaphotel.ultils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService implements IBookingService {

    // Logger để ghi log
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private IRoomService iRoomService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Response saveBooking(Long roomId, Long userId, Booking bookingRequest) {
        logger.info("saveBooking called with roomId: {}, userId: {}, bookingRequest: {}", roomId, userId, bookingRequest);

        Response response = new Response();
        try {
            // Kiểm tra logic ngày đặt phòng
            if (bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())) {
                throw new IllegalArgumentException("Check-in date must come before check-out date.");
            }

            // Tìm kiếm thông tin phòng
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new OurException("Room not found with roomID: " + roomId));
            logger.debug("Room found: {}", room);

            // Tìm kiếm thông tin người dùng
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new OurException("User not found with userID: " + userId));
            logger.debug("User found: {}", user);

            // Kiểm tra tình trạng phòng
            List<Booking> existingBookings = room.getBookings();
            if (!roomIsAvailable(bookingRequest, existingBookings)) {
                throw new OurException("Room not available for the selected date range!");
            }

            // Thiết lập thông tin đặt phòng
            bookingRequest.setRoom(room);
            bookingRequest.setUser(user);

            // Sinh mã xác nhận
            String bookingConfirmationCode = Utils.generateRandomConfirmationCode(10);
            bookingRequest.setBookingConfirmationCode(bookingConfirmationCode);

            // Lưu thông tin đặt phòng vào cơ sở dữ liệu
            bookingRepository.save(bookingRequest);
            logger.info("Booking saved successfully with confirmation code: {}", bookingConfirmationCode);

            // Trả về phản hồi thành công
            response.setStatusCode(200);
            response.setBookingConfirmationCode(bookingConfirmationCode);

        } catch (OurException e) {
            logger.error("OurException occurred: {}", e.getMessage());
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error occurred: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Error with save Booking: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response findBookingByConfirmationCode(String confirmationCode) {
        logger.info("findBookingByConfirmationCode called with confirmationCode: {}", confirmationCode);

        Response response = new Response();
        try {
            Booking booking = bookingRepository.findByBookingConfirmationCode(confirmationCode)
                    .orElseThrow(() -> new OurException("Booking not found"));
            logger.debug("Booking found: {}", booking);

            BookingDTO bookingDTO = Utils.mapBookingEntityToBookingDTOPlusBookedRooms(booking);
            response.setStatusCode(200);
            response.setBooking(bookingDTO);

        } catch (OurException e) {
            logger.error("OurException occurred: {}", e.getMessage());
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error occurred: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Error with find booking by confirmation code: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getAllBookings() {
        logger.info("getAllBookings called");

        Response response = new Response();
        try {
            List<Booking> bookingList = bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            List<BookingDTO> bookingDTOList = Utils.mapBookingListEntityToBookingListDTO(bookingList);

            response.setStatusCode(200);
            response.setBookingList(bookingDTOList);
            logger.debug("Booking list retrieved successfully. Total bookings: {}", bookingDTOList.size());
        } catch (Exception e) {
            logger.error("Unexpected error occurred: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Error with get all bookings: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response cancelBooking(Long bookingId) {
        logger.info("cancelBooking called with bookingId: {}", bookingId);

        Response response = new Response();
        try {
            bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new OurException("Booking does not exist!"));
            bookingRepository.deleteById(bookingId);

            response.setStatusCode(200);
            response.setMessage("Booking deleted successfully");
            logger.info("Booking with ID {} deleted successfully", bookingId);

        } catch (OurException e) {
            logger.error("OurException occurred: {}", e.getMessage());
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error occurred: {}", e.getMessage(), e);
            response.setStatusCode(500);
            response.setMessage("Error with delete booking: " + e.getMessage());
        }
        return response;
    }

    private boolean roomIsAvailable(Booking bookingRequest, List<Booking> existingBookings) {
        logger.info("roomIsAvailable called with bookingRequest: {}", bookingRequest);

        boolean isAvailable = existingBookings.stream()
                .noneMatch(existingBooking ->
                        bookingRequest.getCheckInDate().equals(existingBooking.getCheckInDate()) ||
                                bookingRequest.getCheckOutDate().isBefore(existingBooking.getCheckOutDate()) ||
                                (bookingRequest.getCheckInDate().isAfter(existingBooking.getCheckInDate())
                                        && bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckOutDate())) ||
                                (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate())
                                        && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckOutDate())) ||
                                (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate())
                                        && bookingRequest.getCheckOutDate().isAfter(existingBooking.getCheckOutDate())) ||
                                (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
                                        && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckInDate())) ||
                                (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
                                        && bookingRequest.getCheckOutDate().equals(bookingRequest.getCheckInDate()))
                );

        logger.debug("Room availability for bookingRequest {}: {}", bookingRequest, isAvailable);
        return isAvailable;
    }
}

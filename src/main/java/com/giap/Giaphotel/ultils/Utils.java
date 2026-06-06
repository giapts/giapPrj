package com.giap.Giaphotel.ultils;

import com.giap.Giaphotel.dto.BookingDTO;
import com.giap.Giaphotel.dto.RoomDTO;
import com.giap.Giaphotel.dto.UserDTO;
import com.giap.Giaphotel.entity.Booking;
import com.giap.Giaphotel.entity.Room;
import com.giap.Giaphotel.entity.User;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    // Chuỗi ký tự alphanumeric dùng để tạo mã xác nhận
    private static final String ALPHANUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    // SecureRandom dùng để tạo số ngẫu nhiên an toàn
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Tạo một mã xác nhận ngẫu nhiên với độ dài được chỉ định.
     * @param length Độ dài của mã xác nhận.
     * @return Chuỗi mã xác nhận ngẫu nhiên.
     */
    public static String generateRandomConfirmationCode(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(ALPHANUMERIC_STRING.length());
            char randomChar = ALPHANUMERIC_STRING.charAt(randomIndex);
            stringBuilder.append(randomChar);
        }
        return stringBuilder.toString();
    }

    /**
     * Chuyển đổi một đối tượng `User` sang `UserDTO`.
     * @param user Đối tượng `User`.
     * @return Đối tượng `UserDTO`.
     */
    public static UserDTO mapUserEntityToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setRole(user.getRole());
        return userDTO;
    }

    /**
     * Chuyển đổi một đối tượng `Room` sang `RoomDTO`.
     * @param room Đối tượng `Room`.
     * @return Đối tượng `RoomDTO`.
     */
    public static RoomDTO mapRoomEntityToRoomDTO(Room room) {
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setId(room.getId());
        roomDTO.setRoomType(room.getRoomType());
        roomDTO.setRoomPrice(room.getRoomPrice());
        roomDTO.setRoomPhotoUrl(room.getRoomPhotoUrl());
        roomDTO.setRoomDescription(room.getRoomDescription());
        return roomDTO;
    }

    /**
     * Chuyển đổi một đối tượng `Booking` sang `BookingDTO`.
     * @param booking Đối tượng `Booking`.
     * @return Đối tượng `BookingDTO`.
     */
    public static BookingDTO mapBookingEntityToBookingDTO(Booking booking) {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setId(booking.getId());
        bookingDTO.setCheckInDate(booking.getCheckInDate());
        bookingDTO.setCheckOutDate(booking.getCheckOutDate());
        bookingDTO.setNumOfAdults(booking.getNumOfAdults());
        bookingDTO.setNumOfChildren(booking.getNumOfChildren());
        bookingDTO.setTotalOfGuest(booking.getTotalOfGuest());
        bookingDTO.setBookingConfirmationCode(booking.getBookingConfirmationCode());
        return bookingDTO;
    }

    /**
     * Chuyển đổi một đối tượng `Room` sang `RoomDTO` bao gồm thông tin các lượt đặt phòng.
     * @param room Đối tượng `Room`.
     * @return Đối tượng `RoomDTO`.
     */
    public static RoomDTO mapRoomEntityToRoomDTOPlusBookings(Room room) {
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setId(room.getId());
        roomDTO.setRoomType(room.getRoomType());
        roomDTO.setRoomPrice(room.getRoomPrice());
        roomDTO.setRoomPhotoUrl(room.getRoomPhotoUrl());
        roomDTO.setRoomDescription(room.getRoomDescription());

        // hien thi danh sách các lượt đặt phòng của phòng này
        if (room.getBookings() != null) {
            roomDTO.setBookings(
                    room.getBookings().stream()
                            .map(Utils::mapBookingEntityToBookingDTO)
                            .collect(Collectors.toList())
            );
        }
        return roomDTO;
    }

    /**
     * Chuyển đổi một đối tượng `Booking` sang `BookingDTO` kèm theo thông tin phòng và người dùng (tuỳ chọn).
     * @param booking Đối tượng `Booking`.
     *
     * @return Đối tượng `BookingDTO`.
     */
    public static BookingDTO mapBookingEntityToBookingDTOPlusBookedRooms(Booking booking) {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setId(booking.getId());
        bookingDTO.setCheckInDate(booking.getCheckInDate());
        bookingDTO.setCheckOutDate(booking.getCheckOutDate());
        bookingDTO.setNumOfAdults(booking.getNumOfAdults());
        bookingDTO.setNumOfChildren(booking.getNumOfChildren());
        bookingDTO.setTotalOfGuest(booking.getTotalOfGuest());
        bookingDTO.setBookingConfirmationCode(booking.getBookingConfirmationCode());


        bookingDTO.setUserDTO(Utils.mapUserEntityToUserDTO(booking.getUser()));


        // hien thi thông tin phòng đã đặt
        if (booking.getRoom() != null) {
            bookingDTO.setRoomDTO(mapRoomEntityToRoomDTO(booking.getRoom()));
        }
        return bookingDTO;
    }

    /**
     * Chuyển đổi một đối tượng `User` sang `UserDTO` kèm theo danh sách các lượt đặt phòng.
     * @param user Đối tượng `User`.
     * @return Đối tượng `UserDTO`.
     */
    public static UserDTO mapUserEntityToUserDTOPlusUserBookingsAndRoom(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setRole(user.getRole());

        // Hien thi danh sách các lượt đặt phòng của người dùng
        if (!user.getBookings().isEmpty()) {
            userDTO.setBookings(
                    user.getBookings().stream()
                            .map(Utils::mapBookingEntityToBookingDTOPlusBookedRooms)
                            .collect(Collectors.toList())
            );
        }
        return userDTO;
    }

    /**
     * Chuyển đổi danh sách `User` sang danh sách `UserDTO`.
     * @param userList Danh sách đối tượng `User`.
     * @return Danh sách đối tượng `UserDTO`.
     */
    public static List<UserDTO> mapUserListEntityToUserListDTO(List<User> userList) {
        return userList.stream()
                .map(Utils::mapUserEntityToUserDTO)
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi danh sách `Room` sang danh sách `RoomDTO`.
     * @param roomList Danh sách đối tượng `Room`.
     * @return Danh sách đối tượng `RoomDTO`.
     */
    public static List<RoomDTO> mapRoomListEntityToRoomListDTO(List<Room> roomList) {
        return roomList.stream()
                .map(Utils::mapRoomEntityToRoomDTO)
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi danh sách `Booking` sang danh sách `BookingDTO`.
     * @param bookingList Danh sách đối tượng `Booking`.
     * @return Danh sách đối tượng `BookingDTO`.
     */
    public static List<BookingDTO> mapBookingListEntityToBookingListDTO(List<Booking> bookingList) {
        return bookingList.stream()
                .map(Utils::mapBookingEntityToBookingDTO)
                .collect(Collectors.toList());
    }
}

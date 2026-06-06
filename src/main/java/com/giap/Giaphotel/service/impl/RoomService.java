package com.giap.Giaphotel.service.impl;

import com.giap.Giaphotel.dto.Response;
import com.giap.Giaphotel.dto.RoomDTO;
import com.giap.Giaphotel.entity.Room;
import com.giap.Giaphotel.exception.OurException;
import com.giap.Giaphotel.repo.BookingRepository;
import com.giap.Giaphotel.repo.RoomRepository;
import com.giap.Giaphotel.service.AwsS3Service;
import com.giap.Giaphotel.service.interfac.IRoomService;
import com.giap.Giaphotel.ultils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class RoomService implements IRoomService {

    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AwsS3Service awsS3Service;

    @Override
    public Response addNewRoom(MultipartFile photo, String roomType, BigDecimal roomPrice, String description) {
        logger.info("Attempting to add a new room with type: {}, price: {}", roomType, roomPrice);
        Response response = new Response();
        try {
            // Chuyển đổi ảnh thành đường link
            String imageUrl = awsS3Service.saveImageToS3(photo);

            // Điền thông tin vào Room entity
            Room room = new Room();
            room.setRoomPhotoUrl(imageUrl);
            room.setRoomType(roomType);
            room.setRoomPrice(roomPrice);
            room.setRoomDescription(description);

            // Lưu vào cơ sở dữ liệu
            Room saveRoom = roomRepository.save(room);

            // Chuyển đổi entity sang DTO
            RoomDTO roomDTO = Utils.mapRoomEntityToRoomDTO(saveRoom);

            // Phản hồi thành công
            response.setStatusCode(200);
            response.setMessage("Successfully added new room");
            response.setRoom(roomDTO);

            logger.info("Successfully added room with ID: {}", saveRoom.getId());
        } catch (Exception e) {
            logger.error("Error saving a room: {}", e.getMessage());
            response.setStatusCode(500);
            response.setMessage("Error saving a room: " + e.getMessage());
        }
        return response;
    }

    @Override
    public List<String> getAllRoomType() {
        logger.info("Fetching all distinct room types");
        return roomRepository.findDistinctRoomTypes();
    }

    @Override
    public Response getAllRoom() {
        logger.info("Fetching all rooms from database");
        Response response = new Response();
        try {
            // Lấy danh sách phòng, sắp xếp theo ID giảm dần
            List<Room> roomList = roomRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

            // Chuyển đổi danh sách sang DTO
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomListDTO(roomList);

            response.setStatusCode(200);
            response.setMessage("Successfully fetched all rooms");
            response.setRoomList(roomDTOList);

            logger.info("Fetched {} rooms successfully", roomList.size());
        } catch (Exception e) {
            logger.error("Error fetching all rooms: {}", e.getMessage());
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    @Override
    public Response deleteRoom(Long roomID) {
        logger.info("Attempting to delete room with ID: {}", roomID);
        Response response = new Response();
        try {
            // Kiểm tra xem phòng có tồn tại không
            roomRepository.findById(roomID).orElseThrow(() -> new OurException("Room not found"));

            // Xóa phòng
            roomRepository.deleteById(roomID);

            response.setStatusCode(200);
            response.setMessage("Successfully deleted room");

            logger.info("Successfully deleted room with ID: {}", roomID);
        } catch (OurException e) {
            logger.warn("Room not found with ID: {}", roomID);
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting room: {}", e.getMessage());
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    @Override
    public Response updateRoom(Long roomId, String description, String roomType, BigDecimal roomPrice, MultipartFile photo) {
        logger.info("Attempting to update room with ID: {}", roomId);
        Response response = new Response();
        try {
            // Tìm phòng theo ID
            Room room = roomRepository.findById(roomId).orElseThrow(() -> new OurException("Room not found with ID"));

            // Cập nhật thông tin
            String imageUrl = null;
            if (photo != null && !photo.isEmpty()) {
                imageUrl = awsS3Service.saveImageToS3(photo);
            }
            if (roomType != null) room.setRoomType(roomType);
            if (roomPrice != null) room.setRoomPrice(roomPrice);
            if (description != null) room.setRoomDescription(description);
            if (imageUrl != null) room.setRoomPhotoUrl(imageUrl);

            // Lưu phòng đã cập nhật
            Room saveRoom = roomRepository.save(room);

            // Chuyển đổi sang DTO
            RoomDTO roomDTO = Utils.mapRoomEntityToRoomDTO(saveRoom);

            response.setStatusCode(200);
            response.setMessage("Successfully updated room");
            response.setRoom(roomDTO);

            logger.info("Successfully updated room with ID: {}", roomId);
        } catch (OurException e) {
            logger.warn("Room not found with ID: {}", roomId);
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating room: {}", e.getMessage());
            response.setStatusCode(500);
            response.setMessage("Error updating room: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getRoomById(Long roomId) {
        logger.info("Fetching room with ID: {}", roomId);
        Response response = new Response();
        try {
            Room room = roomRepository.findById(roomId).orElseThrow(() -> new OurException("Room not found with ID"));
            RoomDTO roomDTO = Utils.mapRoomEntityToRoomDTOPlusBookings(room);

            response.setStatusCode(200);
            response.setMessage("Successfully fetched room");
            response.setRoom(roomDTO);

            logger.info("Successfully fetched room with ID: {}", roomId);
        } catch (Exception e) {
            logger.error("Error fetching room with ID: {}", e.getMessage());
            response.setStatusCode(500);
            response.setMessage("Error fetching room: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getAvailableRoomsByDateAndType(LocalDate checkInDate, LocalDate checkOutDate, String roomType) {
        logger.info("Fetching available rooms for type: {}, between {} and {}", roomType, checkInDate, checkOutDate);
        Response response = new Response();
        try {
            List<Room> roomList = roomRepository.findAvailbleRoomsByDateAndTypes(checkInDate, checkOutDate, roomType);
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomListDTO(roomList);

            response.setStatusCode(200);
            response.setMessage("Successfully fetched available rooms");
            response.setRoomList(roomDTOList);

            logger.info("Found {} available rooms", roomList.size());
        } catch (Exception e) {
            logger.error("Error fetching available rooms: {}", e.getMessage());
            response.setStatusCode(500);
            response.setMessage("Error fetching available rooms: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getAllAvailableRooms() {
        logger.info("Fetching all available rooms");
        Response response = new Response();
        try {
            List<Room> roomList = roomRepository.getAllAvailbleRoom();
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomListDTO(roomList);

            response.setStatusCode(200);
            response.setMessage("Successfully fetched all available rooms");
            response.setRoomList(roomDTOList);

            logger.info("Found {} available rooms", roomList.size());
        } catch (Exception e) {
            logger.error("Error fetching all available rooms: {}", e.getMessage());
            response.setStatusCode(500);
            response.setMessage("Error fetching all available rooms: " + e.getMessage());
        }
        return response;
    }
}

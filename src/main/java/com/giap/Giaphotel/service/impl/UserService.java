package com.giap.Giaphotel.service.impl;

import com.giap.Giaphotel.dto.LoginRequest;
import com.giap.Giaphotel.dto.Response;
import com.giap.Giaphotel.dto.UserDTO;
import com.giap.Giaphotel.entity.User;
import com.giap.Giaphotel.exception.OurException;
import com.giap.Giaphotel.repo.UserRepository;
import com.giap.Giaphotel.service.interfac.IUserService;
import com.giap.Giaphotel.ultils.JWTUtils;
import com.giap.Giaphotel.ultils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class UserService implements IUserService {

    // Logger để ghi log thông tin
    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public Response register(User user) {
        LOGGER.info("Bắt đầu xử lý đăng ký người dùng.");
        Response response = new Response();
        try {
            // Kiểm tra vai trò (nếu không có thì gán mặc định là "USER")
            if (user.getRole() == null || user.getRole().isBlank()) {
                user.setRole("USER");
            }

            // Kiểm tra xem email đã tồn tại chưa
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new OurException(user.getEmail() + " đã tồn tại.");
            }

            // Mã hóa mật khẩu
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Lưu người dùng vào cơ sở dữ liệu
            User savedUser = userRepository.save(user);

            // Chuyển đổi sang DTO
            UserDTO userDTO = Utils.mapUserEntityToUserDTO(savedUser);

            // Phản hồi thành công
            response.setStatusCode(200);
            response.setUser(userDTO);
            LOGGER.info("Đăng ký người dùng thành công.");

        } catch (OurException e) {
            LOGGER.warning("Lỗi xảy ra khi đăng ký người dùng: " + e.getMessage());
            response.setStatusCode(409);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Lỗi hệ thống khi đăng ký người dùng: " + e.getMessage());
            response.setStatusCode(500);
            response.setMessage("Đã xảy ra lỗi khi đăng ký người dùng: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response login(LoginRequest loginRequest) {
        LOGGER.info("Bắt đầu xử lý đăng nhập.");
        Response response = new Response();
        try {
            // Xác thực người dùng
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            // Lấy thông tin người dùng từ cơ sở dữ liệu
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new OurException("Người dùng không tìm thấy"));

            // Tạo token JWT
            String token = jwtUtils.generateToken(user);

            // Phản hồi thành công
            response.setStatusCode(200);
            response.setToken(token);
            response.setRole(user.getRole());
            response.setExpirationTime("7 ngày");
            response.setMessage("Đăng nhập thành công.");
            LOGGER.info("Đăng nhập thành công cho email: " + loginRequest.getEmail());

        } catch (OurException e) {
            LOGGER.warning("Lỗi xác thực khi đăng nhập: " + e.getMessage());
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Lỗi hệ thống khi đăng nhập: " + e.getMessage());
            response.setStatusCode(500);
            response.setMessage("Đã xảy ra lỗi khi đăng nhập: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getAllUser() {
        LOGGER.info("Bắt đầu xử lý lấy danh sách người dùng.");
        Response response = new Response();
        try {
            // Lấy danh sách người dùng từ cơ sở dữ liệu
            List<User> userList = userRepository.findAll();

            // Chuyển đổi danh sách sang DTO
            List<UserDTO> userDTOList = Utils.mapUserListEntityToUserListDTO(userList);

            // Phản hồi thành công
            response.setStatusCode(200);
            response.setMessage("Thành công.");
            response.setUserList(userDTOList);
            LOGGER.info("Lấy danh sách người dùng thành công.");

        } catch (Exception e) {
            LOGGER.severe("Lỗi hệ thống khi lấy danh sách người dùng: " + e.getMessage());
            response.setStatusCode(500);
            response.setMessage("Lỗi khi lấy danh sách người dùng: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getUserBookingHistory(String userId) {
        LOGGER.info("Bắt đầu xử lý lấy lịch sử đặt phòng của người dùng với ID: " + userId);
        Response response = new Response();
        try {
            // Tìm người dùng theo ID
            User user = userRepository.findById(Long.valueOf(userId))
                    .orElseThrow(() -> new OurException("Người dùng không tìm thấy"));

            // Chuyển đổi người dùng sang DTO
            UserDTO userDTO = Utils.mapUserEntityToUserDTOPlusUserBookingsAndRoom(user);

            // Phản hồi thành công
            response.setStatusCode(200);
            response.setMessage("Thành công.");
            response.setUser(userDTO);
            LOGGER.info("Lấy lịch sử đặt phòng thành công cho ID: " + userId);

        } catch (OurException e) {
            LOGGER.warning("Không tìm thấy người dùng: " + e.getMessage());
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Lỗi hệ thống khi lấy lịch sử đặt phòng: " + e.getMessage());
            response.setStatusCode(500);
            response.setMessage("Lỗi khi lấy lịch sử đặt phòng: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response deleteUser(String userId) {
        LOGGER.info("Bắt đầu xử lý xóa người dùng với ID: " + userId);
        Response response = new Response();
        try {
            // Kiểm tra xem người dùng có tồn tại không
            userRepository.findById(Long.valueOf(userId))
                    .orElseThrow(() -> new OurException("Người dùng không tìm thấy"));

            // Xóa người dùng
            userRepository.deleteById(Long.valueOf(userId));

            // Phản hồi thành công
            response.setStatusCode(200);
            response.setMessage("Xóa thành công.");
            LOGGER.info("Xóa người dùng thành công với ID: " + userId);

        } catch (OurException e) {
            LOGGER.warning("Không tìm thấy người dùng cần xóa: " + e.getMessage());
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Lỗi hệ thống khi xóa người dùng: " + e.getMessage());
            response.setStatusCode(500);
            response.setMessage("Lỗi khi xóa người dùng: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getUserById(String userId) {
        LOGGER.info("Bắt đầu xử lý lấy thông tin người dùng với ID: " + userId);
        Response response = new Response();
        try {
            // Lấy người dùng theo ID
            User user = userRepository.findById(Long.valueOf(userId))
                    .orElseThrow(() -> new OurException("Người dùng không tìm thấy"));

            // Chuyển đổi sang DTO
            UserDTO userDTO = Utils.mapUserEntityToUserDTO(user);

            // Phản hồi thành công
            response.setStatusCode(200);
            response.setMessage("Thành công.");
            response.setUser(userDTO);
            LOGGER.info("Lấy thông tin người dùng thành công với ID: " + userId);

        } catch (OurException e) {
            LOGGER.warning("Không tìm thấy người dùng: " + e.getMessage());
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Lỗi hệ thống khi lấy thông tin người dùng: " + e.getMessage());
            response.setStatusCode(500);
            response.setMessage("Lỗi khi lấy thông tin người dùng: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getMyInfo(String email) {
        LOGGER.info("Bắt đầu xử lý lấy thông tin cá nhân của email: " + email);
        Response response = new Response();
        try {
            // Lấy người dùng theo email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new OurException("Người dùng không tìm thấy"));

            // Chuyển đổi sang DTO
            UserDTO userDTO = Utils.mapUserEntityToUserDTO(user);

            // Phản hồi thành công
            response.setStatusCode(200);
            response.setMessage("Thành công.");
            response.setUser(userDTO);
            LOGGER.info("Lấy thông tin cá nhân thành công cho email: " + email);

        } catch (OurException e) {
            LOGGER.warning("Không tìm thấy người dùng: " + e.getMessage());
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Lỗi hệ thống khi lấy thông tin cá nhân: " + e.getMessage());
            response.setStatusCode(500);
            response.setMessage("Lỗi khi lấy thông tin cá nhân: " + e.getMessage());
        }
        return response;
    }
}

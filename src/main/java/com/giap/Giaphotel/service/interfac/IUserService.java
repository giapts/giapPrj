package com.giap.Giaphotel.service.interfac;

import com.giap.Giaphotel.dto.LoginRequest;
import com.giap.Giaphotel.dto.Response;
import com.giap.Giaphotel.entity.User;

public interface IUserService {
    Response register(User user);
    Response login(LoginRequest loginRequest);
    Response getAllUser();
    Response getUserBookingHistory(String userId);
    Response deleteUser(String userId);
    Response getUserById(String userId);
    Response getMyInfo(String email);
}

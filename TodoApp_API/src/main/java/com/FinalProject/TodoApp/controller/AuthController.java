package com.FinalProject.TodoApp.controller;

import com.FinalProject.TodoApp.Email;
import com.FinalProject.TodoApp.dto.request.*;
import com.FinalProject.TodoApp.entity.User;
import com.FinalProject.TodoApp.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IUserService userService;
    private final Email email;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    // Lưu tạm thông tin người dùng + OTP trước khi xác minh
    private final Map<String, TempUserData> tempUserStore = new ConcurrentHashMap<>();

    // Đăng ký - chỉ lưu user tạm & gửi OTP
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody @Valid UserRegisterRequestDTO userReq, BindingResult result) {
        Map<String, String> response = new HashMap<>();

        if (result.hasErrors()) {
            response.put("message", "Invalid input");
            return ResponseEntity.badRequest().body(response);
        }

        // Kiểm tra nếu username đã tồn tại trong database
        if (userService.findByUsername(userReq.getUsername()).isPresent()) {
            response.put("message", "Username đã tồn tại trong hệ thống");
            return ResponseEntity.badRequest().body(response);
        }

        // Kiểm tra nếu email đã tồn tại trong database
        if (userService.findByEmail(userReq.getEmail()).isPresent()) {
            response.put("message", "Email đã tồn tại trong hệ thống");
            return ResponseEntity.badRequest().body(response);
        }

        // Kiểm tra nếu email đã được gửi OTP trong tempUserStore
        if (tempUserStore.containsKey(userReq.getEmail())) {
            // Xóa dữ liệu tạm cũ nếu đã tồn tại
            tempUserStore.remove(userReq.getEmail());
        }

        // Tạo người dùng tạm mới và gửi OTP
        User tempUser = modelMapper.map(userReq, User.class);
        tempUser.setPassword(passwordEncoder.encode(userReq.getPassword()));
        tempUser.setActive(false);

        String otpCode = email.getRandom();
        tempUserStore.put(userReq.getEmail(), new TempUserData(tempUser, otpCode));

        // Gửi OTP tới email
        email.sendEmail(userReq.getEmail(), otpCode);

        response.put("message", "OTP đã được gửi đến email. Vui lòng xác minh để hoàn tất đăng ký.");
        return ResponseEntity.ok(response);
    }

    // Xác minh OTP - nếu đúng thì lưu user vào DB
    @PostMapping("/verify-code")
    @Transactional
    public ResponseEntity<Map<String, String>> verifyCode(@RequestBody OTPRequestDTO payload) {
        Map<String, String> response = new HashMap<>();
        TempUserData temp = tempUserStore.get(payload.getEmail());

        if (temp == null) {
            response.put("message", "Không tìm thấy tài khoản tạm hoặc OTP đã hết hạn");
            return ResponseEntity.badRequest().body(response);
        }

        if (!payload.getOtp().equals(temp.getOtp())) {
            response.put("message", "OTP không đúng");
            return ResponseEntity.badRequest().body(response);
        }

        User verifiedUser = temp.getUser();
        verifiedUser.setActive(true);
        userService.saveUser(verifiedUser);

        tempUserStore.remove(payload.getEmail());

        response.put("message","success");
        return ResponseEntity.ok(response);
    }
    // Quên mật khẩu - gửi OTP
    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody @Valid EmailRequestDTO payload) {
        Map<String, String> response = new HashMap<>();
        User user = userService.findByEmail(payload.getEmail()).orElse(null);

        // Kiểm tra nếu email không tồn tại hoặc chưa được xác minh
        if (user == null || !user.isActive()) {
            response.put("message", "Email không tồn tại hoặc chưa được xác minh");
            return ResponseEntity.badRequest().body(response);
        }

        // Tạo OTP mới và lưu vào bộ nhớ tạm
        String otp = email.getRandom();
        TempUserData tempUserData = new TempUserData(user, otp);

        // Lưu thông tin tạm vào tempUserStore
        tempUserStore.put(payload.getEmail(), tempUserData);

        // Gửi OTP qua email
        email.sendEmail(payload.getEmail(), otp);

        response.put("message", "OTP đã được gửi đến email.");
        return ResponseEntity.ok(response);
    }

    // Xác minh OTP để reset mật khẩu
    @PostMapping("/forgot-password/verify-code")
    public ResponseEntity<Map<String, String>> verifyForgotPasswordCode(@RequestBody OTPRequestDTO payload) {
        Map<String, String> response = new HashMap<>();
        // Lấy thông tin tạm từ tempUserStore
        TempUserData tempUserData = tempUserStore.get(payload.getEmail());

        // Kiểm tra nếu không tìm thấy thông tin hoặc OTP hết hạn
        if (tempUserData == null) {
            response.put("message", "Không tìm thấy tài khoản tạm hoặc OTP đã hết hạn");
            return ResponseEntity.badRequest().body(response);
        }

        // Kiểm tra OTP nhập vào có chính xác không
        if (!payload.getOtp().equals(tempUserData.getOtp())) {
            response.put("message", "OTP không chính xác");
            return ResponseEntity.badRequest().body(response);
        }

        // Nếu OTP chính xác, trả về thông báo rằng có thể đặt mật khẩu mới
        response.put("message", "OTP hợp lệ. Vui lòng nhập mật khẩu mới.");
        return ResponseEntity.ok(response);
    }

    // Đặt lại mật khẩu (Sau khi xác minh OTP)
    @PostMapping("/reset-password")

    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordDTO payload) {
        Map<String, String> response = new HashMap<>();

        // Tìm người dùng qua email trong cơ sở dữ liệu
        User user = userService.findByEmail(payload.getEmail()).orElse(null);  // Use orElse(null)

        // Kiểm tra xem người dùng có tồn tại không
        if (user == null) {
            response.put("message", "Tài khoản không tồn tại.");
            return ResponseEntity.badRequest().body(response);
        }

        // Sử dụng ModelMapper để ánh xạ dữ liệu từ DTO sang Entity
        modelMapper.map(payload, user);  // Map thông tin từ payload vào user

        // Cập nhật mật khẩu mới cho người dùng
        user.setPassword(passwordEncoder.encode(payload.getPassword()));
        userService.saveUser(user);

        response.put("message", "password_reset_success");
        return ResponseEntity.ok(response);
    }

}

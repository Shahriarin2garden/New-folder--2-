package NTSA.Mukti_app.controller;

import NTSA.Mukti_app.service.ForgotPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ForgotPasswordController {

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    @GetMapping("/forgot")
    public String forgotPage() {
        return "forgot";
    }

    @PostMapping("/forgot/send-otp")
    @ResponseBody
    public Map<String, String> sendOtp(@RequestParam String email) {
        String result = forgotPasswordService.sendOtp(email);
        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        response.put("status", result.contains("successfully") ? "success" : "error");
        return response;
    }

    @PostMapping("/forgot/verify-otp")
    @ResponseBody
    public Map<String, String> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        // verify only, so password is null
        String result = forgotPasswordService.resetPassword(email, otp, null);
        Map<String, String> response = new HashMap<>();
        if (result.equals("OTP Valid") || result.equals("Password Reset Successful")) {
            response.put("status", "success");
            response.put("message", "OTP Verified Successfully");
        } else {
            response.put("status", "error");
            response.put("message", result);
        }
        return response;
    }
}

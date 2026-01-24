package NTSA.Mukti_app.service;


import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class OtpService {

    public String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    public void sendOtpAsync(String email, String otp) {
        new Thread(() -> {
            System.out.println("OTP sent to " + email + " OTP: " + otp);
        }).start();
    }
}


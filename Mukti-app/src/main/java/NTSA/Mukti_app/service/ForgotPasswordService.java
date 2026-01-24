package NTSA.Mukti_app.service;

import NTSA.Mukti_app.model.OtpToken;
import NTSA.Mukti_app.model.User;
import NTSA.Mukti_app.repository.OtpRepository;
import NTSA.Mukti_app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class ForgotPasswordService {

    private final UserRepository userRepo;
    private final OtpRepository otpRepo;

    public ForgotPasswordService(UserRepository userRepo, OtpRepository otpRepo) {
        this.userRepo = userRepo;
        this.otpRepo = otpRepo;
    }

    @Transactional
    public String sendOtp(String email) {
        // ১. চেক করা ইউজার আছে কিনা
        User user = userRepo.findByEmail(email).orElse(null);
        if (user == null) return "User not found";

        // ২. আগের কোনো OTP থেকে থাকলে তা ডিলিট করা
        otpRepo.findByEmail(email).ifPresent(otpRepo::delete);

        // ৩. নতুন ৬ ডিজিটের OTP জেনারেট করা
        String otp = String.format("%06d", new Random().nextInt(1000000));

        OtpToken token = new OtpToken();
        token.setEmail(email); // ✅ ফোন থেকে ইমেইলে পরিবর্তন
        token.setOtp(otp);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        otpRepo.save(token);
        System.out.println("OTP for " + email + " is: " + otp); // কনসোলে ওটিপি চেক করুন

        return "OTP Sent successfully";
    }

    @Transactional
    public String resetPassword(String email, String otp, String newPassword) {
        // ১. OTP চেক করা
        OtpToken token = otpRepo.findByEmailAndOtp(email, otp).orElse(null);

        if (token == null) return "Invalid OTP";

        // ২. মেয়াদের সময় চেক করা
        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpRepo.delete(token);
            return "OTP Expired";
        }

        // ৩. যদি পাসওয়ার্ড প্যারামিটার দেওয়া থাকে তবে আপডেট করা (ভেরিফিকেশন স্টেপে null থাকবে)
        if (newPassword != null) {
            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setPassword(newPassword);
            userRepo.save(user);
            otpRepo.delete(token); // কাজ শেষ হলে OTP ডিলিট করা
            return "Password Reset Successful";
        }

        return "OTP Valid";
    }
}
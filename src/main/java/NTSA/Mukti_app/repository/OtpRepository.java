package NTSA.Mukti_app.repository;

import NTSA.Mukti_app.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findByEmailAndOtp(String email, String otp); // ✅ ইমেইল অনুযায়ী
    Optional<OtpToken> findByEmail(String email);
}
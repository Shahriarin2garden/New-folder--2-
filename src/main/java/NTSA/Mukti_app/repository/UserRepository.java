package NTSA.Mukti_app.repository;

import NTSA.Mukti_app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);

    Optional<User> findByEmail(String email);

    List<User> findTop10ByOrderByPointsDesc();

    List<User> findAllByOrderByPointsDesc();
}
package NTSA.Mukti_app.repository;

import NTSA.Mukti_app.model.FoodRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FoodRequestRepository extends JpaRepository<FoodRequest, Long> {
    List<FoodRequest> findByRequesterPhone(String requesterPhone);

    List<FoodRequest> findByLocationContainingIgnoreCase(String location);

    List<FoodRequest> findAllByOrderByRequestTimeDesc();
}

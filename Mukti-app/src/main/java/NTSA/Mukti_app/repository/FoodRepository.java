package NTSA.Mukti_app.repository;

import NTSA.Mukti_app.model.FoodPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodRepository extends JpaRepository<FoodPost, Long> {

    List<FoodPost> findByReceived(boolean received);
    
    List<FoodPost> findByDonorPhoneOrderByIdDesc(String donorPhone);
    
    List<FoodPost> findByReceiverPhone(String receiverPhone);

}

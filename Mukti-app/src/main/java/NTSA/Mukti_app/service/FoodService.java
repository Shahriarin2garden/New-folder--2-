package NTSA.Mukti_app.service;

import NTSA.Mukti_app.model.FoodPost;
import NTSA.Mukti_app.repository.FoodRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FoodService {

    private final FoodRepository repo;

    public FoodService(FoodRepository repo) {
        this.repo = repo;
    }

    public FoodPost donate(FoodPost food) {
        food.setReceived(false);
        return repo.save(food);
    }

    public List<FoodPost> allFoods() {
        return repo.findAll();
    }

    public void received(Long id) {
        FoodPost food = repo.findById(id).orElse(null);
        if (food != null) {
            food.setReceived(true);
            repo.save(food);
        }
    }

    // stats
    public int totalDonations() {
        return (int) repo.count();
    }

    public int totalReceived() {
        return repo.findByReceived(true).size();
    }

    public int activePosts() {
        return repo.findByReceived(false).size();
    }
}

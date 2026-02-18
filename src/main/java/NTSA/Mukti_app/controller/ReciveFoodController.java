package NTSA.Mukti_app.controller;

import NTSA.Mukti_app.model.ChatMessage;
import NTSA.Mukti_app.model.FoodPost;
import NTSA.Mukti_app.model.History;
import NTSA.Mukti_app.model.User;
import NTSA.Mukti_app.repository.ChatMessageRepository;
import NTSA.Mukti_app.repository.HistoryRepository;
import NTSA.Mukti_app.service.FoodService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/food")
public class ReciveFoodController {

    @Autowired
    private FoodService service;

    @Autowired
    private HistoryRepository historyRepo;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private NTSA.Mukti_app.service.NotificationService notificationService;

    @GetMapping("/receive-page")
    public String receivePage(Model model) {
        List<FoodPost> allFoods = service.allFoods();
        // Filter out expired food posts
        List<FoodPost> availableFoods = allFoods.stream()
                .filter(food -> food.getExpireTime() == null
                        || food.getExpireTime().isAfter(java.time.LocalDateTime.now()))
                .filter(food -> !food.isReceived())
                .toList();
        model.addAttribute("foods", availableFoods);
        return "receive";
    }

    @PostMapping("/receive/{id}")
    @ResponseBody
    public String receiveFood(@PathVariable Long id, HttpSession session) {
        User receiver = (User) session.getAttribute("user");
        if (receiver == null)
            return "Error: Unauthorized";

        try {
            FoodPost food = service.allFoods().stream()
                    .filter(f -> f.getId().equals(id)).findFirst().orElse(null);

            if (food != null && !food.isReceived()) {
                service.received(id, receiver.getPhone());

                historyRepo.save(new History(receiver.getPhone(), food.getFoodName(), food.getLocation(),
                        "RECEIVER", "Processing", food.getDonorName(), food.getDonorPhone(), id));

                List<History> donorHistories = historyRepo.findByUserPhoneOrderByActivityTimeDesc(food.getDonorPhone());
                for (History h : donorHistories) {
                    if (h.getFoodName().equals(food.getFoodName()) && "DONOR".equals(h.getRole())) {
                        h.setStatus("Processing");
                        h.setOtherPartyName(receiver.getName());
                        h.setOtherPartyPhone(receiver.getPhone());
                        historyRepo.save(h);
                        break;
                    }
                }

                // Create initial chat message
                try {
                    ChatMessage initialMessage = new ChatMessage(
                            id,
                            receiver.getPhone(),
                            receiver.getName(),
                            food.getDonorPhone(),
                            "Hi! I would like to receive the " + food.getFoodName() + ". Can we arrange the pickup?");
                    chatMessageRepository.save(initialMessage);

                    // Notify the donor
                    notificationService.notifyUser(
                            food.getDonorPhone(),
                            "Your food post '" + food.getFoodName() + "' was successfully requested by "
                                    + receiver.getName(),
                            "message");

                } catch (Exception e) {
                    // Log but don't fail the receive operation
                    System.err.println("Failed to create initial chat message: " + e.getMessage());
                }

                return "Success";
            }
            return "Error: Already Taken";
        } catch (Exception e) {
            return "Error";
        }
    }
}
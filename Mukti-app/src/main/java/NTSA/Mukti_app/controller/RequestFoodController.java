package NTSA.Mukti_app.controller;

import NTSA.Mukti_app.model.FoodRequest;
import NTSA.Mukti_app.model.User;
import NTSA.Mukti_app.repository.FoodRequestRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/food")
public class RequestFoodController {

    @Autowired
    private FoodRequestRepository requestRepo;

    @GetMapping("/request")
    public String requestPage(HttpSession session, Model model) {
        if (session.getAttribute("user") == null)
            return "redirect:/login";
        model.addAttribute("request", new FoodRequest());
        model.addAttribute("requests", requestRepo.findAllByOrderByRequestTimeDesc());
        return "request";
    }

    @PostMapping("/request")
    public String submitRequest(@ModelAttribute FoodRequest foodRequest, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            foodRequest.setRequesterName(user.getName());
            foodRequest.setRequesterPhone(user.getPhone());
            foodRequest.setRequestTime(LocalDateTime.now());
            foodRequest.setStatus("Pending");
            requestRepo.save(foodRequest);
        }
        return "redirect:/dashboard?success=Request submitted successfully";
    }
}

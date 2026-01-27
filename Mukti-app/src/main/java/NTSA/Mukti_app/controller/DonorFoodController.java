package NTSA.Mukti_app.controller;

import NTSA.Mukti_app.model.FoodPost;
import NTSA.Mukti_app.model.History;
import NTSA.Mukti_app.model.User;
import NTSA.Mukti_app.repository.HistoryRepository;
import NTSA.Mukti_app.service.FoodService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/food")
public class DonorFoodController {

    @Autowired
    private FoodService service;

    @Autowired
    private HistoryRepository historyRepo;

    @Autowired
    private NTSA.Mukti_app.repository.UserRepository userRepo;

    @Autowired
    private NTSA.Mukti_app.repository.FoodRepository foodRepo;

    @Autowired
    private NTSA.Mukti_app.service.NotificationService notificationService;

    @GetMapping("/donate")
    public String donatePage(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "redirect:/login";

        // Fetch latest version of user to get current address
        User user = userRepo.findByPhone(sessionUser.getPhone()).orElse(sessionUser);
        model.addAttribute("user", user);
        return "donate";
    }

    @PostMapping("/donate")
    public String submitDonation(@ModelAttribute FoodPost foodPost, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            foodPost.setDonorName(user.getName());
            foodPost.setDonorPhone(user.getPhone());
            service.donate(foodPost);
            historyRepo.save(new History(user.getPhone(), foodPost.getFoodName(), foodPost.getLocation(), "DONOR",
                    "Available", null, null, foodPost.getId()));

            // Broadcast donation notification
            String msg = user.getName() + " posted donation: " + foodPost.getFoodName() + " (" + foodPost.getLocation()
                    + ")";
            notificationService.notifyAll(msg, "donation");
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/history")
    public String viewHistory(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null)
            return "redirect:/login";

        List<History> all = historyRepo.findByUserPhoneOrderByActivityTimeDesc(user.getPhone());
        List<History> donations = new ArrayList<>();
        List<History> receives = new ArrayList<>();

        if (all != null) {
            for (History h : all) {
                // Retroactive fix for missing foodPostId
                if (h.getFoodPostId() == null) {
                    List<FoodPost> matchingPosts = foodRepo.findAll().stream()
                            .filter(fp -> fp.getFoodName().equalsIgnoreCase(h.getFoodName()))
                            .filter(fp -> {
                                if ("DONOR".equals(h.getRole())) {
                                    return fp.getDonorPhone().trim().equals(h.getUserPhone().trim());
                                } else {
                                    return fp.getReceiverPhone() != null
                                            && fp.getReceiverPhone().trim().equals(h.getUserPhone().trim());
                                }
                            })
                            .toList();
                    if (!matchingPosts.isEmpty()) {
                        h.setFoodPostId(matchingPosts.get(0).getId());
                        historyRepo.save(h);
                    }
                }

                if ("DONOR".equals(h.getRole()))
                    donations.add(h);
                else
                    receives.add(h);
            }
        }
        model.addAttribute("donations", donations);
        model.addAttribute("receives", receives);
        return "history";
    }

    @PostMapping("/complete/{id}")
    @ResponseBody
    public String complete(@PathVariable Long id) {
        History h = historyRepo.findById(id).orElse(null);
        if (h != null) {
            h.setStatus("Received");
            historyRepo.save(h);
            syncStatus(h.getFoodName(), "Received");

            // Award 10 points to donor
            userRepo.findByPhone(h.getUserPhone()).ifPresent(user -> {
                user.setPoints(user.getPoints() + 10);
                userRepo.save(user);
            });

            return "Success";
        }
        return "Error";
    }

    @GetMapping("/leaderboard")
    public String viewLeaderboard(Model model) {
        model.addAttribute("topDonors", userRepo.findAllByOrderByPointsDesc());
        return "leaderboard";
    }

    @PostMapping("/cancel/{id}")
    @ResponseBody
    public String cancel(@PathVariable Long id) {
        History h = historyRepo.findById(id).orElse(null);
        if (h != null) {
            h.setStatus("Cancelled");
            historyRepo.save(h);
            syncStatus(h.getFoodName(), "Cancelled");
            return "Success";
        }
        return "Error";
    }

    private void syncStatus(String foodName, String status) {
        List<History> all = historyRepo.findAll();
        for (History h : all) {
            if (h.getFoodName().equals(foodName) && "RECEIVER".equals(h.getRole())
                    && "Processing".equals(h.getStatus())) {
                h.setStatus(status);
                historyRepo.save(h);
            }
        }
    }
}
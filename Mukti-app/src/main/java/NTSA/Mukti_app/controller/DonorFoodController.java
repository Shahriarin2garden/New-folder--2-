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

    @Autowired
    private NTSA.Mukti_app.repository.FoodRequestRepository foodRequestRepo;

    @GetMapping("/donate")
    public String donatePage(@RequestParam(required = false) Long requestId, HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "redirect:/login";

        // Fetch latest version of user to get current address
        User user = userRepo.findByPhone(sessionUser.getPhone()).orElse(sessionUser);
        model.addAttribute("user", user);

        // If donating in response to a request, pass it to the view
        if (requestId != null) {
            model.addAttribute("requestId", requestId);
        }

        return "donate";
    }

    @PostMapping("/donate")
    public String submitDonation(@ModelAttribute FoodPost foodPost,
            @RequestParam(required = false) Long requestId,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            foodPost.setDonorName(user.getName());
            foodPost.setDonorPhone(user.getPhone());
            service.donate(foodPost);
            historyRepo.save(new History(user.getPhone(), foodPost.getFoodName(), foodPost.getLocation(), "DONOR",
                    "Available", null, null, foodPost.getId()));

            // If this donation is in response to a request, link them and update status
            if (requestId != null) {
                foodRequestRepo.findById(requestId).ifPresent(request -> {
                    request.setStatus("Processing");
                    request.setDonorPhone(user.getPhone());
                    request.setDonorName(user.getName());
                    request.setFoodPostId(foodPost.getId());
                    foodRequestRepo.save(request);
                });
            }

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

        // Also include food requests made by this user
        List<NTSA.Mukti_app.model.FoodRequest> userRequests = foodRequestRepo.findAll().stream()
                .filter(req -> user.getPhone().equals(req.getRequesterPhone()))
                .sorted((a, b) -> b.getRequestTime().compareTo(a.getRequestTime()))
                .toList();
        model.addAttribute("foodRequests", userRequests);

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

            // If this donation was linked to a food request, mark request as Received
            if (h.getFoodPostId() != null) {
                foodRequestRepo.findAll().stream()
                        .filter(req -> h.getFoodPostId().equals(req.getFoodPostId()))
                        .forEach(req -> {
                            req.setStatus("Received");
                            foodRequestRepo.save(req);
                        });
            }

            return "Success";
        }
        return "Error";
    }

    @PostMapping("/complete-request/{id}")
    @ResponseBody
    public String completeRequest(@PathVariable Long id) {
        History h = historyRepo.findById(id).orElse(null);
        if (h != null && "DONOR".equals(h.getRole())) {
            // Update donor's history to Received
            h.setStatus("Received");
            historyRepo.save(h);

            // Update the food request status
            foodRequestRepo.findById(h.getFoodPostId()).ifPresent(req -> {
                req.setStatus("Received");
                foodRequestRepo.save(req);
            });

            // Update requester's history to Received
            historyRepo.findAll().stream()
                    .filter(history -> history.getFoodPostId() != null
                            && history.getFoodPostId().equals(h.getFoodPostId())
                            && "RECEIVER".equals(history.getRole()))
                    .forEach(history -> {
                        history.setStatus("Received");
                        historyRepo.save(history);
                    });

            // Award 10 points to donor
            userRepo.findByPhone(h.getUserPhone()).ifPresent(user -> {
                user.setPoints(user.getPoints() + 10);
                userRepo.save(user);
            });

            // Notify the requester
            if (h.getOtherPartyPhone() != null) {
                notificationService.notifyUser(
                        h.getOtherPartyPhone(),
                        "Your food request '" + h.getFoodName() + "' has been marked as received by "
                                + h.getOtherPartyName(),
                        "update");
            }

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

    @PostMapping("/cancel-request/{id}")
    @ResponseBody
    public String cancelRequest(@PathVariable Long id) {
        History h = historyRepo.findById(id).orElse(null);
        if (h != null && "DONOR".equals(h.getRole())) {
            // Update donor's history to Cancelled
            h.setStatus("Cancelled");
            historyRepo.save(h);

            // Update the food request status back to Pending
            foodRequestRepo.findById(h.getFoodPostId()).ifPresent(req -> {
                req.setStatus("Pending");
                req.setDonorPhone(null);
                req.setDonorName(null);
                foodRequestRepo.save(req);
            });

            // Update requester's history to Cancelled
            historyRepo.findAll().stream()
                    .filter(history -> history.getFoodPostId() != null
                            && history.getFoodPostId().equals(h.getFoodPostId())
                            && "RECEIVER".equals(history.getRole()))
                    .forEach(history -> {
                        history.setStatus("Cancelled");
                        historyRepo.save(history);
                    });

            // Notify the requester
            if (h.getOtherPartyPhone() != null) {
                notificationService.notifyUser(
                        h.getOtherPartyPhone(),
                        "The donation for your request '" + h.getFoodName() + "' has been cancelled by "
                                + h.getOtherPartyName(),
                        "update");
            }

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
package NTSA.Mukti_app.controller;

import NTSA.Mukti_app.model.User;
import NTSA.Mukti_app.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import NTSA.Mukti_app.service.NotificationService;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private NTSA.Mukti_app.repository.UserRepository userRepo;

    @Autowired
    private AuthService authService;

    @Autowired
    private NTSA.Mukti_app.repository.HistoryRepository historyRepo;

    @Autowired
    private NotificationService notificationService;

    // 1. View Mapping (GET)
    @GetMapping("/")
    public String index() {
        return "index"; // Matches src/main/resources/templates/index.html
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // 2. Dashboard Mapping
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "redirect:/login";

        // Re-fetch to get latest data (city, address, points etc)
        User user = userRepo.findByPhone(sessionUser.getPhone()).orElse(sessionUser);

        // Calculate stats from History
        java.util.List<NTSA.Mukti_app.model.History> userHistory = historyRepo
                .findByUserPhoneOrderByActivityTimeDesc(user.getPhone());

        long totalDonations = userHistory.stream()
                .filter(h -> "DONOR".equals(h.getRole()) && "Received".equals(h.getStatus()))
                .count();
        long totalReceived = userHistory.stream()
                .filter(h -> "RECEIVER".equals(h.getRole()) && "Received".equals(h.getStatus()))
                .count();
        long activePosts = userHistory.stream()
                .filter(h -> "DONOR".equals(h.getRole())
                        && ("Available".equals(h.getStatus()) || "Processing".equals(h.getStatus())))
                .count();

        // Count distinct people helped (otherPartyPhone)
        long peopleHelped = userHistory.stream()
                .filter(h -> "DONOR".equals(h.getRole()) && h.getOtherPartyPhone() != null)
                .map(NTSA.Mukti_app.model.History::getOtherPartyPhone)
                .distinct()
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDonations", totalDonations);
        stats.put("totalReceived", totalReceived);
        stats.put("activePosts", activePosts);
        stats.put("peopleHelped", peopleHelped > 0 ? peopleHelped : 0);

        model.addAttribute("user", user);
        model.addAttribute("stats", stats);
        return "dashboard";
    }

    // 3. Action Mappings
    @PostMapping("/login")
    public String login(@RequestParam String identifier, @RequestParam String password, HttpSession session,
            Model model) {
        User user = authService.login(identifier, password);
        if (user != null) {
            session.setAttribute("user", user);
            // Broadcast login notification
            notificationService.notifyAll(user.getName() + " just logged in!", "login");
            return "redirect:/dashboard"; //
        }
        model.addAttribute("error", "Invalid Credentials");
        return "login";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        try {
            authService.register(user);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed. Please try again.");
            return "register";
        }
    }

    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam String city, @RequestParam String address, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            user.setCity(city);
            user.setAddress(address);
            userRepo.save(user); // Persistence
            session.setAttribute("user", user); // Session update
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/api/user-stats")
    @ResponseBody
    public Map<String, Object> getUserStats(HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return new HashMap<>();

        User user = userRepo.findByPhone(sessionUser.getPhone()).orElse(sessionUser);
        java.util.List<NTSA.Mukti_app.model.History> userHistory = historyRepo
                .findByUserPhoneOrderByActivityTimeDesc(user.getPhone());

        long totalDonations = userHistory.stream()
                .filter(h -> "DONOR".equals(h.getRole()) && "Received".equals(h.getStatus())).count();
        long totalReceived = userHistory.stream()
                .filter(h -> "RECEIVER".equals(h.getRole()) && "Received".equals(h.getStatus())).count();
        long activePosts = userHistory.stream().filter(h -> "DONOR".equals(h.getRole())
                && ("Available".equals(h.getStatus()) || "Processing".equals(h.getStatus()))).count();
        long peopleHelped = userHistory.stream()
                .filter(h -> "DONOR".equals(h.getRole()) && h.getOtherPartyPhone() != null)
                .map(NTSA.Mukti_app.model.History::getOtherPartyPhone).distinct().count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("points", user.getPoints());
        stats.put("totalDonations", totalDonations);
        stats.put("totalReceived", totalReceived);
        stats.put("activePosts", activePosts);
        stats.put("peopleHelped", peopleHelped);

        return stats;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); //
        return "redirect:/login";
    }
}
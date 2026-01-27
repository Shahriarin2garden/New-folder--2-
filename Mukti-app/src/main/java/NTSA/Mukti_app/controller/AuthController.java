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
    private AuthService authService;

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
        User user = (User) session.getAttribute("user");
        if (user == null)
            return "redirect:/login"; //

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDonations", 0);
        stats.put("totalReceived", 0);
        stats.put("activePosts", 0);
        stats.put("peopleHelped", 0);

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

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); //
        return "redirect:/login";
    }
}
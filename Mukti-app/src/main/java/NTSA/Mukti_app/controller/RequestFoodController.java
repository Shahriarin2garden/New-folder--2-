package NTSA.Mukti_app.controller;

import NTSA.Mukti_app.model.FoodRequest;
import NTSA.Mukti_app.model.History;
import NTSA.Mukti_app.model.User;
import NTSA.Mukti_app.repository.FoodRequestRepository;
import NTSA.Mukti_app.repository.HistoryRepository;
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

    @Autowired
    private HistoryRepository historyRepo;

    @Autowired
    private NTSA.Mukti_app.service.NotificationService notificationService;

    @GetMapping("/request")
    public String requestPage(HttpSession session, Model model) {
        if (session.getAttribute("user") == null)
            return "redirect:/login";
        model.addAttribute("request", new FoodRequest());

        // Only show Pending requests in feed (hide Processing/Received/Cancelled)
        java.util.List<FoodRequest> activeRequests = requestRepo.findAllByOrderByRequestTimeDesc().stream()
                .filter(req -> "Pending".equals(req.getStatus()))
                .toList();

        model.addAttribute("requests", activeRequests);
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

            // Broadcast request notification
            String msg = user.getName() + " requested: " + foodRequest.getFoodName() + " (" + foodRequest.getLocation()
                    + ")";
            notificationService.notifyAll(msg, "request");
        }
        return "redirect:/dashboard?success=Request submitted successfully";
    }

    @PostMapping("/respond-to-request/{requestId}")
    @ResponseBody
    public String respondToRequest(@PathVariable Long requestId, HttpSession session) {
        User donor = (User) session.getAttribute("user");
        if (donor == null)
            return "Error: Unauthorized";

        try {
            FoodRequest request = requestRepo.findById(requestId).orElse(null);

            if (request != null && "Pending".equals(request.getStatus())) {
                // Update request status to Processing
                request.setStatus("Processing");
                request.setDonorPhone(donor.getPhone());
                request.setDonorName(donor.getName());
                requestRepo.save(request);

                // Create history entry for Requester (RECEIVER role)
                History requesterHistory = new History(
                        request.getRequesterPhone(),
                        request.getFoodName(),
                        request.getLocation(),
                        "RECEIVER",
                        "Processing",
                        donor.getName(),
                        donor.getPhone(),
                        requestId // Using requestId as foodPostId for tracking
                );
                requesterHistory.setIsRequestDonation(true);
                historyRepo.save(requesterHistory);

                // Create history entry for Donor (DONOR role)
                History donorHistory = new History(
                        donor.getPhone(),
                        request.getFoodName(),
                        request.getLocation(),
                        "DONOR",
                        "Processing",
                        request.getRequesterName(),
                        request.getRequesterPhone(),
                        requestId // Using requestId as foodPostId for tracking
                );
                donorHistory.setIsRequestDonation(true);
                historyRepo.save(donorHistory);

                // Notify the requester
                notificationService.notifyUser(
                        request.getRequesterPhone(),
                        donor.getName() + " is donating to your request: " + request.getFoodName(),
                        "donation");

                return "Success";
            }
            return "Error: Request not available";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}

package NTSA.Mukti_app.controller;

import NTSA.Mukti_app.model.ChatMessage;
import NTSA.Mukti_app.model.FoodPost;
import NTSA.Mukti_app.model.User;
import NTSA.Mukti_app.repository.ChatMessageRepository;
import NTSA.Mukti_app.repository.FoodRepository;
import NTSA.Mukti_app.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NTSA.Mukti_app.repository.FoodRequestRepository foodRequestRepository;

    @Autowired
    private NTSA.Mukti_app.service.NotificationService notificationService;

    // View chat list page
    @GetMapping(value = { "", "/list" })
    public String chatListPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null)
            return "redirect:/login";

        String userPhone = user.getPhone().trim();

        // Get latest messages for all conversations involving this user
        List<ChatMessage> latestMessages = chatMessageRepository.findLatestMessagesByUser(userPhone);

        List<Map<String, Object>> asDonor = new ArrayList<>();
        List<Map<String, Object>> asReceiver = new ArrayList<>();

        for (ChatMessage msg : latestMessages) {
            String foodName;
            String location;
            boolean isDonor;
            boolean isReceiver;
            boolean isPostReceived;

            if (msg.isRequest()) {
                Optional<NTSA.Mukti_app.model.FoodRequest> reqOpt = foodRequestRepository.findById(msg.getFoodPostId());
                if (reqOpt.isEmpty())
                    continue;
                NTSA.Mukti_app.model.FoodRequest req = reqOpt.get();
                foodName = req.getFoodName();
                location = req.getLocation();
                isDonor = req.getDonorPhone() != null && req.getDonorPhone().trim().equals(userPhone);
                isReceiver = req.getRequesterPhone() != null && req.getRequesterPhone().trim().equals(userPhone);
                isPostReceived = "Received".equals(req.getStatus());
            } else {
                Optional<FoodPost> foodPostOpt = foodRepository.findById(msg.getFoodPostId());
                if (foodPostOpt.isEmpty())
                    continue;
                FoodPost post = foodPostOpt.get();
                foodName = post.getFoodName();
                location = post.getLocation();
                isDonor = post.getDonorPhone().trim().equals(userPhone);
                isReceiver = post.getReceiverPhone() != null && post.getReceiverPhone().trim().equals(userPhone);
                isPostReceived = post.isReceived();
            }

            if (!isDonor && !isReceiver)
                continue;

            String otherPhone = msg.getSenderPhone().trim().equals(userPhone)
                    ? msg.getReceiverPhone().trim()
                    : msg.getSenderPhone().trim();

            String otherName = userRepository.findByPhone(otherPhone)
                    .map(User::getName)
                    .orElse(msg.getSenderPhone().trim().equals(userPhone) ? "User" : msg.getSenderName());

            long unreadCount = chatMessageRepository
                    .findConversation(msg.getFoodPostId(), userPhone, otherPhone, msg.isRequest())
                    .stream()
                    .filter(m -> !m.isReadStatus() && m.getReceiverPhone().trim().equals(userPhone))
                    .count();

            Map<String, Object> conv = new HashMap<>();
            conv.put("foodPostId", msg.getFoodPostId());
            conv.put("isRequest", msg.isRequest());
            conv.put("foodName", foodName);
            conv.put("otherUserName", otherName);
            conv.put("otherUserPhone", otherPhone);
            conv.put("lastMessage", msg.getMessage());
            conv.put("lastMessageTime", msg.getTimestamp());
            conv.put("unreadCount", unreadCount);
            conv.put("isReceived", isPostReceived);
            conv.put("location", location);

            if (isDonor) {
                asDonor.add(conv);
            } else {
                asReceiver.add(conv);
            }
        }

        model.addAttribute("donations", asDonor);
        model.addAttribute("receivals", asReceiver);
        model.addAttribute("user", user);
        return "chat-list";
    }

    // View specific chat room
    @GetMapping("/room/{foodPostId}")
    public String chatRoom(@PathVariable Long foodPostId,
            @RequestParam String otherPhone,
            @RequestParam(defaultValue = "false") boolean isRequest,
            HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null)
            return "redirect:/login";

        String foodName;
        boolean isDonor;
        boolean isReceiver;
        String userPhone = user.getPhone().trim();
        String targetPhone = otherPhone.trim();

        if (isRequest) {
            Optional<NTSA.Mukti_app.model.FoodRequest> reqOpt = foodRequestRepository.findById(foodPostId);
            if (reqOpt.isEmpty())
                return "redirect:/chat";
            NTSA.Mukti_app.model.FoodRequest req = reqOpt.get();
            foodName = req.getFoodName();
            isDonor = req.getDonorPhone() != null && req.getDonorPhone().trim().equals(userPhone);
            isReceiver = req.getRequesterPhone() != null && req.getRequesterPhone().trim().equals(userPhone);

            boolean isValidCounterpart = (isDonor && req.getRequesterPhone().trim().equals(targetPhone))
                    || (isReceiver && req.getDonorPhone().trim().equals(targetPhone));
            if (!isValidCounterpart)
                return "redirect:/chat";

            Map<String, Object> postMap = new HashMap<>();
            postMap.put("id", req.getId());
            postMap.put("foodName", req.getFoodName());
            model.addAttribute("foodPost", postMap);
        } else {
            Optional<FoodPost> foodPostOpt = foodRepository.findById(foodPostId);
            if (foodPostOpt.isEmpty())
                return "redirect:/chat";
            FoodPost post = foodPostOpt.get();
            foodName = post.getFoodName();
            isDonor = post.getDonorPhone().trim().equals(userPhone);
            isReceiver = post.getReceiverPhone() != null && post.getReceiverPhone().trim().equals(userPhone);

            boolean isValidCounterpart = (isDonor && post.getReceiverPhone() != null
                    && post.getReceiverPhone().trim().equals(targetPhone))
                    || (isReceiver && post.getDonorPhone().trim().equals(targetPhone));
            if (!isValidCounterpart)
                return "redirect:/chat";

            model.addAttribute("foodPost", post);
        }

        // Get and mark as read
        List<ChatMessage> messages = chatMessageRepository.findConversation(foodPostId, userPhone, targetPhone,
                isRequest);
        chatMessageRepository.markAsRead(foodPostId, userPhone, isRequest);

        String otherName = userRepository.findByPhone(targetPhone)
                .map(User::getName)
                .orElse("Unknown User");

        model.addAttribute("messages", messages);
        model.addAttribute("currentUser", user);
        model.addAttribute("otherUserName", otherName);
        model.addAttribute("otherUserPhone", targetPhone);
        model.addAttribute("myRole", isDonor ? "DONOR" : "RECEIVER");
        model.addAttribute("isRequest", isRequest);

        return "chat-room";
    }

    // Send message via REST API
    @PostMapping("/send")
    @ResponseBody
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> payload, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null)
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        try {
            Long foodPostId = Long.parseLong(payload.get("foodPostId").toString());
            String targetPhone = payload.get("receiverPhone").toString().trim();
            String message = payload.get("message").toString();
            boolean isRequest = payload.containsKey("isRequest") && (boolean) payload.get("isRequest");
            String userPhone = user.getPhone().trim();

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Empty message"));
            }

            if (isRequest) {
                Optional<NTSA.Mukti_app.model.FoodRequest> reqOpt = foodRequestRepository.findById(foodPostId);
                if (reqOpt.isEmpty())
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid request"));
                NTSA.Mukti_app.model.FoodRequest req = reqOpt.get();
                boolean isDonor = req.getDonorPhone() != null && req.getDonorPhone().trim().equals(userPhone);
                boolean isReceiver = req.getRequesterPhone() != null
                        && req.getRequesterPhone().trim().equals(userPhone);
                boolean authorized = (isDonor && req.getRequesterPhone().trim().equals(targetPhone))
                        || (isReceiver && req.getDonorPhone().trim().equals(targetPhone));
                if (!authorized)
                    return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
            } else {
                Optional<FoodPost> postOpt = foodRepository.findById(foodPostId);
                if (postOpt.isEmpty())
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid post"));

                FoodPost post = postOpt.get();
                boolean isDonor = post.getDonorPhone().trim().equals(userPhone);
                boolean isReceiver = post.getReceiverPhone() != null
                        && post.getReceiverPhone().trim().equals(userPhone);

                boolean authorizedReceiver = (isDonor && post.getReceiverPhone() != null
                        && post.getReceiverPhone().trim().equals(targetPhone))
                        || (isReceiver && post.getDonorPhone().trim().equals(targetPhone));

                if (!authorizedReceiver) {
                    return ResponseEntity.status(403)
                            .body(Map.of("error", "You are not authorized to message this user about this post"));
                }
            }

            ChatMessage chatMsg = new ChatMessage(foodPostId, userPhone, user.getName(), targetPhone, message.trim(),
                    isRequest);
            chatMessageRepository.save(chatMsg);

            // Notify Receiver
            notificationService.notifyUser(targetPhone, "New message from " + user.getName(), "message");

            return ResponseEntity.ok(Map.of("success", true, "message", chatMsg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get messages
    @GetMapping("/messages/{foodPostId}")
    @ResponseBody
    public ResponseEntity<?> getMessages(@PathVariable Long foodPostId,
            @RequestParam String otherPhone,
            @RequestParam(defaultValue = "false") boolean isRequest,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null)
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        List<ChatMessage> messages = chatMessageRepository.findConversation(foodPostId, user.getPhone().trim(),
                otherPhone.trim(), isRequest);
        return ResponseEntity.ok(messages);
    }

    // Unread count
    @GetMapping("/unread-count")
    @ResponseBody
    public ResponseEntity<?> getUnreadCount(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null)
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        return ResponseEntity
                .ok(Map.of("unreadCount", chatMessageRepository.countUnreadMessages(user.getPhone().trim())));
    }
}

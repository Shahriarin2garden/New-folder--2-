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

    // View chat list page
    @GetMapping
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
            Optional<FoodPost> foodPostOpt = foodRepository.findById(msg.getFoodPostId());
            if (foodPostOpt.isEmpty())
                continue;

            FoodPost post = foodPostOpt.get();

            // Determine Role
            boolean isDonor = post.getDonorPhone().trim().equals(userPhone);
            boolean isReceiver = post.getReceiverPhone() != null && post.getReceiverPhone().trim().equals(userPhone);

            // Skip if user is neither donor nor receiver (shouldn't happen with the query
            // but good for safety)
            if (!isDonor && !isReceiver)
                continue;

            // Determine Other Party Details
            String otherPhone = msg.getSenderPhone().trim().equals(userPhone)
                    ? msg.getReceiverPhone().trim()
                    : msg.getSenderPhone().trim();

            // Fetch the most up-to-date name from User repo
            String otherName = userRepository.findByPhone(otherPhone)
                    .map(User::getName)
                    .filter(name -> name != null && !name.trim().isEmpty())
                    .orElse(msg.getSenderPhone().trim().equals(userPhone) ? "User" : msg.getSenderName());

            if (otherName == null || otherName.trim().isEmpty())
                otherName = "User";

            // Count unread messages (sent TO current user)
            long unreadCount = chatMessageRepository.findConversation(post.getId(), userPhone, otherPhone)
                    .stream()
                    .filter(m -> !m.isReadStatus() && m.getReceiverPhone().trim().equals(userPhone))
                    .count();

            Map<String, Object> conv = new HashMap<>();
            conv.put("foodPostId", post.getId());
            conv.put("foodName", post.getFoodName());
            conv.put("otherUserName", otherName);
            conv.put("otherUserPhone", otherPhone);
            conv.put("lastMessage", msg.getMessage());
            conv.put("lastMessageTime", msg.getTimestamp());
            conv.put("unreadCount", unreadCount);
            conv.put("isReceived", post.isReceived());
            conv.put("location", post.getLocation());

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
            HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null)
            return "redirect:/login";

        Optional<FoodPost> foodPost = foodRepository.findById(foodPostId);
        if (foodPost.isEmpty())
            return "redirect:/chat";

        FoodPost post = foodPost.get();
        String userPhone = user.getPhone().trim();
        String targetPhone = otherPhone.trim();

        // 1. Authorization: User must be either Donor or Receiver
        boolean isDonor = post.getDonorPhone().trim().equals(userPhone);
        boolean isReceiver = post.getReceiverPhone() != null && post.getReceiverPhone().trim().equals(userPhone);

        // 2. Cross-Verification: The 'otherPhone' must be the actual counterpart
        boolean isValidCounterpart = (isDonor && post.getReceiverPhone() != null
                && post.getReceiverPhone().trim().equals(targetPhone))
                || (isReceiver && post.getDonorPhone().trim().equals(targetPhone));

        if (!(isDonor || isReceiver) || !isValidCounterpart) {
            return "redirect:/chat";
        }

        // Get and mark as read
        List<ChatMessage> messages = chatMessageRepository.findConversation(foodPostId, userPhone, targetPhone);
        messages.stream()
                .filter(m -> !m.isReadStatus() && m.getReceiverPhone().trim().equals(userPhone))
                .forEach(m -> {
                    m.setReadStatus(true);
                    chatMessageRepository.save(m);
                });

        String otherName = userRepository.findByPhone(targetPhone)
                .map(User::getName)
                .filter(name -> name != null && !name.trim().isEmpty())
                .orElse("Unknown User");

        model.addAttribute("foodPost", post);
        model.addAttribute("messages", messages);
        model.addAttribute("currentUser", user);
        model.addAttribute("otherUserName", otherName);
        model.addAttribute("otherUserPhone", targetPhone);
        model.addAttribute("myRole", isDonor ? "DONOR" : "RECEIVER");

        return "chat-room";
    }

    // Send message via REST API
    @PostMapping("/send")
    @ResponseBody
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> payload, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null)
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        try {
            Long foodPostId = Long.parseLong(payload.get("foodPostId"));
            String targetPhone = payload.get("receiverPhone").trim();
            String message = payload.get("message");
            String userPhone = user.getPhone().trim();

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Empty message"));
            }

            // Security: Ensure sender and receiver are the CORRECT participants for this
            // post
            Optional<FoodPost> postOpt = foodRepository.findById(foodPostId);
            if (postOpt.isEmpty())
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid post"));

            FoodPost post = postOpt.get();
            boolean isDonor = post.getDonorPhone().trim().equals(userPhone);
            boolean isReceiver = post.getReceiverPhone() != null && post.getReceiverPhone().trim().equals(userPhone);

            boolean authorizedReceiver = (isDonor && post.getReceiverPhone() != null
                    && post.getReceiverPhone().trim().equals(targetPhone))
                    || (isReceiver && post.getDonorPhone().trim().equals(targetPhone));

            if (!authorizedReceiver) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "You are not authorized to message this user about this post"));
            }

            ChatMessage chatMsg = new ChatMessage(foodPostId, userPhone, user.getName(), targetPhone, message.trim());
            chatMessageRepository.save(chatMsg);

            return ResponseEntity.ok(Map.of("success", true, "message", chatMsg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get messages
    @GetMapping("/messages/{foodPostId}")
    @ResponseBody
    public ResponseEntity<?> getMessages(@PathVariable Long foodPostId, @RequestParam String otherPhone,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null)
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        List<ChatMessage> messages = chatMessageRepository.findConversation(foodPostId, user.getPhone().trim(),
                otherPhone.trim());
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

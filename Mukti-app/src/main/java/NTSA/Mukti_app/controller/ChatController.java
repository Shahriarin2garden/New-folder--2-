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
        if (user == null) return "redirect:/login";

        // Get all food posts user is involved in
        List<FoodPost> donorPosts = foodRepository.findByDonorPhoneOrderByIdDesc(user.getPhone());
        List<FoodPost> receivedPosts = foodRepository.findByReceiverPhone(user.getPhone());

        // Get latest messages for conversations
        List<ChatMessage> latestMessages = chatMessageRepository.findLatestMessagesByUser(user.getPhone());

        // Build conversation list with food post details
        List<Map<String, Object>> conversations = new ArrayList<>();
        Set<Long> processedPostIds = new HashSet<>();

        for (ChatMessage msg : latestMessages) {
            if (processedPostIds.contains(msg.getFoodPostId())) continue;

            Optional<FoodPost> foodPost = foodRepository.findById(msg.getFoodPostId());
            if (foodPost.isEmpty()) continue;

            processedPostIds.add(msg.getFoodPostId());
            FoodPost post = foodPost.get();

            // Determine other party
            String otherPhone = msg.getSenderPhone().equals(user.getPhone()) 
                              ? msg.getReceiverPhone() : msg.getSenderPhone();
            String otherName = msg.getSenderPhone().equals(user.getPhone()) 
                             ? userRepository.findByPhone(otherPhone).map(User::getName).orElse("Unknown")
                             : msg.getSenderName();

            // Count unread messages from this conversation
            long unreadCount = chatMessageRepository.findConversation(
                post.getId(), user.getPhone(), otherPhone)
                .stream()
                .filter(m -> !m.isReadStatus() && m.getReceiverPhone().equals(user.getPhone()))
                .count();

            Map<String, Object> conversation = new HashMap<>();
            conversation.put("foodPostId", post.getId());
            conversation.put("foodName", post.getFoodName());
            conversation.put("otherUserName", otherName);
            conversation.put("otherUserPhone", otherPhone);
            conversation.put("lastMessage", msg.getMessage());
            conversation.put("lastMessageTime", msg.getTimestamp());
            conversation.put("unreadCount", unreadCount);
            conversation.put("isReceived", post.isReceived());

            conversations.add(conversation);
        }

        model.addAttribute("conversations", conversations);
        model.addAttribute("user", user);
        return "chat-list";
    }

    // View specific chat room
    @GetMapping("/room/{foodPostId}")
    public String chatRoom(@PathVariable Long foodPostId, 
                          @RequestParam String otherPhone,
                          HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<FoodPost> foodPost = foodRepository.findById(foodPostId);
        if (foodPost.isEmpty()) {
            return "redirect:/chat";
        }

        FoodPost post = foodPost.get();

        // Verify user is part of this conversation
        boolean isAuthorized = post.getDonorPhone().equals(user.getPhone()) 
                            || post.getReceiverPhone().equals(user.getPhone());
        if (!isAuthorized) {
            return "redirect:/chat";
        }

        // Get conversation messages
        List<ChatMessage> messages = chatMessageRepository.findConversation(
            foodPostId, user.getPhone(), otherPhone);

        // Mark messages as read
        messages.stream()
            .filter(m -> !m.isReadStatus() && m.getReceiverPhone().equals(user.getPhone()))
            .forEach(m -> {
                m.setReadStatus(true);
                chatMessageRepository.save(m);
            });

        // Get other user info
        Optional<User> otherUser = userRepository.findByPhone(otherPhone);
        String otherName = otherUser.map(User::getName).orElse("Unknown");

        model.addAttribute("foodPost", post);
        model.addAttribute("messages", messages);
        model.addAttribute("currentUser", user);
        model.addAttribute("otherUserName", otherName);
        model.addAttribute("otherUserPhone", otherPhone);

        return "chat-room";
    }

    // Send message via REST API
    @PostMapping("/send")
    @ResponseBody
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> payload, 
                                        HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            Long foodPostId = Long.parseLong(payload.get("foodPostId"));
            String receiverPhone = payload.get("receiverPhone");
            String message = payload.get("message");

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty"));
            }

            // Verify food post exists
            Optional<FoodPost> foodPost = foodRepository.findById(foodPostId);
            if (foodPost.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Food post not found"));
            }

            // Create and save message
            ChatMessage chatMessage = new ChatMessage(
                foodPostId, 
                user.getPhone(), 
                user.getName(),
                receiverPhone, 
                message.trim()
            );
            chatMessageRepository.save(chatMessage);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", chatMessage);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to send message: " + e.getMessage()));
        }
    }

    // Get messages for a food post
    @GetMapping("/messages/{foodPostId}")
    @ResponseBody
    public ResponseEntity<?> getMessages(@PathVariable Long foodPostId,
                                        @RequestParam String otherPhone,
                                        HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        List<ChatMessage> messages = chatMessageRepository.findConversation(
            foodPostId, user.getPhone(), otherPhone);

        return ResponseEntity.ok(messages);
    }

    // Get unread message count
    @GetMapping("/unread-count")
    @ResponseBody
    public ResponseEntity<?> getUnreadCount(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        Long count = chatMessageRepository.countUnreadMessages(user.getPhone());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }
}

package NTSA.Mukti_app.repository;

import NTSA.Mukti_app.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Get all messages for a specific food post
    List<ChatMessage> findByFoodPostIdOrderByTimestampAsc(Long foodPostId);

    // Get conversation between two users for a food post
    @Query("SELECT m FROM ChatMessage m WHERE m.foodPostId = ?1 AND " +
           "((m.senderPhone = ?2 AND m.receiverPhone = ?3) OR " +
           "(m.senderPhone = ?3 AND m.receiverPhone = ?2)) " +
           "ORDER BY m.timestamp ASC")
    List<ChatMessage> findConversation(Long foodPostId, String phone1, String phone2);

    // Get all conversations for a user
    @Query("SELECT DISTINCT m.foodPostId FROM ChatMessage m WHERE " +
           "m.senderPhone = ?1 OR m.receiverPhone = ?1")
    List<Long> findUserConversations(String phone);

    // Count unread messages for a user
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiverPhone = ?1 AND m.readStatus = false")
    Long countUnreadMessages(String phone);

    // Mark messages as read
    @Query("UPDATE ChatMessage m SET m.readStatus = true WHERE " +
           "m.foodPostId = ?1 AND m.receiverPhone = ?2 AND m.readStatus = false")
    void markAsRead(Long foodPostId, String phone);

    // Get latest message for each food post
    @Query("SELECT m FROM ChatMessage m WHERE m.id IN " +
           "(SELECT MAX(m2.id) FROM ChatMessage m2 WHERE " +
           "(m2.senderPhone = ?1 OR m2.receiverPhone = ?1) " +
           "GROUP BY m2.foodPostId) ORDER BY m.timestamp DESC")
    List<ChatMessage> findLatestMessagesByUser(String phone);
}

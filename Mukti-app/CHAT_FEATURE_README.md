# 💬 MUKTI Chat Feature Documentation

## Overview
The MUKTI app now includes a comprehensive chat/messaging system that enables **food donors** and **food receivers** to communicate directly with each other. This feature facilitates coordination of pickup times, food details, and builds community connections.

---

## ✨ Key Features

### 1. **Automatic Chat Initiation**
- When a receiver claims food, an initial chat message is automatically sent to the donor
- Default message: "Hi! I would like to receive the [Food Name]. Can we arrange the pickup?"

### 2. **Real-time Messaging**
- Messages are polled every 3 seconds for near-real-time updates
- No page refresh needed - new messages appear automatically
- Smooth animations and modern UI

### 3. **Conversation Management**
- View all active conversations in one place
- See the latest message and timestamp
- Unread message badges
- Food post status indicators (Available/Received)

### 4. **Message Features**
- Send text messages
- View message history
- Mark messages as read automatically when viewing
- See sender name and timestamp for each message

---

## 🎯 How It Works

### For Food Receivers:
1. Browse available food on the `/food/receive-page`
2. Click "Receive" on a food post
3. A chat conversation is automatically created with the donor
4. Navigate to **Messages** from the dashboard
5. Chat with the donor to arrange pickup details

### For Food Donors:
1. Post food on `/food/donate`
2. Wait for receivers to claim your food
3. Check **Messages** on the dashboard when someone receives your food
4. Coordinate pickup time and location with the receiver

---

## 🗂️ Technical Implementation

### Database Tables

#### `chat_messages`
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key |
| food_post_id | BIGINT | Reference to food post |
| sender_phone | VARCHAR | Sender's phone number |
| sender_name | VARCHAR | Sender's display name |
| receiver_phone | VARCHAR | Receiver's phone number |
| message | TEXT | Message content |
| timestamp | TIMESTAMP | When message was sent |
| read_status | BOOLEAN | Whether message has been read |
| message_type | VARCHAR | Type of message (TEXT/SYSTEM) |

### API Endpoints

#### Chat Pages
- `GET /chat` - View all conversations
- `GET /chat/room/{foodPostId}?otherPhone={phone}` - Open specific chat room

#### REST API
- `POST /chat/send` - Send a new message
  ```json
  {
    "foodPostId": 123,
    "receiverPhone": "1234567890",
    "message": "Hello!"
  }
  ```

- `GET /chat/messages/{foodPostId}?otherPhone={phone}` - Get messages
- `GET /chat/unread-count` - Get unread message count

### Key Components

1. **Model**: `ChatMessage.java`
   - Entity representing a chat message
   - Includes sender, receiver, food post reference, and message content

2. **Repository**: `ChatMessageRepository.java`
   - JPA repository with custom queries
   - Find conversations, mark as read, count unread, etc.

3. **Controller**: `ChatController.java`
   - Handles chat UI and REST endpoints
   - Manages conversation list and chat rooms
   - Processes message sending

4. **Templates**:
   - `chat-list.html` - Conversation list page
   - `chat-room.html` - Individual chat room with messaging interface

---

## 🚀 Usage Examples

### Accessing the Chat
From the dashboard, click on the **Messages** card:
```
Dashboard → Messages → View Conversations
```

### Sending a Message
1. Open a conversation from the list
2. Type your message in the input field
3. Click the send button (➤) or press Enter
4. Message appears instantly in the chat

### Viewing Unread Messages
- Unread message count is displayed as a badge on each conversation
- Messages are marked as read when you open the chat room

---

## 🎨 UI Features

### Chat List Page
- Shows all active conversations
- Displays:
  - Other user's name and avatar
  - Food post name
  - Last message preview
  - Timestamp
  - Unread count badge
  - Status (Available/Received)

### Chat Room Page
- Clean, modern messaging interface
- Sent messages appear on the right (purple gradient)
- Received messages appear on the left (white background)
- Timestamps for each message
- Auto-scroll to latest messages
- Responsive design for mobile and desktop

---

## 🔧 Configuration

### Message Polling Interval
Messages are polled every 3 seconds by default. To change:

In `chat-room.html`, line ~200:
```javascript
setInterval(async () => {
    // Poll for new messages
}, 3000); // Change this value (in milliseconds)
```

### WebSocket Integration (Future Enhancement)
The app already has WebSocket configuration at `/ws/chat`. This can be enhanced to provide true real-time messaging instead of polling.

---

## 📱 Mobile Support
- Fully responsive design
- Touch-friendly UI elements
- Optimized for small screens
- Works seamlessly on phones and tablets

---

## 🔒 Security Features
- Session-based authentication required
- Users can only access their own conversations
- Phone numbers validated on backend
- HTML escaping to prevent XSS attacks
- Messages scoped to specific food posts

---

## 🐛 Troubleshooting

### Messages Not Appearing
1. Check if you're logged in
2. Verify the food post was properly received
3. Check browser console for errors
4. Ensure backend is running

### Can't Send Messages
1. Verify session is active
2. Check network connection
3. Ensure food post ID is valid
4. Check backend logs for errors

---

## 🎯 Future Enhancements

### Planned Features
1. **WebSocket Real-time Updates** - Replace polling with true real-time messaging
2. **Image Sharing** - Allow users to share photos of food
3. **Location Sharing** - Share pickup location via map
4. **Notification System** - Push notifications for new messages
5. **Message Search** - Search within conversations
6. **Read Receipts** - Show when messages are read
7. **Typing Indicators** - Show when other person is typing
8. **Message Reactions** - Like/react to messages
9. **Voice Messages** - Record and send voice messages
10. **Group Chat** - Multiple receivers for one food post

---

## 📊 Database Queries

### Get Conversation
```java
chatMessageRepository.findConversation(foodPostId, phone1, phone2);
```

### Count Unread Messages
```java
chatMessageRepository.countUnreadMessages(userPhone);
```

### Get Latest Messages
```java
chatMessageRepository.findLatestMessagesByUser(userPhone);
```

### Mark as Read
```java
chatMessageRepository.markAsRead(foodPostId, userPhone);
```

---

## 🎉 Benefits

1. **Better Coordination** - Donors and receivers can discuss pickup details
2. **Builds Community** - Creates connections between users
3. **Reduces No-shows** - Clear communication prevents misunderstandings
4. **Transparency** - Both parties can verify details
5. **Convenience** - No need for external messaging apps
6. **History** - All communication is logged and accessible

---

## 📞 Support

For issues or questions about the chat feature:
1. Check the troubleshooting section above
2. Review application logs at runtime
3. Check browser console for client-side errors
4. Verify database schema is up to date

---

## 🏆 Success Metrics

Track these metrics to measure chat feature success:
- Number of active conversations
- Average messages per conversation
- Response time between messages
- User satisfaction ratings
- Completion rate of food transfers with chat vs without

---

**Note**: This chat feature is designed to facilitate food sharing and build community. Please use it responsibly and maintain respectful communication.

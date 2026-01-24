package NTSA.Mukti_app.socket;

import org.springframework.stereotype.Component;
import java.io.*;
import java.net.*;

@Component
public class NotificationClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8888;

    public void send(String message) {
        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(message);
        } catch (IOException e) {
            System.err.println("Notification Server is not running. Message not sent.");
        }
    }
}
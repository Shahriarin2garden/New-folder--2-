package NTSA.Mukti_app.socket;

import java.io.*;
import java.net.*;

public class NotificationServer {
    private static final int PORT = 8888; // ৮০৮০ এর সাথে যাতে সংঘাত না হয়

    public void start() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Notification Server Started on Port: " + PORT);
                while (true) {
                    try (Socket socket = serverSocket.accept();
                         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                        String message = in.readLine();
                        System.out.println("New Notification: " + message);
                    }
                }
            } catch (IOException e) {
                // যদি পোর্ট আগে থেকেই চালু থাকে তবে এই এররটি হ্যান্ডেল করবে
                System.err.println("Could not listen on port " + PORT + ". It might be already in use.");
            }
        }).start();
    }
}
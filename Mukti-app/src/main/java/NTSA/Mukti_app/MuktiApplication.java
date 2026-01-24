package NTSA.Mukti_app;

import NTSA.Mukti_app.socket.NotificationServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MuktiApplication {



    public static void main(String[] args) {
        // ১. স্প্রিং বুট অ্যাপ্লিকেশন চালু করা (Web Server on 8080)
        SpringApplication.run(MuktiApplication.class, args);

        // ২. সকেট সার্ভার চালু করা (Notification Server on 8888)
        // এটি একটি আলাদা থ্রেডে চলবে যাতে মেইন অ্যাপ্লিকেশন ব্লক না হয়
        new NotificationServer().start();

        System.out.println("==============================================");
        System.out.println("   MUKTI APP IS READY ON PORT 8080");
        System.out.println("   NOTIFICATION SERVER READY ON PORT 8888");
        System.out.println("==============================================");
    }
}

package com.example.bulkEmailArchitecture.Service;

import com.example.bulkEmailArchitecture.Dto.EmailRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BulkEmailServiceRabbitMQ {

    private final RabbitTemplate rabbitTemplate;
    private final JavaMailSender mailSender;
    private final Logger log = LoggerFactory.getLogger(BulkEmailServiceRabbitMQ.class);

    // Queue config (must match config class)
    private static final String EXCHANGE = "email_exchange";
    private static final String ROUTING_KEY = "email_routing_key";

    // =========================
    // PRODUCER (SEND TO QUEUE)
    // =========================
    public String sendBulkEmail(List<EmailRequest> emailRequestList) {

        try {
            for (EmailRequest email : emailRequestList) {

                rabbitTemplate.convertAndSend(
                        EXCHANGE,
                        ROUTING_KEY,
                        email
                );
            }

            return "Emails pushed to RabbitMQ queue successfully";

        } catch (Exception e) {
            return "Failed to send to queue: " + e.getMessage();
        }
    }

    // =========================
    // CONSUMER (AUTO LISTENER)
    // =========================
    @RabbitListener(queues = "email_queue")
    public void consumeEmail(EmailRequest emailRequest) {

        try {
            sendEmail(emailRequest);

            System.out.println("Email sent to: " + emailRequest.to());

        } catch (Exception e) {
            System.out.println("Failed email: " + emailRequest.to());
        }
    }

    // =========================
    // EMAIL SENDER
    // =========================
    public void sendEmail(EmailRequest emailRequest) throws InterruptedException {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(emailRequest.to());
        message.setSubject(emailRequest.subject());

        String time = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        message.setText(
                emailRequest.body() +
                        "\n\nSent Time: " + time
        );

//        mailSender.send(message); // Will be Open for Real Email Send

        Thread.sleep(1000);


        log.info("Email Sending to -----: " + message); // Demo


    }
}
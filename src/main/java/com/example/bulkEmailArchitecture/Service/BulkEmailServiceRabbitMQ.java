package com.example.bulkEmailArchitecture.Service;

import com.example.bulkEmailArchitecture.Dto.EmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkEmailServiceRabbitMQ {

    private final RabbitTemplate rabbitTemplate;
    private final JavaMailSender mailSender;

    private static final String EXCHANGE = "email_exchange";
    private static final String ROUTING_KEY = "email_routing_key";

    // Pushes bulk requests into the exchange.

    public String sendBulkEmail(List<EmailRequest> emailRequestList) {
        try {
            for (EmailRequest email : emailRequestList) {
                rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, email);
            }
            log.info("Successfully queued {} email requests.", emailRequestList.size());
            return "Emails pushed to RabbitMQ queue successfully";
        } catch (Exception e) {
            log.error("Failed to push emails to RabbitMQ: {}", e.getMessage());
            return "Failed to send to queue: " + e.getMessage();
        }
    }


    @RabbitListener(queues = "email_queue")
    public void consumeEmail(EmailRequest emailRequest) {
        log.info("Processing background email task for: {}", emailRequest.to());

        try {
            sendEmail(emailRequest);
            log.info("Successfully processed email for: {}", emailRequest.to());
        } catch (MailException | InterruptedException e) {
            log.error("Error processing email for {}: {}", emailRequest.to(), e.getMessage());
            // Rethrowing ensures RabbitMQ knows the processing failed
            throw new RuntimeException("Triggering RabbitMQ retry for: " + emailRequest.to(), e);
        }
    }


    private void sendEmail(EmailRequest emailRequest) throws InterruptedException, MailException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailRequest.to());
        message.setSubject(emailRequest.subject());

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        message.setText(emailRequest.body() + "\n\nSent Time: " + time);

        // heavy work
        Thread.sleep(1000);

        // In production, mailSender.send(message) throws MailException if it fails
         mailSender.send(message);

        log.debug("Draft message ready for: {}", emailRequest.to());
    }


    @RabbitListener(queues = "email_queue_dlq")
    public void handlePermanentFailure(EmailRequest failedEmail, Message message) {
        String reason = message.getMessageProperties().getHeader("x-first-death-reason");
        log.error("Task permanently failed for {}. Saving to DB for manual review. Reason: {}",
                failedEmail.to(), reason);

        // saveToDatabase(failedEmail, reason);
    }
}
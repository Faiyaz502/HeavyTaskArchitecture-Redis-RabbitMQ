package com.example.bulkEmailArchitecture.Service;

import com.example.bulkEmailArchitecture.Dto.EmailRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;


@Service
@RequiredArgsConstructor
public class BulkEmailServiceRedis {

    private final RedisTemplate<String,Object> redisTemplate;
    private static final String EMAIL_KEY = "email_queue";
    private final JavaMailSender mailSender;
    private final Logger log = LoggerFactory.getLogger(BulkEmailServiceRedis.class);


    public String sendBulkEmail(List<EmailRequest> emailRequestList) {

        // storing the emails bulk in redis ---
       try{
           for(EmailRequest email : emailRequestList){


               putEmailInQueue(email);

           }
           log.info("BulkEmail Sent Successfully");

            return "Process Started ";

       }catch (Exception e){

           log.error("Exception in BulkSendEmail : ->{}", e.getMessage());

           return "Failed to Start the process";
       }



    }
    public void putEmailInQueue(EmailRequest emailRequest){


        redisTemplate.opsForList().rightPush(EMAIL_KEY,emailRequest);

    }

    //fetching from queue
    @Scheduled(fixedDelay = 20000)
    public void consumerOfEmailsFromQueue(){
        try{

            //Sending bulk email
            EmailRequest emailRequest = (EmailRequest) redisTemplate.opsForList().leftPop(EMAIL_KEY);


            if(emailRequest != null){

                sendEmail(emailRequest);
            }

            log.info("Consuming email from Queue : ->{}",emailRequest);

        }catch (Exception e){

            e.printStackTrace();

        }

    }

    public void sendEmail(EmailRequest emailRequest) throws InterruptedException {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailRequest.to());
        message.setSubject(emailRequest.subject());
        message.setText(
                emailRequest.body()
        );

        LocalDate localDate = LocalDate.now();

        Date date = Date.from(
                localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        );


        message.setSentDate(date);


//        mailSender.send(message); // for Real Email Send

        Thread.sleep(1000);

        System.out.println("Sending Email : ->{} " + message); // Demo



    }

}

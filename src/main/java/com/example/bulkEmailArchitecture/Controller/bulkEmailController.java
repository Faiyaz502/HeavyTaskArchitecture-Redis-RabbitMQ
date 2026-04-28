package com.example.bulkEmailArchitecture.Controller;

import com.example.bulkEmailArchitecture.Dto.EmailRequest;
import com.example.bulkEmailArchitecture.Service.BulkEmailServiceRabbitMQ;
import com.example.bulkEmailArchitecture.Service.BulkEmailServiceRedis;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/bulkEmailSender")
@RequiredArgsConstructor
public class bulkEmailController {

    private final BulkEmailServiceRedis bulkEmailServiceRedis;

    private final BulkEmailServiceRabbitMQ bulkEmailServiceRabbitMQ;

    @PostMapping("/redis")
    public ResponseEntity<String> sendBulkEmailRedis(@RequestBody List<EmailRequest> emailRequestList) {


       String res = this.bulkEmailServiceRedis.sendBulkEmail(emailRequestList);

        return ResponseEntity.ok().body(res);
    }

    @PostMapping("/rabbitMQ")
    public ResponseEntity<String> sendBulkEmailRabbitMQ(@RequestBody List<EmailRequest> emailRequestList) {


        String res = this.bulkEmailServiceRabbitMQ.sendBulkEmail(emailRequestList);

        return ResponseEntity.ok().body(res);
    }
}

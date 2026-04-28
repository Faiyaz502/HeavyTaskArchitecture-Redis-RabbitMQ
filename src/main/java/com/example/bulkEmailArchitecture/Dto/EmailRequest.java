package com.example.bulkEmailArchitecture.Dto;

public record EmailRequest(
        String to , String subject , String body
) {
}

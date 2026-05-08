package com.ebanx.challenge.dto;

public record EventRequest(
        String type,
        String origin,
        String destination,
        Integer amount
) {
}
package com.metamong.metaticket.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class PaymentDTO {

    @Getter
    @Setter
    @Builder
    public static class HiSTORY {
        Long id;
        String userName;
        String concertTitle;
        String concertAddress;
        Long concertPhamplet;
        int amount;
        String status;
    }
}

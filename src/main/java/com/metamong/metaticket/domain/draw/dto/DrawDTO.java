package com.metamong.metaticket.domain.draw.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class DrawDTO {

    @Getter
    @Setter
    @Builder
    public static class HISTORY {
        Long drawId; //응모번호
        Long concertId; //공연번호
        Long concertPhamplet; //공연 팜플렛
        String concertTitle; //공연명
        String concertAddress; //공연장소
        int ranking; //대기번호
        String state; //당첨여부
    }
}

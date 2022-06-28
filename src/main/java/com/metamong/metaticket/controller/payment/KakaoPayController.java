package com.metamong.metaticket.controller.payment;

import com.metamong.metaticket.domain.concert.dto.ConcertDto;
import com.metamong.metaticket.domain.payment.Payment;
import com.metamong.metaticket.service.concert.ConcertService;
import com.metamong.metaticket.service.payment.KakaoPayService;
import com.metamong.metaticket.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.h2.engine.Mode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;
    private final PaymentService paymentService;
    private final ConcertService concertService;

    @GetMapping("/payment")
    public String PaymentPage(@RequestParam("concert") Long concertId, Model model) {
        model.addAttribute( "concertInfo", concertService.concertInfo(concertId));
        return "payment/kakao/pay";
    }

    @PostMapping("/kakaoPay/{concertId}")
    public String kakaoPayPost(@PathVariable Long concertId) {
        log.info("kakaoPay post............................................");

        Payment payment = paymentService.createPayment(concertId);
        return "redirect:" + kakaoPayService.kakaoPayReady(payment);
    }

    @GetMapping("/pay/kakao/success/{paymentId}")
    public String kakaoPaySuccess(@RequestParam("pg_token") String pg_token,@PathVariable("paymentId") Long paymentId, Model model) {
        log.info("kakaoPaySuccess get............................................");
        log.info("kakaoPaySuccess pg_token : " + pg_token);

        model.addAttribute("info", kakaoPayService.kakaoPayInfo(pg_token, paymentId));
        return "redirect:mypage/reservation";
    }

}

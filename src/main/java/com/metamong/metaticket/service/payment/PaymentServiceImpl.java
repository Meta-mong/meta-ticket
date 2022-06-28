package com.metamong.metaticket.service.payment;

import com.metamong.metaticket.domain.concert.Concert;
import com.metamong.metaticket.domain.draw.Draw;
import com.metamong.metaticket.domain.draw.DrawState;
import com.metamong.metaticket.domain.payment.Payment;
import com.metamong.metaticket.domain.payment.PaymentStatus;
import com.metamong.metaticket.domain.payment.dto.PaymentDTO;
import com.metamong.metaticket.domain.user.User;
import com.metamong.metaticket.domain.user.dto.UserDTO;
import com.metamong.metaticket.repository.concert.ConcertRepository;
import com.metamong.metaticket.repository.payment.PaymentRepository;
import com.metamong.metaticket.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ConcertRepository concertRepository;
    private final UserRepository userRepository;
    private final JavaMailSender javaMailSender;
    private final HttpSession httpSession;


    @Override
    @Transactional
    public Payment savePayment(User user, Concert concert) {
        return paymentRepository.save(Payment.createPayment(user, concert));
    }

    @Override
    public List<PaymentDTO.HiSTORY> findByUserId(Long userId) {
        List<Payment> findPayments = paymentRepository.findByUserId(userId);
        List<PaymentDTO.HiSTORY> myPayments = findPayments.stream().map(
                        p -> PaymentDTO.HiSTORY.builder()
                                .id(p.getId())
                                .userName(p.getUser().getName())
                                .concertTitle(p.getConcert().getTitle())
                                .concertAddress(p.getConcert().getAddress())
                                .concertPhamplet(p.getConcert().getPhamplet().getId())
                                .amount(p.getAmount())
                                .status(getPaymentStatusForFront(p.getPaymentStatus()))
                                .build()
                )
                .collect(Collectors.toList());

        return myPayments;
    }

    @Override
    @Transactional
    public void paymentSuccess(User user, Concert concert) {
        Payment paymentInfo = getPaymentInfo(user, concert);
        paymentInfo.setPaymentStatus(PaymentStatus.COMPLETE);
    }

    @Override
    @Transactional
    public void paymentFail(User user, Concert concert) {
        Payment paymentInfo = getPaymentInfo(user, concert);
        paymentInfo.setPaymentStatus(PaymentStatus.FAILED);
    }

    @Override
    public User getUser() {
        return userRepository.findById(getUserId()).orElseThrow(() -> new NoSuchElementException());
    }

    @Override
    public Long getUserId() {
        UserDTO.SESSION_USER_DATA user = getUserDTO();
        return user.getId();
    }

    @Override
    @Transactional
    public Payment createPayment(Long concertId) {
        Concert concert = concertRepository.findById(concertId).orElseThrow(() -> new NoSuchElementException());
        return paymentRepository.save(Payment.createPayment(getUser(), concert));
    }

    @Override
    @Transactional
    public void sendPaymentEmail(Draw draw) {
        SimpleMailMessage message = getSimpleMailMessage(draw);
        javaMailSender.send(message);
        draw.setState(DrawState.WIN);
        draw.setEmailSendDate(LocalDateTime.now());
    }


    private SimpleMailMessage getSimpleMailMessage(Draw draw) {
        String paymentURL = "http://localhost/payment?concert=" + draw.getConcert().getId();
        String text = "이메일 수신일 기준 3일 안에 결제하지 않으면 당첨이 취소됩니다.\n" +
                "결제 페이지: " + paymentURL;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("metaticket3@gmail.com");
        message.setTo(draw.getUser().getEmail());
        message.setSubject("[메타티켓] 콘서트 티켓 당첨 안내");
        message.setText(text);
        return message;
    }

    private Payment getPaymentInfo(User user, Concert concert) {
        Long userId = user.getId();
        Long concertId = concert.getId();
        Payment paymentInfo = paymentRepository.findByUserIdAndConcertId(userId, concertId);
        return paymentInfo;
    }

    private UserDTO.SESSION_USER_DATA getUserDTO() {
        return (UserDTO.SESSION_USER_DATA) httpSession.getAttribute("user");
    }

    private String getPaymentStatusForFront(PaymentStatus status) {
        switch (status) {
            case FAILED: return "결제실패";
            case IN_PROGRESS: return "결제중";
            case COMPLETE: return "결제완료";
        }
        return null;
    }
}

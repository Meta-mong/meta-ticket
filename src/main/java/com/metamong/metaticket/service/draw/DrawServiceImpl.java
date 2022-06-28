package com.metamong.metaticket.service.draw;

import com.metamong.metaticket.domain.concert.Concert;
import com.metamong.metaticket.domain.draw.Draw;
import com.metamong.metaticket.domain.draw.DrawState;
import com.metamong.metaticket.domain.draw.dto.DrawDTO;
import com.metamong.metaticket.domain.user.User;
import com.metamong.metaticket.repository.concert.ConcertRepository;
import com.metamong.metaticket.repository.draw.DrawRepository;
import com.metamong.metaticket.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class DrawServiceImpl implements DrawService {

    private final DrawRepository drawRepository;
    private final ConcertRepository concertRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Draw applyDraw(Long userId, Long concertId) {
       User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException());
        Concert concert = concertRepository.findById(concertId).orElseThrow(() -> new NoSuchElementException());
        Draw draw = Draw.builder().user(user).concert(concert).state(DrawState.STANDBY).build();
        return drawRepository.save(draw);
    }

    @Override
    public List<DrawDTO.HISTORY> findByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException());
        List<Draw> findDraws = drawRepository.findByUser(user);

        List<DrawDTO.HISTORY> myDraws = findDraws.stream().map(
                d -> DrawDTO.HISTORY.builder()
                        .drawId(d.getId())
                        .concertId(d.getConcert().getId())
                        .concertPhamplet(d.getConcert().getPhamplet().getId())
                        .concertTitle(d.getConcert().getTitle())
                        .concertAddress(d.getConcert().getAddress())
                        .ranking(d.getRanking() - drawRepository.findLowRankingGroupByConcert(d.getConcert().getId()).orElse(0))
                        .state(getDrawStateForFront(d.getState()))
                        .build()
                )
                .collect(Collectors.toList());

        return myDraws;
    }

    @Override
    public List<Draw> findByConcertId(Long concertId) {
        Concert concert = concertRepository.findById(concertId).orElseThrow(() -> new NoSuchElementException());
        return drawRepository.findByConcert(concert);
    }

    @Override
    @Transactional
    public void cancelDraw(Long drawId) {
        Draw findDraw = drawRepository.findById(drawId).orElseThrow(() -> new NoSuchElementException());

        if (findDraw.getState() == DrawState.WIN) {
            User findUser = userRepository.findById(findDraw.getUser().getId()).orElseThrow(() -> new NoSuchElementException());
            findUser.setCancelCnt(findUser.getCancelCnt() + 1);
        }

        findDraw.setState(DrawState.CANCEL);
    }

    private String getDrawStateForFront(DrawState state) {
        switch (state){
            case WIN: return "당첨";
            case QUEUE: return "대기순번";
            case CANCEL: return "취소";
            case STANDBY: return "응모 대기중";
            case PAYMENT_FINISH: return "당첨";
        }
        return null;
    }
}

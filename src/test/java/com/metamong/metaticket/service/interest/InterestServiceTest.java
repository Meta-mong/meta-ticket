package com.metamong.metaticket.service.interest;

import com.metamong.metaticket.domain.concert.Concert;
import com.metamong.metaticket.domain.concert.Genre;
import com.metamong.metaticket.domain.concert.Phamplet_File;
import com.metamong.metaticket.domain.concert.Ratings;
import com.metamong.metaticket.domain.draw.Draw;
import com.metamong.metaticket.domain.draw.DrawState;
import com.metamong.metaticket.domain.interest.Interest;
import com.metamong.metaticket.domain.user.User;
import com.metamong.metaticket.repository.concert.ConcertRepository;
import com.metamong.metaticket.repository.draw.DrawRepository;
import com.metamong.metaticket.repository.interest.InterestRepository;
import com.metamong.metaticket.repository.user.UserRepository;
import com.metamong.metaticket.service.draw.DrawService;
import com.metamong.metaticket.service.draw.DrawServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

    @Mock private InterestRepository interestRepository;
    @Mock private UserRepository userRepository;
    @Mock private ConcertRepository concertRepository;

    private InterestService interestService;

    User user;
    Phamplet_File file;
    Concert concert;
    Interest interest;

    @BeforeEach
    void setUp() {
        interestService = new InterestServiceImpl(interestRepository, userRepository, concertRepository);

        user = User.builder().id(1L).email("metamong@naver.com").passwd("7852").name("person1").age(27)
                .number("01012345678").loserCnt(3).cancelCnt(3).build();

        file = new Phamplet_File(1L,"????????????.jpg","/uploadImg/");
        concert = Concert.builder().id(1L).title("????????????").description("???????????? ????????? ????????? ????????? ???????????? ????????? ?????????.").phamplet(file)
                .concertDate(LocalDateTime.now()).genre(Genre.MUSICAL_DRAMA).ratings(Ratings.FIFTEEN).address("?????????????????? ?????????")
                .host("(???)EMK??????????????????").seatNum(250).drawStartDate(LocalDate.now()).drawEndDate(LocalDate.now()).price(150000)
                .visitCnt(5).build();

        interest = Interest.builder().user(user).concert(concert).build();
    }

    @Test
    @DisplayName("?????????")
    void saveInterest() {
        //given
        given(userRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(user));
        given(concertRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(concert));
        given(interestRepository.save(any(Interest.class))).willReturn(interest);

        //when
        Interest savedInterest = interestService.saveInterest(user.getId(), concert.getId());

        //then
        assertEquals(interest, savedInterest);
        verify(userRepository, times(1)).findById(any(Long.class));
        verify(concertRepository, times(1)).findById(any(Long.class));
        verify(interestRepository, times(1)).save(any(Interest.class));
    }

    @Test
    @DisplayName("??? ??????")
    void isInterested() {
        //given
        given(interestRepository.existsByUserIdAndAndConcertId(any(Long.class), any(Long.class))).willReturn(true);

        //when
        boolean isInterested = interestService.isInterested(user.getId(), concert.getId());

        //then
        assertEquals(true, isInterested);
        verify(interestRepository, times(1)).existsByUserIdAndAndConcertId(any(Long.class), any(Long.class));
    }

    @Test
    @DisplayName("?????? ????????? ??????")
    void findUserInterestedConcertList() {
        //given
        List<Interest> interestList = new ArrayList<>();
        interestList.add(interest);

        List<Concert> expect = new ArrayList<>();
        expect.add(concert);

        given(interestRepository.findByUserId(any(Long.class))).willReturn(interestList);
        given(concertRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(concert));

        //when
        List<Concert> interestedConcertList  = interestService.findUserInterestedConcertList(user.getId());

        //then
        assertEquals(expect.get(0), interestedConcertList.get(0));
        verify(interestRepository, times(1)).findByUserId(any(Long.class));
        verify(concertRepository, times(1)).findById(any(Long.class));
    }

    @Test
    @DisplayName("??? ??????")
    void deleteInterest() {
        //given
        given(interestRepository.findByUserIdAndAndConcertId(any(Long.class), any(Long.class))).willReturn(interest);

        //when
        interestService.deleteInterest(user.getId(), concert.getId());

        //then
        verify(interestRepository, times(1)).findByUserIdAndAndConcertId(any(Long.class), any(Long.class));
    }
}
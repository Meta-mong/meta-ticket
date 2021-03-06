package com.metamong.metaticket.service.draw;

import com.metamong.metaticket.domain.concert.Concert;
import com.metamong.metaticket.domain.concert.Genre;
import com.metamong.metaticket.domain.concert.Phamplet_File;
import com.metamong.metaticket.domain.concert.Ratings;
import com.metamong.metaticket.domain.draw.Draw;
import com.metamong.metaticket.domain.draw.DrawState;
import com.metamong.metaticket.domain.draw.dto.DrawDTO;
import com.metamong.metaticket.domain.user.User;
import com.metamong.metaticket.repository.concert.ConcertRepository;
import com.metamong.metaticket.repository.concert.FilesRepository;
import com.metamong.metaticket.repository.draw.DrawRepository;
import com.metamong.metaticket.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class DrawServiceTest {

    @Mock
    private DrawRepository drawRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ConcertRepository concertRepository;

//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private ConcertRepository concertRepository;
//    @Autowired
//    private FilesRepository filesRepository;
//    @Autowired
//    private DrawRepository drawRepository;

    private DrawService drawService;

    Draw draw;
    User user;
    Concert concert;

    @BeforeEach
    void setUp() {
        drawService = new DrawServiceImpl(drawRepository,concertRepository,userRepository);

        user = User.builder().id(1L).email("metamong@naver.com").passwd("7852").name("person1").age(27)
                .number("01012345678").loserCnt(3).cancelCnt(3).build();

        Phamplet_File file = new Phamplet_File(1L,"????????????.jpg","/uploadImg/");
        concert = Concert.builder().id(1L).title("????????????").description("???????????? ????????? ????????? ????????? ???????????? ????????? ?????????.").phamplet(file)
                .concertDate(LocalDateTime.now()).genre(Genre.MUSICAL_DRAMA).ratings(Ratings.FIFTEEN).address("?????????????????? ?????????")
                .host("(???)EMK??????????????????").seatNum(250).drawStartDate(LocalDate.now()).drawEndDate(LocalDate.now()).price(150000)
                .visitCnt(5).build();

        draw = Draw.builder().user(user).concert(concert).state(DrawState.STANDBY).build();
    }

    @Test
    @DisplayName("????????????")
    void applyDraw() {
        System.out.println(userRepository.getClass());
        //given
        given(userRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(user));
        given(concertRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(concert));
        given(drawRepository.save(any(Draw.class))).willReturn(draw);

        //when
        Draw findDraw = drawService.applyDraw(1L, 1L);

        //then
        assertThat(findDraw.getUser().getEmail()).isEqualTo("metamong@naver.com");
        assertThat(findDraw.getConcert().getTitle()).isEqualTo("????????????");

        verify(userRepository, times(1)).findById(any(Long.class));
        verify(concertRepository, times(1)).findById(any(Long.class));
        verify(drawRepository, times(1)).save(any(Draw.class));
    }

    @Test
    @DisplayName("????????? ??????")
    void findByUser() {
        //given
        List<Draw> draws = new ArrayList<>();
        draws.add(draw);
        given(userRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(user));
        given(drawRepository.findByUser(any(User.class))).willReturn(draws);

        //when
        List<DrawDTO.HISTORY> findDraws = drawService.findByUserId(1L);

        //then
        assertThat(findDraws.get(0)).isEqualTo(draws.get(0));
        verify(userRepository, times(1)).findById(any(Long.class));
        verify(drawRepository, times(1)).findByUser(any(User.class));
    }

    @Test
    @DisplayName("???????????? ??????")
    void findByConcert() {
        //given
        List<Draw> draws = new ArrayList<>();
        draws.add(draw);
        given(concertRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(concert));
        given(drawRepository.findByConcert(any(Concert.class))).willReturn(draws);

        //when
        List<Draw> findDraws = drawService.findByConcertId(1L);

        //then
        assertThat(findDraws.get(0)).isEqualTo(draws.get(0));
        verify(concertRepository, times(1)).findById(any(Long.class));
        verify(drawRepository, times(1)).findByConcert(any(Concert.class));
    }


    @Test
    @DisplayName("?????? ??????")
    void cancelDraw() {
        //given
        given(drawRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(draw));

        //when
        drawService.cancelDraw(1L);

        //then
        assertThat(draw.getState()).isEqualTo(DrawState.CANCEL);
        verify(drawRepository, times(1)).findById(any(Long.class));
    }

//    @Test
//    @DisplayName("????????? ?????? ????????? ????????? ??????")
//    void createList() {
//        Phamplet_File phamplet_file = Phamplet_File.builder().fileOriname("?????????1").fileOriname("?????????2").build();
//        Phamplet_File savedFile = filesRepository.save(phamplet_file);
//        Concert concert = Concert.builder().title("?????? ????????? ?????????").phamplet(savedFile).genre(Genre.CONCERT).ratings(Ratings.FIFTEEN).build();
//        Concert savedConcert = concertRepository.save(concert);
//        User user = userRepository.findById(3L).orElse(null);
//        for (int i = 0; i < 8; i++) {
//            Draw draw = Draw.builder().
//                    emailSendDate(LocalDateTime.now()).
//                    concert(savedConcert).
//                    state(DrawState.PAYMENT_FINISH).
//                    user(user).
//                    build();
//            drawRepository.save(draw);
//        }
//    }
}

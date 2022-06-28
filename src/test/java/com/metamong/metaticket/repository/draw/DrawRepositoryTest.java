package com.metamong.metaticket.repository.draw;

import com.metamong.metaticket.domain.concert.Concert;
import com.metamong.metaticket.domain.concert.Genre;
import com.metamong.metaticket.domain.concert.Phamplet_File;
import com.metamong.metaticket.domain.concert.Ratings;
import com.metamong.metaticket.domain.draw.Draw;
import com.metamong.metaticket.domain.draw.DrawState;
import com.metamong.metaticket.domain.user.User;
import com.metamong.metaticket.repository.concert.ConcertRepository;
import com.metamong.metaticket.repository.concert.FilesRepository;
import com.metamong.metaticket.repository.user.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class DrawRepositoryTest {

    @Autowired
    DrawRepository drawRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ConcertRepository concertRepository;

    @Autowired
    FilesRepository filesRepository;

    Draw draw;
    User user;
    Concert concert;
    Phamplet_File file;

//    @BeforeEach
//    void setUp() {
//        user = User.builder().email("metamong@naver.com").passwd("7852").name("person1").age(27)
//                .number("01012345678").loserCnt(3).cancelCnt(3).build();
//
//        userRepository.save(user);
//
//        file = Phamplet_File.builder().filePath("/uploadImg/").fileOriname("웃는남자.jpg").build();
//        filesRepository.save(file);
//
//        concert = Concert.builder().title("웃는남자").description("부자들의 낙원은 가난한 자들의 지옥으로 세워진 것이다.").phamplet(file)
//                .concertDate(LocalDateTime.now()).genre(Genre.MUSICAL_DRAMA).ratings(Ratings.FIFTEEN).address("세종문화회관 대극장")
//                .host("(주)EMK뮤지컬컴퍼니").seatNum(250).drawStartDate(LocalDate.now()).drawEndDate(LocalDate.now()).price(150000)
//                .visitCnt(5).build();
//
//        concertRepository.save(concert);
//        draw = Draw.builder().user(user).concert(concert).state(DrawState.STANDBY).build();
//    }

//    @AfterEach
//    void clean() {
//        drawRepository.deleteAll();
//        userRepository.deleteAll();
//        concertRepository.deleteAll();
//        filesRepository.deleteAll();
//    }
//
//    @Test
//    @DisplayName("조회")
//    void find() {
//        Draw savedDraw = drawRepository.save(draw);
//        Optional<Draw> findDraw = drawRepository.findById(savedDraw.getId());
//
//        findDraw.ifPresent(d -> {
//            assertEquals(d.getId(), savedDraw.getId());
//        });
//    }

    @Test
    @DisplayName("삭제")
    void delete() {
        Draw savedDraw = drawRepository.save(draw);
        drawRepository.delete(savedDraw);
        Optional<Draw> findDraw = drawRepository.findById(savedDraw.getId());

        if (findDraw.isPresent())
            fail();

    }

    @Test
    @DisplayName("저장")
    void save() {
        Draw savedDraw = drawRepository.save(draw);
        assertNotNull(savedDraw);
    }

    @Test
    @DisplayName("콘서트로 가져오기")
    void findByConcert() {
        drawRepository.save(draw);
        List<Draw> findDraws = drawRepository.findByConcert(concert);
        assertNotNull(findDraws.get(0));
    }

    @Test
    @DisplayName("유저로 가져오기")
    void findByUser() {
        drawRepository.save(draw);
        List<Draw> findDraws = drawRepository.findByUser(user);
        assertNotNull(findDraws.get(0));

    }

    @Test
    @DisplayName("이메일 보낸 유저 중 가장 낮은 랭킹 가져오기")
    void findLowRankingGroupByConcert() {
//        int rank = drawRepository.findLowRankingGroupByConcert(1L);
//        System.out.println("rank = " + rank);
    }

    @Test
    @DisplayName("콘서트별 유효한 응모수")
    void findValidDrawCntByConcert() {
        int validDrawCntByConcert = drawRepository.findValidDrawCntByConcert(1L);
        System.out.println("validDrawCntByConcert = " + validDrawCntByConcert);
    }
}

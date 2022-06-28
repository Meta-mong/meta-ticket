package com.metamong.metaticket.repository.interest;

import com.metamong.metaticket.domain.concert.Concert;
import com.metamong.metaticket.domain.concert.Genre;
import com.metamong.metaticket.domain.concert.Phamplet_File;
import com.metamong.metaticket.domain.concert.Ratings;
import com.metamong.metaticket.domain.draw.Draw;
import com.metamong.metaticket.domain.interest.Interest;
import com.metamong.metaticket.domain.user.User;
import com.metamong.metaticket.repository.concert.ConcertRepository;
import com.metamong.metaticket.repository.concert.FilesRepository;
import com.metamong.metaticket.repository.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class InterestRepositoryTest {

    @Autowired
    InterestRepository interestRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ConcertRepository concertRepository;

    @Autowired
    FilesRepository filesRepository;

    Interest interest;
    User user;
    Phamplet_File file;
    Concert concert;

    @BeforeEach
    void setup() {
        user = User.builder().email("metamong@naver.com").passwd("7852").name("person1").age(27)
                .number("01012345678").loserCnt(3).cancelCnt(3).build();

        userRepository.save(user);

        file = Phamplet_File.builder().filePath("/uploadImg/").fileOriname("웃는남자.jpg").build();
        filesRepository.save(file);

        concert = Concert.builder().title("웃는남자").description("부자들의 낙원은 가난한 자들의 지옥으로 세워진 것이다.").phamplet(file)
                .concertDate(LocalDateTime.now()).genre(Genre.MUSICAL_DRAMA).ratings(Ratings.FIFTEEN).address("세종문화회관 대극장")
                .host("(주)EMK뮤지컬컴퍼니").seatNum(250).drawStartDate(LocalDate.now()).drawEndDate(LocalDate.now()).price(150000)
                .visitCnt(5).build();

        concertRepository.save(concert);

        interest = Interest.builder().user(user).concert(concert).build();
    }

    @AfterEach
    void clear() {
        interestRepository.deleteAll();
        concertRepository.deleteAll();
        filesRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("찜 저장")
    void save() {
        Interest savedInterest = interestRepository.save(interest);
        assertEquals(interest, savedInterest);
    }

    @Test
    @DisplayName("찜 조회")
    void find() {
        Interest savedInterest = interestRepository.save(interest);
        Optional<Interest> findInterest = interestRepository.findById(savedInterest.getId());

        if (findInterest.isPresent())
            assertEquals(interest, findInterest.get());
        else
            fail();
    }

    @Test
    @DisplayName("찜 여부")
    void isInterested() {
        interestRepository.save(interest);
        boolean isInterested = interestRepository.existsByUserIdAndAndConcertId(user.getId(), concert.getId());
        assertEquals(true, isInterested);
    }

    @Test
    @DisplayName("유저 찜 목록")
    void findUserInterestList() {
        interestRepository.save(interest);
        List<Interest> findInterests = interestRepository.findByUserId(user.getId());

        assertEquals(interest, findInterests.get(0));
        assertEquals(1, findInterests.size());
    }

    @Test
    @DisplayName("찜 삭제")
    void delete() {
        Interest savedInterest = interestRepository.save(interest);

        interestRepository.delete(savedInterest);
    }

    @Test
    void ex() {
        boolean b = interestRepository.existsByUserIdAndAndConcertId(1L, 1L);
        System.out.println("b = " + b);
    }
}
package com.metamong.metaticket.repository.draw;

import com.metamong.metaticket.domain.concert.Concert;
import com.metamong.metaticket.domain.draw.Draw;
import com.metamong.metaticket.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DrawRepository extends JpaRepository<Draw, Long> {
    List<Draw> findByConcert(Concert concert);
    List<Draw> findByUser(User user);
    Draw findByUserAndAndConcert(User user, Concert concert);

    @Query("select max(d.ranking) from Draw d join d.concert c where c.id=:concertId and d.emailSendDate is not null")
    Optional<Integer> findLowRankingGroupByConcert(@Param("concertId") Long concertId);

    @Query("select count(d.id) from Draw d join d.concert c where c.id=:concertId and d.state<>'CANCEL'")
    int findValidDrawCntByConcert(@Param("concertId") Long concertId);
}

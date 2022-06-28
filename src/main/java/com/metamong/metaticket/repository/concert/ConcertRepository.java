package com.metamong.metaticket.repository.concert;

import com.metamong.metaticket.domain.concert.Concert;
import com.metamong.metaticket.domain.concert.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.web.PageableDefault;

import java.time.LocalDate;
import java.util.List;

public interface ConcertRepository extends JpaRepository<Concert,Long> {
    Page<Concert> findByGenre(@PageableDefault(size = 16) Pageable pageable,Genre genre);

    List<Concert> findAllByDrawStartDateAfter(LocalDate now, Pageable pageable);

    //오픈 티켓 모든 장르
    List<Concert> findAllByDrawStartDateAfterOrderByDrawStartDateAsc(LocalDate now);

    //장르별 오픈 티켓
    List<Concert> findByGenreAndDrawStartDateAfterOrderByDrawStartDateAsc(Genre genre, LocalDate now);

}

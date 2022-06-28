package com.metamong.metaticket.service.concert;

import com.metamong.metaticket.domain.concert.Concert;
import com.metamong.metaticket.domain.concert.Genre;
import com.metamong.metaticket.domain.concert.Phamplet_File;
import com.metamong.metaticket.domain.concert.dto.ConcertDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.List;

public interface ConcertService {

    // 공연 생성
    Long addConcert(Concert concert);

    // 공연 상세내역 조회 -> user
    ConcertDto concertInfo(Long id);

    // 공연 상세내역 조회 -> admin
    ConcertDto concertAdmin(Long id);

    // 공연 수정
    void updateConcert(ConcertDto concertDto, Phamplet_File files);

    // 공연 삭제
    void deleteConcert(Long id);

    // 공연 전체 조회
//    List<ConcertDto> concertAllInfo();
    Page<ConcertDto> concertAllInfo(@PageableDefault(size = 10) Pageable pageable);

    // 장르별 공연 조회
//    List<ConcertDto> concertGenreInfo(Genre genre);
    Page<ConcertDto> concertGenreInfo(@PageableDefault(size = 16) Pageable pageable,Genre genre);

    //상시 판매로 변경
    void checkRemainingSeat(Concert concert);

    //오픈될 티켓 목록 8개 가져오기
    List<ConcertDto> openTickets();

    //모든 오픈 예정 티켓 목록
    List<ConcertDto> allOpenTickets();

    //장르별 오픈 예정 티켓 목록
    List<ConcertDto> openTicketsByGenre(Genre genre);

    // 응모 시작 일자 , 응모 종료 일자 비교
    void isValidDate(ConcertDto.FromAdminConcert concertDto) throws Exception;
}
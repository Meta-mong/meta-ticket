package com.metamong.metaticket.service.concert;

import com.metamong.metaticket.domain.concert.Concert;
import com.metamong.metaticket.domain.concert.Genre;
import com.metamong.metaticket.domain.concert.Phamplet_File;
import com.metamong.metaticket.domain.concert.dto.ConcertDto;
import com.metamong.metaticket.repository.concert.ConcertRepository;
import com.metamong.metaticket.repository.draw.DrawRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Transactional
@RequiredArgsConstructor
@Service
public class ConcertServiceImpl implements ConcertService {

    private final ConcertRepository concertRepository;
    private final DrawRepository drawRepository;


    // 공연 생성
    @Override
    public Long addConcert(Concert concert) {
        Concert saveConcert = concertRepository.save(concert);
        return  saveConcert.getId();
//        return saveConcert;
    }

    // 공연 상세내역 조회 -> user
    @Override
    public ConcertDto concertInfo(Long id) {
        Concert findConcert = concertRepository.findById(id).orElse(null);
        findConcert.setVisitCnt(findConcert.getVisitCnt()+1);
        ConcertDto concertDto = ConcertDto.createDto(findConcert);
        return concertDto;
    }

    // 공연 상세조최 내역 -> admin
    @Override
    public ConcertDto concertAdmin(Long id) {
        Concert findConcert = concertRepository.findById(id).orElse(null);
        ConcertDto concertDto = ConcertDto.createDto(findConcert);
        return concertDto;
    }

    // 공연 수정
    @Override
    public void updateConcert(ConcertDto concertDto, Phamplet_File files) {
        Concert findConcert = concertRepository.findById(concertDto.getId()).orElse(null);

        findConcert.setTitle(concertDto.getTitle());
        findConcert.setDescription(concertDto.getDescription());
        findConcert.setPhamplet(files);
        findConcert.setConcertDate(concertDto.getConcertDate());
        findConcert.setGenre(concertDto.getGenre());
        findConcert.setRatings(concertDto.getRatings());
        findConcert.setAddress(concertDto.getAddress());
        findConcert.setHost(concertDto.getHost());
        findConcert.setSeatNum(concertDto.getSeatNum());
        findConcert.setDrawStartDate(concertDto.getDrawStartDate());
        findConcert.setDrawEndDate(concertDto.getDrawEndDate());
        findConcert.setPrice(concertDto.getPrice());

        concertRepository.save(findConcert);
    }

    // 공연 삭제
    @Override
    public void deleteConcert(Long id) {
        concertRepository.deleteById(id);
    }



    // 공연 전체 조회
    @Override
    public Page<ConcertDto> concertAllInfo(@PageableDefault(size = 10) Pageable pageable) {

        int pagenum = (pageable.getPageNumber() == 0) ? 0 : (pageable.getPageNumber() - 1); // page는 index 처럼 0부터 시작
        pageable = PageRequest.of(pagenum, 10,Sort.by("id").descending());
        Page<Concert> page = concertRepository.findAll(pageable);
        Page<ConcertDto> pageDto = page.map(ConcertDto::createDto);
        return pageDto;
    }
    // 장르별 공연 조회
    @Override
    public Page<ConcertDto> concertGenreInfo(@PageableDefault(size = 16) Pageable pageable, Genre genre){

        int pagenum = (pageable.getPageNumber() == 0) ? 0 : (pageable.getPageNumber() - 1); // page는 index 처럼 0부터 시작
        pageable = PageRequest.of(pagenum, 16,Sort.by("id").descending());
        Page<Concert> page = concertRepository.findByGenre(pageable,genre);
        Page<ConcertDto> pageDto = page.map(ConcertDto::createDto);
        return pageDto;
    }

    //상시 판매로 변경
    @Override
    public void checkRemainingSeat(Concert concert) {
        if (concert.getSeatNum() > drawRepository.findValidDrawCntByConcert(concert.getId()))
            concert.changeSalesMethod();
    }

    @Override
    public List<ConcertDto> openTickets() {
        Sort sort = Sort.by("drawStartDate").ascending();
        Pageable pageable = PageRequest.of(0, 8, sort);
        List<Concert> concerts = concertRepository.findAllByDrawStartDateAfter(LocalDate.now(), pageable);
        List<ConcertDto> opentickets = concerts.stream().map(ConcertDto::createDto).collect(Collectors.toList());
        return  opentickets;
    }

    @Override
    public List<ConcertDto> allOpenTickets() {
        List<Concert> concerts = concertRepository.findAllByDrawStartDateAfterOrderByDrawStartDateAsc(LocalDate.now());
        List<ConcertDto> opentickets = concerts.stream().map(ConcertDto::createDto).collect(Collectors.toList());
        return opentickets;
    }

    @Override
    public List<ConcertDto> openTicketsByGenre(Genre genre) {
        List<Concert> concerts = concertRepository.findByGenreAndDrawStartDateAfterOrderByDrawStartDateAsc(genre, LocalDate.now());
        List<ConcertDto> opentickets = concerts.stream().map(ConcertDto::createDto).collect(Collectors.toList());
        return opentickets;
    }

    @Override
    // 응모 시작 일자 , 응모 종료 일자 비교
    public void isValidDate(ConcertDto.FromAdminConcert concertDto) throws Exception {
        LocalDateTime concertDate = LocalDateTime.parse(concertDto.getConcertDate());
        LocalDate drawStartDate = LocalDate.parse(concertDto.getDrawStartDate());
        LocalDate drawEndDate = LocalDate.parse(concertDto.getDrawEndDate());

        if(drawEndDate.isBefore(drawStartDate) || concertDate.toLocalDate().isBefore(drawEndDate)){
            throw new Exception();
        }
    }

}
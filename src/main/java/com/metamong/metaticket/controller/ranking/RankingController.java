package com.metamong.metaticket.controller.ranking;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.metamong.metaticket.domain.concert.Concert;
import com.metamong.metaticket.domain.concert.Genre;
import com.metamong.metaticket.domain.concert.dto.ConcertDto;
import com.metamong.metaticket.repository.concert.ConcertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/ranking")
public class RankingController {

    @Autowired
    ConcertRepository concertRepository;

    @GetMapping
    public String ranking(Model model){
        Sort sort = Sort.by("visitCnt").descending();
        Pageable pageable = PageRequest.of(0,10,sort);

        Page<Concert> concerts = concertRepository.findByGenre(pageable,Genre.CONCERT);
        Page<ConcertDto> pageDto = concerts.map(ConcertDto::createDto);
        model.addAttribute("concert",pageDto);
        return "ranking/ranking";
    }

    @GetMapping(value={"/","/{genre}"})
    @ResponseBody
    public Page<ConcertDto> concertRanking(@PathVariable(required = false) Genre genre, Model model){
        Sort sort = Sort.by("visitCnt").descending();
        Pageable pageable = PageRequest.of(0,10,sort);
        Page<Concert> concerts = concertRepository.findByGenre(pageable,genre);
        Page<ConcertDto> pageDto = concerts.map(ConcertDto::createDto);
        model.addAttribute("concert",pageDto);

        return pageDto;
    }

    @GetMapping("/main/{genre}")
    @ResponseBody
    public Page<ConcertDto> ranking(@PathVariable Genre genre, Model model){
        Sort sort = Sort.by("visitCnt").descending();
        Pageable pageable = PageRequest.of(0,5,sort);
        Page<Concert> concerts = concertRepository.findByGenre(pageable,genre);
        Page<ConcertDto> pageDto = concerts.map(ConcertDto::createDto);
        model.addAttribute("concert",pageDto);

        return pageDto;
    }

}

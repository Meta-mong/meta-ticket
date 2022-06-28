package com.metamong.metaticket.controller;

import com.metamong.metaticket.domain.concert.Concert;
import com.metamong.metaticket.domain.concert.Genre;
import com.metamong.metaticket.domain.concert.dto.ConcertDto;
import com.metamong.metaticket.service.concert.ConcertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MainController {

    @Autowired
    ConcertService concertService;

    @GetMapping("/")
    public String main(Model model){
        List<ConcertDto> opentickets = concertService.openTickets();
        model.addAttribute("opentickets", opentickets);
        return "index";
    }

    @GetMapping(value={"/ticketopen", "/ticketopen/{genre}"})
    public String ticketOpen(@PathVariable(required = false) Genre genre, Model model){
        List<ConcertDto> opentickets = concertService.openTickets();
        model.addAttribute("opentickets", opentickets);
        List<ConcertDto> openticketList = null;
        if(genre==null || genre==Genre.TOTAL) {
            openticketList = concertService.allOpenTickets();
        }else{
            openticketList = concertService.openTicketsByGenre(genre);
            model.addAttribute("genre", genre);
        }
        model.addAttribute("openticketList", openticketList);
        return "ticketopen/ticket_open";
    }

}

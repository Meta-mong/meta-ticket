package com.metamong.metaticket.controller.interest;

import com.metamong.metaticket.domain.concert.Concert;
import com.metamong.metaticket.domain.concert.dto.ConcertDto;
import com.metamong.metaticket.domain.user.dto.UserDTO;
import com.metamong.metaticket.service.interest.InterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
public class InterestController {

    private final InterestService interestService;
    private final HttpSession session;

    @GetMapping("/interests")
    public String selectInterests(Model model) {
        UserDTO.SESSION_USER_DATA currentUser = (UserDTO.SESSION_USER_DATA) session.getAttribute("user");
        List<Concert> interestedConcertList = interestService.findUserInterestedConcertList(currentUser.getId());
        List<ConcertDto> concertDtoList = interestedConcertList.stream().map(ConcertDto::createDto).collect(Collectors.toList());
        model.addAttribute("concerts", concertDtoList); //concert 사진만으로 바꾸기

        return "mypage/myPage_like";
    }

    @PostMapping("/interests/{concertId}")
    public String saveOrDeleteInterest(@PathVariable("concertId") Long concertId) {
        UserDTO.SESSION_USER_DATA currentUser = (UserDTO.SESSION_USER_DATA) session.getAttribute("user");
        if (!interestService.isInterested(currentUser.getId(), concertId))
            interestService.saveInterest(currentUser.getId(), concertId);
        else
            interestService.deleteInterest(currentUser.getId(), concertId);
        return "redirect:/";
    }

    @ResponseBody
    @GetMapping("/interests/{concertId}")
    public String isInterest(@PathVariable("concertId") Long concertId) {
        UserDTO.SESSION_USER_DATA currentUser = (UserDTO.SESSION_USER_DATA) session.getAttribute("user");

        if (currentUser == null) return "true";

        if (interestService.isInterested(currentUser.getId(), concertId))
            return "false";
        else
            return "true";
    }
}

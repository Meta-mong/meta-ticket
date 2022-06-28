package com.metamong.metaticket.controller.notice;

import com.metamong.metaticket.domain.concert.dto.ConcertDto;
import com.metamong.metaticket.domain.notice.Notice;
import com.metamong.metaticket.domain.notice.dto.NoticeDTO;
import com.metamong.metaticket.repository.notice.NoticeRepository;
import com.metamong.metaticket.service.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Controller
@RequestMapping("/notice")
@RequiredArgsConstructor
@Slf4j
public class NoticeController {

    @Autowired
    NoticeService noticeService;

    @Autowired
    NoticeRepository noticeRepository;

    //이용안내/FAQ
    @GetMapping("/faq")
    public String faq()throws Exception{
        return "notice/user_FAQ";
    }


// 공지사항  상세페이지 조회
    @GetMapping("/noticedetail/{noticeId}")
    public String noticedetail (@PathVariable Long noticeId,Model model) throws Exception {
        NoticeDTO.Notice noticeDto = noticeService.noticedetail(noticeId);
        model.addAttribute("notice",noticeDto);
        return "notice/usernoticedetail";
    }



    // 공지사항 전체 리스트 불러오기
    @GetMapping("/nlist")
    public String noticeList(Model model, Pageable pageable) throws Exception {

        Page<NoticeDTO.Notice> noticeList = noticeService.allNoticeInfo(pageable);
        model.addAttribute("allNoticeList", noticeList);

        log.info("총 element 수 : {}, 전체 page 수 : {}, 페이지에 표시할 element 수 : {}, 현재 페이지 index : {}, 현재 페이지의 element 수 : {}",
                noticeList.getTotalElements(), noticeList.getTotalPages(), noticeList.getSize(),
                noticeList.getNumber(), noticeList.getNumberOfElements());

        return "notice/usernoticelist";
    }

    // 공지사항 등록
    @PostMapping(value = "/upload")
    public String noticeupload(@ModelAttribute NoticeDTO.Notice dto, Model model,Pageable pageable){

        try {
            boolean result = noticeService.register(dto);
            if(result == true){
                Page<NoticeDTO.Notice> lup = noticeService.allNoticeInfo(pageable);
                model.addAttribute("list",lup);
                return "noticelist";
            }
            throw new Exception();
        } catch (Exception e) {
            model.addAttribute("err","등록실패");
            return "noticeupload"; // jsp

        }
    }

    //공지사항 수정
    @PostMapping("/update/{id}")
    public void noticeUpdate(@PathVariable Long id , @ModelAttribute NoticeDTO.Notice dto, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Notice noticeUpdate =noticeService.noticeupdate(dto);
        RequestDispatcher dispatcher = request.getRequestDispatcher("notice/noticelist");
        dispatcher.forward(request, response);
    }


    //공지사항 삭제
    @PostMapping("/delete/{id}")
    public ResponseEntity noticeDelete (@PathVariable Long id) throws Exception {
        noticeService.noticeDelete(id);
        return ResponseEntity.ok(id);
    }


}

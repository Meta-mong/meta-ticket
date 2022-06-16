package com.metamong.metaticket.controller.concert;

import com.metamong.metaticket.domain.concert.Concert;
import com.metamong.metaticket.domain.concert.Genre;
import com.metamong.metaticket.domain.concert.Phamplet_File;
import com.metamong.metaticket.domain.concert.dto.ConcertDto;
import com.metamong.metaticket.service.concert.ConcertService;
import com.metamong.metaticket.service.concert.FilesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;


@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/concert")
public class ConcertController {

    private final ConcertService concertService;

    @Autowired
    FilesService filesService;

    // 공연 생성
    @GetMapping("/adminConcert/upload")
    public String addConcert(){
        return "/admin/admin_addticket";
    }

    @PostMapping("/adminConcert/upload")
    public String addConcert(@ModelAttribute ConcertDto.FromAdminConcert dto, Model model) throws Exception{
        Phamplet_File files = filesService.saveFile(dto.getFile());
        Concert concert = ConcertDto.createConcert(dto,files);
        concert.setDraw(true);
        long concertId = concertService.addConcert(concert);
        ConcertDto concertDto = concertService.concertInfo(concertId);
        model.addAttribute("concert",concertDto);
        return "/admin/admin_ticket_detail";
    }

    // 공연 상세내역 조회
    @GetMapping("/{id}")
    public String concertInfo(@PathVariable Long id , Model model){
        ConcertDto concertDto = concertService.concertInfo(id);
        concertDto.setVisitCnt(concertDto.getVisitCnt()+1);
        model.addAttribute("concert",concertDto);
        return "concert/concert_detail"; // view 이름
    }

    // 관리자 페이지 공연 상세내역 조회
    @GetMapping("/admin/{id}")
    public String adminConcertInfo(@PathVariable Long id , Model model){
        ConcertDto concertDto = concertService.concertInfo(id);
        model.addAttribute("concert",concertDto);
        return "/admin/admin_ticket_detail"; // view 이름
    }

    @GetMapping("/readImg/{id}")
    public void concertImg(@PathVariable Long id, HttpServletResponse response){
        Phamplet_File files = filesService.findById(id);
        File file = new File(files.getFilePath()+files.getFileOriname());
        FileInputStream fis = null;
        try{
            OutputStream out = response.getOutputStream();
            fis = new FileInputStream(file);
            FileCopyUtils.copy(fis,out);
            out.flush();
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            try{
                if(fis != null) fis.close();
            }catch (IOException e){

            }
        }
    }

    // 공연 수정
    @PostMapping("/adminConcert/update/{id}")
    public String updateConcert(@PathVariable Long id,@ModelAttribute ConcertDto.FromAdminConcert dto,Model model) throws Exception {
        ConcertDto concert = concertService.concertInfo(id);
        Phamplet_File files = filesService.findById(concert.getPhamplet());
        filesService.updateFile(dto.getFile(), concert.getPhamplet());
        ConcertDto concertDto = ConcertDto.createConcertDto(dto,files.getId(),concert);
        concertService.updateConcert(concertDto,files);
        model.addAttribute("concert",concertDto);
        return "/admin/admin_ticket_detail";
    }

    // 공연 삭제
    @GetMapping("/adminConcert/delete/{id}")
    public String deleteConcert(@PathVariable Long id,@PageableDefault(size = 10) Pageable pageable,Model model) throws Exception {
        Long fileId = concertService.concertInfo(id).getPhamplet();
        concertService.deleteConcert(id);
        filesService.deleteFile(fileId);

        model.addAttribute("concert",concertService.concertAllInfo(pageable));
        return "/admin/admin_ticket";
    }


    // 공연 전체 조회
    @GetMapping("/adminConcert")
    public String concertList(@PageableDefault(size = 10) Pageable pageable,Model model){
        model.addAttribute("concert",concertService.concertAllInfo(pageable));
        return "/admin/admin_ticket";
    }

    // 장르별 공연 조회
    @GetMapping("/Contents/{genre}")
    public String concertList_Genre(@PageableDefault(size = 16) Pageable pageable ,@PathVariable Genre genre, Model model){
        model.addAttribute("concert",concertService.concertGenreInfo(pageable,genre));
        return "/concert/concert";
    }

}

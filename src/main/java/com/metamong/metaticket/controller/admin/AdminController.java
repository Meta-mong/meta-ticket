package com.metamong.metaticket.controller.admin;

import com.metamong.metaticket.domain.draw.Draw;
import com.metamong.metaticket.domain.notice.Notice;
import com.metamong.metaticket.domain.notice.dto.NoticeDTO;
import com.metamong.metaticket.domain.question.Question;
import com.metamong.metaticket.domain.question.dto.QuestionDTO;
import com.metamong.metaticket.domain.user.User;
import com.metamong.metaticket.domain.user.dto.UserDTO;
import com.metamong.metaticket.domain.user.dto.UserPage;
import com.metamong.metaticket.repository.notice.NoticeRepository;
import com.metamong.metaticket.repository.question.QuestionRepository;
import com.metamong.metaticket.service.admin.AdminService;
import com.metamong.metaticket.service.draw.DrawService;
import com.metamong.metaticket.service.notice.NoticeService;
import com.metamong.metaticket.service.question.QuestionService;
import com.metamong.metaticket.service.user.PageService;
import com.metamong.metaticket.service.user.UserService;
import groovy.util.logging.Log4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.websocket.server.PathParam;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    @Autowired
    HttpSession session;

    @Autowired
    AdminService adminService;

    @Autowired
    UserService userService;

    @Autowired
    PageService pageService;

    @Autowired
    DrawService drawService;

    @Autowired
    NoticeService noticeService;

    @Autowired
    NoticeRepository noticeRepository;

    @Autowired
    QuestionService questionService;

    @Autowired
    QuestionRepository questionRepository;

    //로그인 /로그아웃
    @PostMapping (value = "/login")
    public String adminLogin(@RequestParam ("admin") String adminloginId,
                             @RequestParam("admin123") String password, Model model){
        try {
            boolean result = adminService.adminLogin(adminloginId,password);
            if(result ==true){
                session.setAttribute("adminId",adminloginId);
                model.addAttribute("adminlogin",adminService.adminInfo(adminloginId));
                return "main"; //view
            }else {
                model.addAttribute("err","로그인에 실패했습니다.");
                return "loginForm"; //view
            }

        }catch (Exception e){
            model.addAttribute("err","계정 정보가 없습니다.");
            return "loginForm"; //view
         }
    }

    @GetMapping(value = "/logout")
    public String adminLogout(HttpSession session){
        adminService.adminLogout(session);

        return "redirect:/admin/adminloginform";
    }

    //전체 사용자 조회
    @GetMapping(value = {"/allusers", "/allusers/{presentPage}"})
    public String allUsers(Model model, @PathVariable(required = false) Integer presentPage){
        UserPage userPage = new UserPage();
        if(presentPage != null) userPage.setPresentPage(presentPage);
        long totalCnt = (int)userService.allUserCnt();
        pageService.setPage(userPage, totalCnt);

        Pageable pageable = PageRequest.of(userPage.getPresentPage()-1, 10);
        Page<User> users = userService.createPage(pageable);
        Page<UserDTO.SESSION_USER_DATA> userDtos = users.map(User::createUserDTO);

        model.addAttribute("users", userDtos);
        model.addAttribute("pageInfo", userPage);

        return "/admin/admin_user";
    }

    //회원 정보 상세 조회 페이지
    @GetMapping("/userdetail/{id}")
    public String userDetail(@PathVariable Long id, Model model){
        System.out.println("id : "+ id);
        User user = userService.userInfo(id);
        UserDTO.SESSION_USER_DATA dto = User.createUserDTO(user);
//        List<Draw> draws = drawService.findByUserId(user.getId());
        model.addAttribute("user", dto);
//        model.addAttribute("draws", draws);
        return "/admin/admin_user_detail";
    }

    //회원 정보 수정
    @PostMapping ("/modifyuser")
    @ResponseBody
    public Map<String, Object> modifyUser(@RequestParam("email") String email, @RequestParam("name") String name,
                                          @RequestParam("loserCnt") int loserCnt, @RequestParam("cancelCnt") int cancelCnt){
        Map<String, Object> map = new HashMap<>();
        User user = userService.userInfo(email);
        user.setName(name.trim());
        user.setLoserCnt(loserCnt);
        user.setCancelCnt(cancelCnt);
        map.put("result", userService.saveUser(user));

        return map;
    }

    //공지사항 전체 조회
    @GetMapping("/anlist")
    public String noticeList(Model model, Pageable pageable) throws Exception {

        Page<NoticeDTO.Notice> noticeList = noticeService.allNoticeInfo(pageable);
        model.addAttribute("allNoticeList", noticeList);

        log.info("총 element 수 : {}, 전체 page 수 : {}, 페이지에 표시할 element 수 : {}, 현재 페이지 index : {}, 현재 페이지의 element 수 : {}",
                noticeList.getTotalElements(), noticeList.getTotalPages(), noticeList.getSize(),
                noticeList.getNumber(), noticeList.getNumberOfElements());

        return "admin/admin_noticelist";
    }

// 공지사항  상세페이지 조회
    @GetMapping("/noticedetail/{noticeId}")
    public String noticedetail (@PathVariable Long noticeId,Model model) throws Exception {
        NoticeDTO.Notice noticeDto = noticeService.noticedetail(noticeId);
        model.addAttribute("notice",noticeDto);
        return "/admin/admin_noticedetail";
    }


    //관리자 공지사항 등록

    @GetMapping("/noticeadd")
    public String questionadd(){

        return "/admin/admin_noticeadd";
    }

    @PostMapping("/noticeadd")
    @ResponseBody
    public Map<String,Object> noticeadd(@RequestParam("title") String title, @RequestParam("classify") String classify,
                            @RequestParam("content")String content) throws Exception {
        Map<String,Object> map = new HashMap<>();
        NoticeDTO.Notice dto = NoticeDTO.Notice.builder()
                .title(title)
                .classify(classify)
                .content(content)
                .build();
        try {
            boolean result = noticeService.register(dto);
            if(result == true){
                map.put("result", true);
            }
            throw new Exception();
        } catch (Exception e) {
            map.put("result", false);
        }
        return map;
    }

    //공지사항 수정
    @GetMapping("/noticeupdate/{noticeId}")
    public String noticeUpdate(@PathVariable Long noticeId, Model model) throws Exception {
        NoticeDTO.Notice dto = noticeService.noticedetail(noticeId);
        model.addAttribute("notice", dto);
        return "/admin/admin_noticeupdate";
    }


    //공지사항 수정
    @PostMapping("/noticeupdate/{noticeId}")
    @ResponseBody
    public Map<String,Object> noticeUpdate(@PathVariable Long noticeId, @RequestParam("title") String title, @RequestParam("classify") String classify,
                                        @RequestParam("content")String content) throws Exception {
        Map<String,Object> map = new HashMap<>();
        NoticeDTO.Notice dto = NoticeDTO.Notice.builder()
                .id(noticeId)
                .title(title)
                .classify(classify)
                .content(content)
                .build();
        try {
            Notice notice = noticeService.noticeupdate(dto);
            System.out.println(notice.toString());
            if(notice != null){
                map.put("result", true);
            }
//            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
            map.put("result", false);
        }
        return map;
    }


    //공지사항 삭제

    @GetMapping("/noticedelete/{noticeId}")
    public void noticedelete (@PathVariable Long noticeId, HttpServletRequest request,
                                HttpServletResponse response) throws Exception {
        noticeService.noticeDelete(noticeId);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/admin/anlist");
        dispatcher.forward(request,response);
    }





    ///////////////////////////////////////////////////////////////





    //문의사항 전체 조회
    @GetMapping("/aqlist")
    public String questionList( Model model, Pageable pageable) throws Exception{
        Page<QuestionDTO.Quest> questionList = questionService.allQuestionList(pageable);
        model.addAttribute("allQuestionList", questionList);

        log.info("총 element 수 : {}, 전체 page 수 : {}, 페이지에 표시할 element 수 : {}, 현재 페이지 index : {}, 현재 페이지의 element 수 : {}",
                questionList.getTotalElements(), questionList.getTotalPages(), questionList.getSize(),
                questionList.getNumber(), questionList.getNumberOfElements());

        return "admin/admin_qnalist";
    }
    //문의사항 상세조회
    @GetMapping("/qnadetail/{questionId}")
    public String questiondetail (@PathVariable Long questionId,Model model) throws Exception {
        QuestionDTO.Quest questionDto = questionService.questiondetail(questionId);
        User user = userService.userInfo(questionDto.getUserId());
        model.addAttribute("userName", user.getName());
        model.addAttribute("question",questionDto);
        return "/admin/admin_qnadetail";
    }

    //문의사항 답글 등록 페이지 이동
    @GetMapping("/qnareply/{questionId}")
    public String answer (@PathVariable Long questionId, Model model)throws Exception{
        QuestionDTO.Quest questionDto = questionService.questiondetail(questionId);
        User user = userService.userInfo(questionDto.getUserId());
        model.addAttribute("userName", user.getName());
        model.addAttribute("question",questionDto);
        return "/admin/admin_qnareply";
    }

    // 문의사항 답글 등록 처리
    @PostMapping("/qnareply/{questionId}")
    @ResponseBody
    public Map<String, Object> questionUpdate(@PathVariable Long questionId,
                                 @RequestParam("admincontent") String admincontent) {
        System.out.println("확인 : "+admincontent);
        Map<String,Object> map = new HashMap<>();
        try {
            questionService.answer(questionId, admincontent);
            map.put("result", true);
        }catch(Exception e){
            e.getStackTrace();
            map.put("result", false);
        }
        return map;

    }

    //관리자 문의 답변 삭제
    @GetMapping("/replydelete/{questionId}")
    public void questionDelete (@PathVariable Long questionId, HttpServletRequest request,
                                HttpServletResponse response) throws Exception {
        questionService.questionDelete(questionId);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/admin/aqlist");
        dispatcher.forward(request,response);
    }






}

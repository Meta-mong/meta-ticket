package com.metamong.metaticket.controller.question;

import com.metamong.metaticket.domain.notice.Notice;
import com.metamong.metaticket.domain.notice.dto.NoticeDTO;
import com.metamong.metaticket.domain.question.Question;
import com.metamong.metaticket.domain.question.dto.QuestionDTO;
import com.metamong.metaticket.domain.user.User;
import com.metamong.metaticket.domain.user.dto.UserDTO;
import com.metamong.metaticket.service.question.QuestionService;
import com.metamong.metaticket.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/question")
@RequiredArgsConstructor
@Slf4j
public class QuestionController {

    @Autowired
    QuestionService questionService;

    @Autowired
    UserService userService;

    @Autowired
    HttpSession session;
    @GetMapping("/reply")
    public String reply(){
        return "reply";
    }



    //문의사항 리스트 조회
    @GetMapping(value = {"/qlist","/qlist/{classify}"})
    public String questionList(@PathVariable(required = false)String classify, Model model, Pageable pageable) throws Exception{

        Page<QuestionDTO.Quest> questionList = null;
        if(classify == null){
            questionList = questionService.allQuestionList(pageable);
        }else {
            questionList = questionService.qnaselet(classify, pageable);
        }

        model.addAttribute("allQuestionList", questionList);


        log.info("총 element 수 : {}, 전체 page 수 : {}, 페이지에 표시할 element 수 : {}, 현재 페이지 index : {}, 현재 페이지의 element 수 : {}",
                questionList.getTotalElements(), questionList.getTotalPages(), questionList.getSize(),
                questionList.getNumber(), questionList.getNumberOfElements());

    return "question/userqnalist";
    }

    //문의사항 상세보기
    @GetMapping("/questiondetail/{questionId}")
    public String questiondetail (@PathVariable Long questionId,Model model) throws Exception {
        QuestionDTO.Quest questionDto = questionService.questiondetail(questionId);
        User user = userService.userInfo(questionDto.getUserId());
        model.addAttribute("userName", user.getName());
        model.addAttribute("question",questionDto);
        return "question/userqnadetail";
    }

    //문의사항 등록
    @GetMapping("/userqnaadd")
    public String questionadd(Model model){
        UserDTO.SESSION_USER_DATA currentUser = (UserDTO.SESSION_USER_DATA)session.getAttribute("user");
        if (currentUser == null)
            return "redirect:/signin";
        model.addAttribute("userId", currentUser.getName());
        return "question/userqnaadd";
    }


    //문의사항 등록 - 처리
    @PostMapping( "/userqnaadd")
    public String questionadd(@ModelAttribute QuestionDTO.AddQuest dto, Model model,Pageable pageable){
        try {
            boolean result = questionService.register(dto, session);
            if(result == true){
                Page<QuestionDTO.Quest> qup = questionService.allQuestionList(pageable);
                model.addAttribute("list",qup);
                return "redirect:question/qlist";
            }
            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("err","등록실패");
            return "redirect:question/userqnaadd";

        }
    }

    //문의사항 삭제
    @GetMapping("/userqnadelete/{questionId}")
    public void questionDelete (@PathVariable Long questionId, HttpServletRequest request,
                                HttpServletResponse response) throws Exception {
        questionService.questionDelete(questionId);
        RequestDispatcher dispatcher = request.getRequestDispatcher("question/qlist");
        dispatcher.forward(request,response);
    }


    //문의사항 수정 페이지
    @GetMapping("/userqnaupdate/{questionId}")
    public String questionUpdate(@PathVariable Long questionId, Model model ) throws Exception {
        QuestionDTO.Quest questionDto = questionService.questiondetail(questionId);
        User user = userService.userInfo(questionDto.getUserId());
        model.addAttribute("userName", user.getName());
        model.addAttribute("question",questionDto);
        return "question/userqnaupdate";
    }


    //문의사항 수정
    @PostMapping("/userqnaupdate/{questionId}")
    @ResponseBody
    public Map<String,Object> questionUpdate(@PathVariable Long questionId,
                              @RequestParam("classify") String classify,@RequestParam("title") String title, @RequestParam("content") String content) throws Exception {
        Map<String,Object> map = new HashMap<>();

        QuestionDTO.Quest dto = questionService.questiondetail(questionId);
        dto.setTitle(title);
        dto.setQuesContent(content);
        dto.setClassify(classify);

        Question questionUpdate = questionService.updateQuestion(dto);
        map.put("result",true);
        return map;
    }


    //댓글 추가
    @PostMapping("/replycontent")
    public String answer (@RequestAttribute("id") Long ques_id,
                                @RequestAttribute("reply") String answer)throws Exception{
        System.out.println("ddd : "+answer);
        questionService.answer(ques_id,answer);
        return "re";
    }

    //댓글 삭제
    @PostMapping("/replydelete/{gues_id}")
    public ResponseEntity replyDelete(@PathVariable Long ques_id) throws Exception{
        questionService.replyDelete(ques_id);
        return  ResponseEntity.ok(ques_id);
    }





}

package com.metamong.metaticket.controller.user;

import com.metamong.metaticket.domain.payment.dto.PaymentDTO;
import com.metamong.metaticket.domain.user.User;
import com.metamong.metaticket.domain.user.dto.UserDTO;
import com.metamong.metaticket.service.draw.DrawService;
import com.metamong.metaticket.service.payment.PaymentService;
import com.metamong.metaticket.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MypageController {
    @Autowired
    UserService userService;

    @Autowired
    DrawService drawService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    HttpSession session;

    //비밀번호+age 변경
    @PostMapping("/user/modifyInfo")
    @ResponseBody
    public Map<String, Object> modifyInfo(@RequestParam("email") String email, @RequestParam("passwd") String passwd, @RequestParam("age") int age){
        Map<String, Object> map = new HashMap<>();
        boolean result = userService.modifyInfo(session, passwd, age);
        if(result==false){
            map.put("result", "수정 실패. 다시 시도해주세요.");
        }else{
            UserDTO.SESSION_USER_DATA dto = (UserDTO.SESSION_USER_DATA)session.getAttribute("user");
            User user = userService.userInfo(dto.getId());
            dto.setPasswd(user.getPasswd());
            dto.setAge(age);
            session.setAttribute("user", dto);
            if(session.getAttribute("firstKakaoLogin")!=null) session.removeAttribute("firstKakaoLogin");
            map.put("result", "변경사항이 성공적으로 반영되었습니다.");
        }
        return map;
    }

    //회원 탈퇴 -> 페이지 만들고 해당 페이지에서 비밀번호 입력하고 탈퇴 버튼 누르도록 구현?
    @PostMapping("/user/unregister")
    @ResponseBody
    public Map<String, Object> unregister(@RequestParam("passwd") String passwd){
        Map<String, Object> map = new HashMap<>();
        boolean result = userService.unregister(session, passwd);
        map.put("result", result);
        if(result==true) session.invalidate();
        return map;
    }

    //마이페이지 응모내역
    @GetMapping("/mypage/draw")
    public String myPageDraw(Model model){
        UserDTO.SESSION_USER_DATA currentUser = (UserDTO.SESSION_USER_DATA) session.getAttribute("user");
        if(currentUser==null) return "user/signin";

        model.addAttribute("myDraws", drawService.findByUserId(currentUser.getId()));

        return "mypage/myPage_draw";
    }

    //마이페이지 예매내역
    @GetMapping("/mypage/reservation")
    public String myPageReservation(Model model){
        UserDTO.SESSION_USER_DATA currentUser = (UserDTO.SESSION_USER_DATA) session.getAttribute("user");
        if (currentUser == null) return "redirect:signin";

        model.addAttribute("myPayments", paymentService.findByUserId(currentUser.getId()));

        return "mypage/myPage_reservation";
    }

    //마이페이지 찜목록
    @GetMapping("/mypage/like")
    public String myPageLike(HttpServletRequest request, HttpServletResponse response){
        if(session.getAttribute("user")==null) return "user/signin";
        return "mypage/myPage_like";
    }

}

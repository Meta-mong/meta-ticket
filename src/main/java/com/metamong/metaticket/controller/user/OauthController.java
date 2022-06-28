package com.metamong.metaticket.controller.user;

import com.metamong.metaticket.service.user.OauthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
public class OauthController {

    @Autowired
    HttpSession session;

    @Autowired
    OauthService oauthService;

    @GetMapping("signin/oauth2/code/kakao")
    public void kakaoCallback(@RequestParam String code,
                                HttpServletResponse response) throws Exception {
        String accessToken = oauthService.getAccessToken(code);
        try {
            int result = oauthService.kakaoUserAccess(accessToken);
            if(result==0){
                session.setAttribute("firstKakaoLogin", true);
                response.sendRedirect("mypage");
            }else{
                response.sendRedirect("/");
            }
        }catch (Exception e){
            e.printStackTrace();
            response.sendRedirect("/");
        }
    }



}

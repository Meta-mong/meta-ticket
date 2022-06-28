package com.metamong.metaticket.service.user;

import com.metamong.metaticket.domain.concert.Phamplet_File;
import com.metamong.metaticket.domain.user.User;
import com.metamong.metaticket.domain.user.dto.UserDTO;
import com.metamong.metaticket.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
//@Transactional
class UserServiceTest {
    @Autowired
    private UserService service;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HttpSession session;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ServletContext servletContext;

    @Test
    @DisplayName("이메일 중복 체크")
    void emailCheck() {
        String email = "metamong3@naver.com";
        boolean result = service.emailCheck(email);
        if(result==false){
            System.out.println(email+" 사용 가능");
        } else {
            System.out.println(email+" 사용 불가능");
        }
    }

    @Test
    @DisplayName("전화번호 중복 체크")
    void phoneNumberCheck() {
        String number = "01012345673";
        boolean result = service.phoneNumberCheck(number);
        if(result==false){
            System.out.println(number+" 사용 가능");
        } else {
            System.out.println(number+" 사용 불가능");
        }
    }

    @Test
    @DisplayName("인증번호 전송 체크")
    void sendSms() {
        String number = "01076303241"; //인증번호 보낼 타겟 전화번호
        int authNumber = 7578;
        boolean result = service.sendSms(number, authNumber);
        if(result==true){
            System.out.println("전송 성공");
        } else {
            System.out.println("전송 실패");
        }
    }

    @Test
    @DisplayName("패스워드 체크")
    void passwdCheck() {
        Optional<User> user = userRepository.findById(1L);
        boolean result = service.passwdCheck("3241", user.get());
        System.out.println("result : "+result);
    }

    @Test
    @DisplayName("이메일 찾아오기")
    void inquireEmail() {
        UserDTO.FIND_EMAIL dto = UserDTO.FIND_EMAIL
                .builder()
                .name("person1")
                .number("01012345671").build();
        try{
            System.out.println("이메일 : "+service.existEmail(dto));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Test
    @DisplayName("비밀번호 변경")
    void modifyPasswd() {
        User user = userRepository.findById(1L).get();
        UserDTO.SESSION_USER_DATA userDTO = UserDTO.SESSION_USER_DATA.builder().
                id(user.getId()).
                email(user.getEmail()).
                passwd(user.getPasswd()).
                name(user.getName()).
                age(user.getAge()).
                number(user.getNumber()).
                loserCnt(user.getLoserCnt()).
                cancelCnt(user.getCancelCnt()).
                build();
        session.setAttribute("user", userDTO);
        String passwd = "1234";
        int age = 20220626;
        boolean result = service.modifyInfo(session, passwd, age);
        System.out.println("modify result : "+result);

        boolean comparePasswd = service.passwdCheck(passwd, userRepository.findById(1L).get());
        System.out.println("modified hash passwd compare : "+comparePasswd);
    }

    @Test
    @DisplayName("회원가입")
    void signUp() {
        UserDTO.SIGN_UP userDTO = UserDTO.SIGN_UP.builder().
                email("metamong6@naver.com").
                passwd("1234").
                name("person6").
                age(24).
                number("01012345676").
                build();

        System.out.println("회원가입 결과 : " + service.signUp(userDTO));
    }

    @Test
    @DisplayName("로그인")
    void signIn() {
        UserDTO.SIGN_IN dto = UserDTO.SIGN_IN.builder()
                .email("metamong2@naver.com")
                .passwd("1234")
                .build();
        int result = service.signIn(dto, session);
        if(result==0) {
            System.out.println("로그인 실패");
        }else if(result==1){
            System.out.println("로그인 성공");
            UserDTO.SESSION_USER_DATA sessionUser = (UserDTO.SESSION_USER_DATA) session.getAttribute("user");
            System.out.println("user in session : "+sessionUser.toString());
        } else if (result==2) {
            System.out.println("탈퇴하신 회원 정보를 복구하시겠습니까?");
        } else{
            System.out.println("이미 탈퇴한 계정입니다.");
        }
    }

    @Test
    @DisplayName("로그아웃")
    void signOut(){
        service.signOut(session);
    }

    @Test
    @DisplayName("경로 테스트")
    void pathTest() throws IOException {
        String filePath = servletContext.getRealPath("/uploadImg/1edf622a-9de5-4beb-90c1-5fc11658c90clegend_litte_baseball.jpg");
        File file = new File(filePath);
        System.out.println(file.getPath());
    }
}
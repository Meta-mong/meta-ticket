package com.metamong.metaticket.service.user;

import com.metamong.metaticket.domain.user.User;
import com.metamong.metaticket.domain.user.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

//회원가입, 로그인/로그아웃 -> SpringSecurity 사용 방법도 익히기
public interface UserService {
    //이메일 중복 체크
    public boolean emailCheck(String email);

    //전화번호 중복 체크
    public boolean phoneNumberCheck(String number);

    //전화번호 인증(문자) -> Naver Sens
    //5분 동안 인증 가능하도록 하고, 5분 지나면 다시 문자 인증 버튼 출력하도록 구현
    public boolean sendSms(String userNumber, int authNumber);

    //Signature(서명) 생성
    public String getSignature(String time) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException;

    //해시된 비밀번호 일치 체크
    public boolean passwdCheck(String passwd, User user);

    //회원의 정보 유무 확인 -> 번호 인증 기반
    public boolean existEmail(UserDTO.FIND_EMAIL dto);

    //이메일 조회 by 전화번호
    public String inquireEmail(String number);

    //비밀번호 변경
    //public boolean modifyPasswd(HttpSession session, String passwd);
    public boolean modifyInfo(HttpSession session, String passwd, int age);

    //비밀번호 변경(다형성)
    public void modifyPasswd(Long id, String passwd);

    //회원가입
    public boolean signUp(UserDTO.SIGN_UP userDTO);

    //로그인
    public int signIn(UserDTO.SIGN_IN dto, HttpSession session);

    //로그아웃
    public void signOut(HttpSession session);

    //계정 확인
    public int accountCheck(String email, String number);

    //임시 비밀번호 생성 및 변경
    public String passwdGenerator(String email);

    //임시 비밀번호 발송
    public boolean sendSms(String userNumber, String generatedPasswd);

    //회원 정보 조회
    public User userInfo(Long id);

    //다형성
    public User userInfo(String email);

    //전체 회원 조회
    public List<UserDTO.SESSION_USER_DATA> allUserInfo();

    //회원 탈퇴
    public boolean unregister(HttpSession session, String passwd);

    //계정 복구
    public boolean resign(String email);

    //회원 정보 변경 in admin
    public boolean saveUser(User user);

    //전체 회원 수 조회
    public long allUserCnt();

    //pageable 객체 생성
    public Page<User> createPage(Pageable pageable);
}

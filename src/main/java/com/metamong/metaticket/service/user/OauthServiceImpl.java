package com.metamong.metaticket.service.user;

import com.metamong.metaticket.domain.log.Log;
import com.metamong.metaticket.domain.user.User;
import com.metamong.metaticket.domain.user.dto.UserDTO;
import com.metamong.metaticket.repository.log.LogRepository;
import com.metamong.metaticket.repository.user.UserRepository;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;

@Service
public class OauthServiceImpl implements OauthService{

    @Value("${kakao.rest-api-key}")
    public String apiKey;

    @Value("${kakao.redirect-uri}")
    public String redirectUri;

    @Value("${kakao.client-secret}")
    public String clientSecret;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LogRepository logRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    HttpSession session;

    @Autowired
    UserService userService;

    @Override
    public String getAccessToken(String code){
        String accessToken = "";
        String refreshToken = "";
        String requestURL = "https://kauth.kakao.com/oauth/token";

        try {
            URL url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            //POST 요청
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            conn.setRequestProperty("content-type", "application/x-www-form-urlencoded;charset=utf-8");

            //POST 요청에 필요한 파라미터
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=authorization_code");
            sb.append("&client_id="+apiKey);
            sb.append("&redirect_uri="+redirectUri);
            sb.append("&code=" + code);
            sb.append("&client_secret=" + clientSecret);
            bw.write(sb.toString());
            bw.flush();

            //response code==200 : 성공
            int responseCode = conn.getResponseCode();
            if(responseCode!=200) throw new Exception("Access token 발급 실패");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";

            while((line = br.readLine()) != null){
                result += line;
            }
            //System.out.println("response body : " + result);

            //response msg parsing
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject) parser.parse(result);
            accessToken = object.get("access_token").toString();
            refreshToken = object.get("refresh_token").toString();

            br.close();
            bw.close();

        }catch (Exception e){
            e.printStackTrace();
        }
        return accessToken;
    }

    public int kakaoUserAccess(String token) throws Exception{
        String requestURL = "https://kapi.kakao.com/v2/user/me";
        int res = 0; //0:new     1:exist
        //access token을 이용한 사용자 정보 조회
        try{
            URL url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Authorization", "Bearer "+token);

            int responseCode = conn.getResponseCode();
            if(responseCode != 200) throw new Exception("카카오 사용자 정보 로드 실패");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";
            while ((line = br.readLine()) != null){
                result += line;
            }
            //System.out.println("response body : " + result);

            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject) parser.parse(result);

            long id = (long)object.get("id");
            JSONObject properties = (JSONObject)object.get("properties");
            String nickname = properties.get("nickname").toString();

            JSONObject kakaoAccount = (JSONObject)object.get("kakao_account");
            boolean hasEmail = (boolean)kakaoAccount.get("has_email");
            String email = "";

            if(hasEmail){
                email = kakaoAccount.get("email").toString();
            }

            User user = userRepository.findByEmail(email);
            if(user==null){
                UserDTO.SIGN_UP dto = UserDTO.SIGN_UP.builder()
                        .email(email)
                        .passwd(String.valueOf(id)) //카카오 계정의 아이디를 비밀번호로 설정
                        .name(nickname)
                        .age(0)
                        .number("")
                        .build();
                user = User.createUser(dto, passwordEncoder);
                userRepository.save(user);
            }else{
                res=1;
            }
            /* 추후 user table에 social login attr 추가
            UserDTO.SIGN_IN signInDto = UserDTO.SIGN_IN.builder()
                    .email(user.getEmail())
                    .passwd(String.valueOf(id))
                    .build();

            int signInResult = userService.signIn(signInDto, session);
            if(signInResult!=1) throw new Exception("kakao login 실패");
            */

            UserDTO.SESSION_USER_DATA userDTO = User.createUserDTO(user);
            session.setAttribute("user", userDTO);
            Log log = Log.builder()
                    .visitDate(LocalDateTime.now())
                    .user(user)
                    .build();
            logRepository.save(log);

            br.close();
        } catch(Exception e){
            e.printStackTrace();
            throw e;
        }
        return res;
    }
}

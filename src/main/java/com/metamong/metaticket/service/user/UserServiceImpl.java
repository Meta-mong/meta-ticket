package com.metamong.metaticket.service.user;

import com.metamong.metaticket.domain.log.Log;
import com.metamong.metaticket.domain.user.User;
import com.metamong.metaticket.domain.user.dto.UserDTO;
import com.metamong.metaticket.repository.log.LogRepository;
import com.metamong.metaticket.repository.user.UserRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

@Service
//@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    HttpSession session;

    @Value("${serviceId}")
    private String serviceId;

    @Value("${accessKey}")
    private String accessKey;

    @Value("${secretKey}")
    private String secretKey;

    @Value("${from}")
    private String from;

    @Override
    public boolean emailCheck(String email) {
        String parsedEmail = email.trim();
        boolean result = false; //중복된 이메일 없음
        User user = userRepository.findByEmail(parsedEmail);
        if(user!= null) result = true; //중복된 이메일 있음
        return result;
    }

    @Override
    public boolean phoneNumberCheck(String number) {
        String parsedNumber = number.trim();
        boolean result = false; //중복된 전화번호 없음
        User user = userRepository.findByNumber(parsedNumber);
        if(user!=null) result = true; //중복된 전화번호 있음

        return result;
    }

    @Override
    public boolean sendSms(String userNumber, int authNumber) {
        boolean result = false; //default = 실패
        String time = String.valueOf(System.currentTimeMillis());
        String accessKey = this.accessKey;

        String serviceId = this.serviceId;
        String from = this.from; //등록한 번호만 사용 가능
        String to = userNumber;
        String subject = "[meta_ticket 인증]"; //기본 메시지 제목
        String apiUrl = "https://sens.apigw.ntruss.com/sms/v2/services/"+serviceId+"/messages";

        JSONObject bodyJson = new JSONObject();
        JSONObject toJson = new JSONObject();
        JSONArray toArr = new JSONArray();

        toJson.put("to", to);
        toJson.put("content", "인증번호("+authNumber+") 입력시 인증완료");
        toArr.add(toJson);

        bodyJson.put("type", "SMS");
        bodyJson.put("contentType", "COMM");
        bodyJson.put("countryCode", "82");
        bodyJson.put("from", from);
        bodyJson.put("subject", subject);
        bodyJson.put("content", "인증번호 전송"); //to가 우선순위가 높음
        bodyJson.put("messages", toArr);

        String body = bodyJson.toJSONString();

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn =  (HttpURLConnection)url.openConnection();
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("content-type", "application/json; charset=utf-8");
            conn.setRequestProperty("x-ncp-apigw-timestamp", time);
            conn.setRequestProperty("x-ncp-iam-access-key", accessKey);
            conn.setRequestProperty("x-ncp-apigw-signature-v2", getSignature(time));

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

            dos.write(body.getBytes());
            dos.flush();
            dos.close();

            int responseCode = conn.getResponseCode();
            //System.out.println("responseCode : " + responseCode);
            BufferedReader br;
            if(responseCode==202){
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                result = true; //성공
            } else{
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                result = false; //실패
            }

            String inputLine;
            StringBuilder response = new StringBuilder();
            while((inputLine = br.readLine()) != null){
                response.append(inputLine);
            }
            br.close();

            //System.out.println(response.toString());

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException ie){

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public String getSignature(String time) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        String serviceId = this.serviceId;
        String accessKey = this.accessKey;
        String secretKey = this.secretKey;
        String space = " ";
        String newLine = "\n";
        String method = "POST";
        String url = "/sms/v2/services/" + serviceId + "/messages";

        String message = new StringBuilder()
                .append(method)
                .append(space)
                .append(url)
                .append(newLine)
                .append(time)
                .append(newLine)
                .append(accessKey)
                .toString();

        SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);

        byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
        String encodeBase64String = Base64.getEncoder().encodeToString(rawHmac);

        return encodeBase64String;
    }

    @Override
    public boolean passwdCheck(String passwd, User user) {
        //비밀번호 일치시 true / 일치하지 않을시 false 리턴
        return passwordEncoder.matches(passwd, user.getPasswd());
    }

    @Override
    public boolean existEmail(UserDTO.FIND_EMAIL dto){
        //User user = userRepository.findByNameAndNumber(dto.getName(), dto.getNumber());
        User user = userRepository.findByNumber(dto.getNumber().trim());
        if(user == null) {
            return false;
        }else{
            return true;
        }
    }

    @Override
    public String inquireEmail(String number) {
        User user = userRepository.findByNumber(number.trim());
        try{
            return user.getEmail();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    @Transactional
    public boolean modifyInfo(HttpSession session, String passwd, int age) {
        //인증과정은 생략됨
        String encryptedPasswd = passwordEncoder.encode(passwd);
        UserDTO.SESSION_USER_DATA userDTO = (UserDTO.SESSION_USER_DATA)session.getAttribute("user");
        User user = userRepository.findById(userDTO.getId()).get();
        user.setPasswd(encryptedPasswd);
        user.setAge(age);
        User modifiedUser = userRepository.save(user);
        if(userDTO.getId()==modifiedUser.getId())return true;
        return false;
    }

    @Override
    //@Transactional
    public void modifyPasswd(Long id, String passwd){
        User user = userRepository.findById(id).get();
        String encryptedPasswd = passwordEncoder.encode(passwd);
        user.setPasswd(encryptedPasswd);
        User modifiedser = userRepository.save(user);
    }

    @Override
    @Transactional
    public boolean signUp(UserDTO.SIGN_UP userDTO) { //return type 변경
        //view에서 받은 param들을 Controller에서 UserDTO객체로 받아 넘겨주는 것이 선행되어야 함
        //email이나 passwd 제약은 정규식으로 front단에서 체크

        //이메일 중복 체크
        if(emailCheck(userDTO.getEmail())==true) return false;
        //전화번호 중복체크 -> 일차척으로 front단에서 전화인증 성공 못하면 넘어올 수 없음
        if(phoneNumberCheck(userDTO.getNumber())==true) return false;

        User inputUser = userRepository.save(User.createUser(userDTO ,passwordEncoder));
        //System.out.println(inputUser.toString());

        return true;
    }

    @Override
    @Transactional
    public int signIn(UserDTO.SIGN_IN dto, HttpSession session) {
        //0 : 로그인 실패
        //1 : 로그인 성공
        //2 : 탈퇴한 회원 다시 계정 살릴지 ask
        //-1 : 탈퇴한 계정입니다.

        //Controller 단에서 userDTO가 null인지 확인해서 처리
        User user = userRepository.findByEmail(dto.getEmail());
        UserDTO.SESSION_USER_DATA userDTO = null;

        if(user==null) {
            //System.out.println("계정 정보 없음");
            return 0;
        }
        boolean passwdCheck = passwdCheck(dto.getPasswd(), user);
        //System.out.println("valid 확인 : "+ user.isValid());
        if(passwdCheck==true) {
            if(user.isValid()==true){
                userDTO = User.createUserDTO(user);
                session.setAttribute("user", userDTO);
                Log log = Log.builder()
                        .visitDate(LocalDateTime.now())
                        .user(user)
                        .build();
                Log inputLog = logRepository.save(log);
                //System.out.println(inputLog.toString());
                return 1;
            }else if(user.getValid_date().isAfter(LocalDateTime.now())) { //접속일이 회원 정보 유지 유효기간 내 일시
                return 2; //js 추가해야 함
            }else {
                return -1; //회원 정보 복구할 수 있는 기간을 지남
            }
        }else{
            return 0;
        }
    }

    @Override
    public void signOut(HttpSession session) {
        session.invalidate();
    }

    @Override
    public int accountCheck(String email, String number){
        // -1 : 이메일 없음, -2 : 이메일과 전화번호가 맞지 않음, 1 : 이메일과 전화번호가 일치
        User user = userRepository.findByEmail(email.trim());
        if(user==null){
            return -1;
        }else{
            if(!user.getNumber().equals(number.trim())){
                return -2;
            }else{
                return 1;
            }
        }
    }

    @Override
    public String passwdGenerator(String email){
        try {
            User user = userRepository.findByEmail(email.trim());
            int leftLimit = 97; // letter 'a'
            int rightLimit = 122; // letter 'z'
            int targetStringLength = 10;
            Random random = new Random();
            String generatedPasswd = random.ints(leftLimit, rightLimit + 1)
                    .limit(targetStringLength) //생성할 글자 수
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append) //결과 처리
                    .toString();
            System.out.println(generatedPasswd);
            modifyPasswd(user.getId(), generatedPasswd);
            return generatedPasswd;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean sendSms(String userNumber, String generatedPasswd){
        boolean result = false; //default = 실패
        String time = String.valueOf(System.currentTimeMillis());
        String accessKey = this.accessKey;

        String serviceId = this.serviceId;
        String from = this.from; //등록한 번호만 사용 가능
        String to = userNumber;
        String subject = "[meta_ticket 인증]"; //기본 메시지 제목
        String apiUrl = "https://sens.apigw.ntruss.com/sms/v2/services/"+serviceId+"/messages";

        JSONObject bodyJson = new JSONObject();
        JSONObject toJson = new JSONObject();
        JSONArray toArr = new JSONArray();

        toJson.put("to", to);
        toJson.put("content", "임시 비밀번호는 ("+generatedPasswd+") 입니다. 로그인 후 변경해주세요.");
        toArr.add(toJson);

        bodyJson.put("type", "SMS");
        bodyJson.put("contentType", "COMM");
        bodyJson.put("countryCode", "82");
        bodyJson.put("from", from);
        bodyJson.put("subject", subject);
        bodyJson.put("content", "인증번호 전송"); //to가 우선순위가 높음
        bodyJson.put("messages", toArr);

        String body = bodyJson.toJSONString();

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn =  (HttpURLConnection)url.openConnection();
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("content-type", "application/json; charset=utf-8");
            conn.setRequestProperty("x-ncp-apigw-timestamp", time);
            conn.setRequestProperty("x-ncp-iam-access-key", accessKey);
            conn.setRequestProperty("x-ncp-apigw-signature-v2", getSignature(time));

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

            dos.write(body.getBytes());
            dos.flush();
            dos.close();

            int responseCode = conn.getResponseCode();
            BufferedReader br;
            if(responseCode==202){
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                result = true; //성공
            } else{
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                result = false; //실패
            }

            String inputLine;
            StringBuilder response = new StringBuilder();
            while((inputLine = br.readLine()) != null){
                response.append(inputLine);
            }
            br.close();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException ie){

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public User userInfo(Long id) {
        return userRepository.findById(id).get();
    }

    @Override
    public User userInfo(String email) {
        return userRepository.findByEmail(email.trim());
    }

    @Override
    public List<UserDTO.SESSION_USER_DATA> allUserInfo() {
        List<User> users = userRepository.findAll();
        List<UserDTO.SESSION_USER_DATA> usersDTO = new ArrayList<>();
        for(User user:users){
            UserDTO.SESSION_USER_DATA dto = User.createUserDTO(user);
            usersDTO.add(dto);
        }
        return usersDTO;
    }

    @Override
    public boolean unregister(HttpSession session, String passwd) {
        try {
            UserDTO.SESSION_USER_DATA dto = (UserDTO.SESSION_USER_DATA) session.getAttribute("user");
            //추후 연쇄 삭제될 사항 있는지 확인
            User user = userRepository.findById(dto.getId()).get();
            boolean passwdCheck = passwdCheck(passwd, user);
            if(passwdCheck==false) return false;

            //탈퇴 시 계정 복구 기간을 90일로 지정
            user.setValid(false);
            user.setValid_date(LocalDateTime.now().plusDays(90));
            userRepository.save(user);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean resign(String email){
        User user = userRepository.findByEmail(email.trim());
        user.setValid(true);
        user.setValid_date(null);
        try{
            userRepository.save(user);
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean saveUser(User user){
        try {
            userRepository.save(user);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    @Override
    public long allUserCnt() {
        return userRepository.count();
    }

    @Override
    public Page<User> createPage(Pageable pageable){
        Page<User> users = userRepository.findAll(pageable);
        return users;
    }

}

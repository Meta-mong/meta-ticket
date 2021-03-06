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
        boolean result = false; //????????? ????????? ??????
        User user = userRepository.findByEmail(parsedEmail);
        if(user!= null) result = true; //????????? ????????? ??????
        return result;
    }

    @Override
    public boolean phoneNumberCheck(String number) {
        String parsedNumber = number.trim();
        boolean result = false; //????????? ???????????? ??????
        User user = userRepository.findByNumber(parsedNumber);
        if(user!=null) result = true; //????????? ???????????? ??????

        return result;
    }

    @Override
    public boolean sendSms(String userNumber, int authNumber) {
        boolean result = false; //default = ??????
        String time = String.valueOf(System.currentTimeMillis());
        String accessKey = this.accessKey;

        String serviceId = this.serviceId;
        String from = this.from; //????????? ????????? ?????? ??????
        String to = userNumber;
        String subject = "[meta_ticket ??????]"; //?????? ????????? ??????
        String apiUrl = "https://sens.apigw.ntruss.com/sms/v2/services/"+serviceId+"/messages";

        JSONObject bodyJson = new JSONObject();
        JSONObject toJson = new JSONObject();
        JSONArray toArr = new JSONArray();

        toJson.put("to", to);
        toJson.put("content", "????????????("+authNumber+") ????????? ????????????");
        toArr.add(toJson);

        bodyJson.put("type", "SMS");
        bodyJson.put("contentType", "COMM");
        bodyJson.put("countryCode", "82");
        bodyJson.put("from", from);
        bodyJson.put("subject", subject);
        bodyJson.put("content", "???????????? ??????"); //to??? ??????????????? ??????
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
                result = true; //??????
            } else{
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                result = false; //??????
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
        //???????????? ????????? true / ???????????? ????????? false ??????
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
        //??????????????? ?????????
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
    public boolean signUp(UserDTO.SIGN_UP userDTO) { //return type ??????
        //view?????? ?????? param?????? Controller?????? UserDTO????????? ?????? ???????????? ?????? ??????????????? ???
        //email?????? passwd ????????? ??????????????? front????????? ??????

        //????????? ?????? ??????
        if(emailCheck(userDTO.getEmail())==true) return false;
        //???????????? ???????????? -> ??????????????? front????????? ???????????? ?????? ????????? ????????? ??? ??????
        if(phoneNumberCheck(userDTO.getNumber())==true) return false;

        User inputUser = userRepository.save(User.createUser(userDTO ,passwordEncoder));
        //System.out.println(inputUser.toString());

        return true;
    }

    @Override
    @Transactional
    public int signIn(UserDTO.SIGN_IN dto, HttpSession session) {
        //0 : ????????? ??????
        //1 : ????????? ??????
        //2 : ????????? ?????? ?????? ?????? ????????? ask
        //-1 : ????????? ???????????????.

        //Controller ????????? userDTO??? null?????? ???????????? ??????
        User user = userRepository.findByEmail(dto.getEmail());
        UserDTO.SESSION_USER_DATA userDTO = null;

        if(user==null) {
            //System.out.println("?????? ?????? ??????");
            return 0;
        }
        boolean passwdCheck = passwdCheck(dto.getPasswd(), user);
        //System.out.println("valid ?????? : "+ user.isValid());
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
            }else if(user.getValid_date().isAfter(LocalDateTime.now())) { //???????????? ?????? ?????? ?????? ???????????? ??? ??????
                return 2; //js ???????????? ???
            }else {
                return -1; //?????? ?????? ????????? ??? ?????? ????????? ??????
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
        // -1 : ????????? ??????, -2 : ???????????? ??????????????? ?????? ??????, 1 : ???????????? ??????????????? ??????
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
                    .limit(targetStringLength) //????????? ?????? ???
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append) //?????? ??????
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
        boolean result = false; //default = ??????
        String time = String.valueOf(System.currentTimeMillis());
        String accessKey = this.accessKey;

        String serviceId = this.serviceId;
        String from = this.from; //????????? ????????? ?????? ??????
        String to = userNumber;
        String subject = "[meta_ticket ??????]"; //?????? ????????? ??????
        String apiUrl = "https://sens.apigw.ntruss.com/sms/v2/services/"+serviceId+"/messages";

        JSONObject bodyJson = new JSONObject();
        JSONObject toJson = new JSONObject();
        JSONArray toArr = new JSONArray();

        toJson.put("to", to);
        toJson.put("content", "?????? ??????????????? ("+generatedPasswd+") ?????????. ????????? ??? ??????????????????.");
        toArr.add(toJson);

        bodyJson.put("type", "SMS");
        bodyJson.put("contentType", "COMM");
        bodyJson.put("countryCode", "82");
        bodyJson.put("from", from);
        bodyJson.put("subject", subject);
        bodyJson.put("content", "???????????? ??????"); //to??? ??????????????? ??????
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
                result = true; //??????
            } else{
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                result = false; //??????
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
            //?????? ?????? ????????? ?????? ????????? ??????
            User user = userRepository.findById(dto.getId()).get();
            boolean passwdCheck = passwdCheck(passwd, user);
            if(passwdCheck==false) return false;

            //?????? ??? ?????? ?????? ????????? 90?????? ??????
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

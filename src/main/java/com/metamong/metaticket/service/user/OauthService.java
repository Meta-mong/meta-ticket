package com.metamong.metaticket.service.user;

public interface OauthService {
    //인가 코드를 사용한 access token 발급
    public String getAccessToken(String code);

    //카카오 계정 사용자 생성
    public int kakaoUserAccess(String token) throws Exception;

}

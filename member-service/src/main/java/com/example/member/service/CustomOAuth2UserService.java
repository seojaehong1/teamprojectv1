package com.example.member.service;

import com.example.member.model.Member;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

// 소셜 로그인(OAuth2) 사용자 정보 처리 서비스
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberService memberService;

    public CustomOAuth2UserService(MemberService memberService) {
        this.memberService = memberService;
    }

    // OAuth2 로그인 시 사용자 정보 로드
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // 로그인 제공자 확인 (google, naver)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String email;
        String name;
        
        // Google 로그인
        if ("google".equals(registrationId)) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
        } 
        // Naver 로그인
        else if ("naver".equals(registrationId)) {
            Map<String, Object> response = oAuth2User.getAttribute("response");
            email = (String) response.get("email");
            name = (String) response.get("name");
        } 
        // 지원하지 않는 제공자
        else {
            throw new OAuth2AuthenticationException("지원하지 않는 로그인 방식입니다.");
        }

        // 회원 등록 또는 조회 (password, birthDate, phoneNum은 NULL)
        Member.Provider provider = Member.Provider.valueOf(registrationId.toUpperCase());
        Member member = memberService.registerOrGetSocialMember(email, name, provider);

        // OAuth2User 반환
        return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())),
            oAuth2User.getAttributes(),
            userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName()
        );
    }
}

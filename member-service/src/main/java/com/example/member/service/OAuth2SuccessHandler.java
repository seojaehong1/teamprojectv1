package com.example.member.service;

import com.example.member.model.Member;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberService memberService;

    public OAuth2SuccessHandler(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        String email;
        // Google인 경우
        if (oAuth2User.getAttribute("email") != null) {
            email = oAuth2User.getAttribute("email");
        } 
        // Naver인 경우
        else {
            Map<String, Object> responseMap = oAuth2User.getAttribute("response");
            email = (String) responseMap.get("email");
        }

        Member member = memberService.getMemberByEmail(email);
        String token = memberService.generateTokenForMember(member);

        // URL 인코딩 적용
        String encodedUsername = URLEncoder.encode(member.getUsername(), StandardCharsets.UTF_8);

        // 프론트엔드로 리다이렉트 (토큰 전달)
        String redirectUrl = "http://localhost:8000/oauth2/callback?token=" + token 
                           + "&username=" + encodedUsername
                           + "&userId=" + member.getUserId()
                           + "&role=" + member.getRole().name()
                           + "&provider=" + member.getProvider().name();
        
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}

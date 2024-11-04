package org.revenue.management.member.oauth2.service;

import lombok.RequiredArgsConstructor;
import org.revenue.management.member.dto.MemberDto;
import org.revenue.management.member.entity.Member;
import org.revenue.management.member.entity.Role;
import org.revenue.management.member.repository.MemberRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.revenue.management.member.oauth2.dto.GoogleResponse;
import org.revenue.management.member.oauth2.dto.OAuth2Response;
import org.revenue.management.member.oauth2.userinfo.CustomOAuth2User;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        System.out.println(oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;

        if (registrationId.equals("google")) {

            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }
        else {

            return null;
        }

        //리소스 서버에서 발급 받은 정보로 사용자를 특정할 아이디값을 만듬
        String username = oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId();
        Member existData = memberRepository.findByUsername(username);

        if (existData == null) {
            Member member = Member.builder()
                    .email(oAuth2Response.getEmail())
                    .name(oAuth2Response.getName())
                    .username(username)
                    .role(Role.MEMBER)
                    .socialType(oAuth2Response.getSocialType())
                    .build();

            memberRepository.save(member);
        }

        MemberDto memberDto = new MemberDto();

        memberDto.setId(existData.getId());
        memberDto.setEmail(oAuth2Response.getEmail());
        memberDto.setName(oAuth2Response.getName());
        memberDto.setUsername(username);
        memberDto.setRole(Role.MEMBER);
        memberDto.setSocialType(oAuth2Response.getSocialType());

        return new CustomOAuth2User(memberDto);
    }
}
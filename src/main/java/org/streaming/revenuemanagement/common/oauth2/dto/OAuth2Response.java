package org.streaming.revenuemanagement.common.oauth2.dto;

import org.streaming.revenuemanagement.domain.member.entity.SocialType;

public interface OAuth2Response {

    //제공자 (Ex. naver, google, ...)
    String getProvider();
    //제공자에서 발급해주는 아이디(번호)
    String getProviderId();
    //이메일
    String getEmail();
    //사용자 실명 (설정한 이름)
    String getName();

    SocialType getSocialType();
}

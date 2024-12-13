package com.auction.bid.global.oauth2.userinfo;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class KakaoUserInfo implements OAuth2UserInfo{

    private final Map<String,Object> attributes;

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getName() {
        return (String) ((Map<?, ?>) attributes.get("properties")).get("nickname");
    }
}

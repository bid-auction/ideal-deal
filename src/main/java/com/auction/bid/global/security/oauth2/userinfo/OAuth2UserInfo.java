package com.auction.bid.global.security.oauth2.userinfo;

public interface OAuth2UserInfo {
    String getProviderId();
    String getProvider();
    String getName();
}

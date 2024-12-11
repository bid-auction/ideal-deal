package com.auction.bid.global.oauth2.userinfo;

public interface OAuth2UserInfo {
    String getProviderId();
    String getProvider();
    String getName();
}

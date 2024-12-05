package com.auction.bid.global.security;

public abstract class ConstSecurity {
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER  = "Bearer ";

    public static final String ROLE_MEMBER = "ROLE_MEMBER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    public static final String HAS_ROLE_MEMBER = "hasRole('MEMBER')";
}
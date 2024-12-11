package com.auction.bid.domain.member.service;

import com.auction.bid.domain.member.Address;
import com.auction.bid.domain.member.MemberRepository;
import com.auction.bid.domain.member.MemberService;
import com.auction.bid.domain.member.dto.SignUpDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class MemberServiceImplIntegrTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    SignUpDto.Request signUpReq;
    SignUpDto.Response signUpRes;

    @BeforeEach
    void setUp() {
        signUpReq = SignUpDto.Request.builder()
                .loginId("testLoginId")
                .password("1234567890")
                .email("test@naver.com")
                .nickname("testNickname")
                .name("testName")
                .phoneNumber("010-1234-5678")
                .emailVerified(true)
                .address(
                        Address.builder()
                                .city("seoul")
                                .street("saemalo")
                                .zipcode("548")
                                .build())
                .build();

        signUpRes = SignUpDto.Response.builder()
                .id(1L)
                .loginId("resLoginId")
                .name("resName")
                .nickname("resNickName")
                .build();
    }

    @Test
    @DisplayName("회원 가입 성공")
    void signup_success() throws Exception {
        SignUpDto.Response response = memberService.signUp(signUpReq);

        assertEquals(1L, response.getId());
        assertEquals("testNickname", response.getNickname());
        assertEquals("testLoginId", response.getLoginId());
        assertEquals("testName", response.getName());
    }

    @Test
    @DisplayName("이메일 전송 성공")
    void sendEmail_success() throws Exception {
        String to = "kongminoo@naver.com";
        String actual = memberService.sendEmail(to);
        assertEquals(to, actual);
    }

}

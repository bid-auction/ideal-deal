package com.auction.bid;

import com.auction.bid.domain.member.Address;
import com.auction.bid.domain.member.MemberService;
import com.auction.bid.domain.member.dto.SignUpDto;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
//@RequiredArgsConstructor
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class BidApplication {

//	private final MemberService memberService;

	public static void main(String[] args) {
		SpringApplication.run(BidApplication.class, args);
	}

//	@EventListener(ApplicationReadyEvent.class)
//	public void init() {
//		memberService.signUp(SignUpDto.Request.builder()
//				.loginId("testlogin1")
//				.password("1234567890")
//				.name("홍미누")
//				.email("kongminoo@naver.com")
//				.nickname("철수")
//				.phoneNumber("010-1234-5678")
//				.emailVerified(true)
//				.address(Address.builder()
//						.city("seoul")
//						.street("saemalo")
//						.zipcode("548")
//						.build())
//				.build());
//
//		memberService.signUp(SignUpDto.Request.builder()
//				.loginId("testlogin2")
//				.password("1234567890")
//				.name("홍미누")
//				.email("kongminoo@naver.com")
//				.nickname("영희")
//				.phoneNumber("010-1234-5678")
//				.emailVerified(true)
//				.address(Address.builder()
//						.city("seoul")
//						.street("saemalo")
//						.zipcode("548")
//						.build())
//				.build());
//
//		memberService.signUp(SignUpDto.Request.builder()
//				.loginId("testlogin3")
//				.password("1234567890")
//				.name("홍미누")
//				.email("kongminoo@naver.com")
//				.nickname("예원")
//				.phoneNumber("010-1234-5678")
//				.emailVerified(true)
//				.address(Address.builder()
//						.city("seoul")
//						.street("saemalo")
//						.zipcode("548")
//						.build())
//				.build());
//
//		memberService.signUp(SignUpDto.Request.builder()
//				.loginId("testlogin4")
//				.password("1234567890")
//				.name("홍미누")
//				.email("kongminoo@naver.com")
//				.nickname("민우")
//				.phoneNumber("010-1234-5678")
//				.emailVerified(true)
//				.address(Address.builder()
//						.city("seoul")
//						.street("saemalo")
//						.zipcode("548")
//						.build())
//				.build());
//
//		memberService.signUp(SignUpDto.Request.builder()
//				.loginId("testlogin5")
//				.password("1234567890")
//				.name("홍미누")
//				.email("kongminoo@naver.com")
//				.nickname("은비")
//				.phoneNumber("010-1234-5678")
//				.emailVerified(true)
//				.address(Address.builder()
//						.city("seoul")
//						.street("saemalo")
//						.zipcode("548")
//						.build())
//				.build());
//
//	}

}


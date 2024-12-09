package com.auction.bid.global.security.jwt.userdetails;

import com.auction.bid.domain.member.Member;
import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Member findMember = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_EMAIL));

        return new CustomUserDetails(findMember);
    }

}

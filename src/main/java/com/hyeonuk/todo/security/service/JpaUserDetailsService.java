package com.hyeonuk.todo.security.service;

import com.hyeonuk.todo.member.entity.Member;
import com.hyeonuk.todo.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재하지 않습니다."));

        return new CustomUserDetail(member);
    }
}

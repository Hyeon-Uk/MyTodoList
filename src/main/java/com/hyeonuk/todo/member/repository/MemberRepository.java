package com.hyeonuk.todo.member.repository;

import com.hyeonuk.todo.member.entity.Member;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,String> {
    Optional<Member> findByEmail(String email);
}

package com.hyeonuk.todo.todo.repository;

import com.hyeonuk.todo.member.entity.Member;
import com.hyeonuk.todo.todo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category,Long> {
    List<Category> findByMember(Member member);
}

package com.hyeonuk.todo.todo.entity;

import com.hyeonuk.todo.integ.entity.BaseEntity;
import com.hyeonuk.todo.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Todo extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="todo_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional=true)
    @JoinColumn(name="category_id")
    private Category category;

    @Column(name="content",length = 200)
    private String content;

    @Column(name="complete")
    private boolean complete;
}

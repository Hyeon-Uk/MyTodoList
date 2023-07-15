package com.hyeonuk.todo.todo.entity;

import com.hyeonuk.todo.integ.entity.BaseEntity;
import com.hyeonuk.todo.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Category extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="category_id")
    private Long id;

    @Column(name="title",length = 100)
    private String title;

    @ManyToOne
    @JoinColumn(name="member_id")
    private Member member;
}

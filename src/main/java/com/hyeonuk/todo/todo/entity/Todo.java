package com.hyeonuk.todo.todo.entity;

import com.hyeonuk.todo.integ.entity.BaseEntity;
import com.hyeonuk.todo.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@ToString
public class Todo extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="todo_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category_id")
    private Category category;

    @Column(name="content",length = 200,nullable = false)
    private String content;

    @Column(name="complete")
    private boolean complete;

    //변경된 상태를 return
    public boolean toggleComplete(){
        return this.complete = !this.complete;
    }

    public void updateContent(String content){
        //null혹은 공백으로 채워진 content라면 return
        if(content == null || content.trim().equals("")){
            return;
        }
        this.content=content;
    }

    public void updateCategory(Category category){
        //카테고리가 해당todo의 member id와 같은지 비교 후 아니면 return
        if(category!=null && category.getMember().getId() != this.getMember().getId()){
            return;
        }
        this.category = category;
    }
}

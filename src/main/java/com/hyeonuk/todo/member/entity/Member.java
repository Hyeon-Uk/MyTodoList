package com.hyeonuk.todo.member.entity;

import com.hyeonuk.todo.integ.data.MemberColumnLength;
import com.hyeonuk.todo.integ.data.MEMBER_MAX_LENGTH;
import com.hyeonuk.todo.integ.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Member extends BaseEntity {
    @Id
    @Column(name="id")
    @MemberColumnLength(length = MEMBER_MAX_LENGTH.ID)
    private String id;

    public void changeEmail(String email){
        this.email=email;
    }

    @Column(name="email",unique = true,nullable = false)
    @MemberColumnLength(length = MEMBER_MAX_LENGTH.EMAIL)
    private String email;

    @Column(name="password",nullable=false)
    @MemberColumnLength(length = MEMBER_MAX_LENGTH.PASSWORD)
    private String password;

    @Column(name="name",nullable = false)
    @MemberColumnLength(length = MEMBER_MAX_LENGTH.NAME)
    private String name;

    @Column(name="img")
    @MemberColumnLength(length = MEMBER_MAX_LENGTH.IMG)
    private String img;

    @Column(name="description")
    @MemberColumnLength(length = MEMBER_MAX_LENGTH.DESC)
    private String description;

    //secret
    @Column(name="try_count")
    @ColumnDefault("0")
    private int tryCount;

    @Column(name="blocked_time")
    private LocalDateTime blockedTime;

    @PreUpdate
    @PrePersist
    public void validationLength(){
        for(Field field : this.getClass().getDeclaredFields()){
            MemberColumnLength memberColumnLength = field.getAnnotation(MemberColumnLength.class);
            if(memberColumnLength != null) {
                MEMBER_MAX_LENGTH maxLength = memberColumnLength.length();
                field.setAccessible(true);

                try {
                    String value = (String) field.get(this);
                    if (value != null && value.length() > maxLength.getValue()) {
                        throw new DataIntegrityViolationException("Field " + field.getName() + " exceeds maximum length of " + maxLength.getValue());
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

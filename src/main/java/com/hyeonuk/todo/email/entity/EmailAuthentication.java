package com.hyeonuk.todo.email.entity;

import com.hyeonuk.todo.integ.entity.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
@RedisHash(timeToLive = 60*3)//이메일에 대한 인증 정보가 3분동안 지속
public class EmailAuthentication{
    @Id
    private String email;//이메일을 키값으로 저장

    private String code;//코드를value로 저장

    private LocalDateTime createdAt;
}

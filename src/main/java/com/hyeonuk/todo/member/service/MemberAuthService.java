package com.hyeonuk.todo.member.service;

import com.hyeonuk.todo.integ.exception.AlreadyExistException;
import com.hyeonuk.todo.member.dto.SaveDTO;
import com.hyeonuk.todo.member.entity.Member;
import com.hyeonuk.todo.member.exception.SaveException;

public interface MemberAuthService {
    SaveDTO.Response save(SaveDTO.Request dto) throws SaveException, AlreadyExistException;

    default SaveDTO.Response entityToSaveDTO(Member member){
        return SaveDTO.Response.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .build();
    }

    default Member saveDTOToEntity(SaveDTO.Request dto){
        return Member.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .build();
    }
}

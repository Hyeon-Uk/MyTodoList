package com.hyeonuk.todo.member.service;

import com.hyeonuk.todo.integ.data.MEMBER_MAX_LENGTH;
import com.hyeonuk.todo.integ.exception.AlreadyExistException;
import com.hyeonuk.todo.integ.exception.ValidationException;
import com.hyeonuk.todo.member.dto.SaveDTO;
import com.hyeonuk.todo.member.entity.Member;
import com.hyeonuk.todo.member.exception.SaveException;
import com.hyeonuk.todo.member.repository.MemberRepository;
import com.hyeonuk.todo.integ.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberAuthServiceImpl implements MemberAuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final String ID_REGEX = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{5,"+MEMBER_MAX_LENGTH.ID.getValue()+"}$";
    private final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private final String PW_REGEX ="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z\\d!@#$%^&*]{8,16}$";



    //정규식이 빠를까? 아니면 for문이 빠를까?
    private void saveDtoValidation(SaveDTO.Request dto) throws ValidationException {
        if (StringUtils.isBlank(dto.getId())
                || StringUtils.isBlank(dto.getEmail())
                || StringUtils.isBlank(dto.getName())
                || StringUtils.isBlank(dto.getPassword())
                || StringUtils.isBlank(dto.getPasswordCheck())) throw new ValidationException("입력값을 다시 확인해주세요");

        //아이디 : 5~20자의 영문 소문자, 대문자, 숫자만 사용이 가능합니다.(특수문자 넣으면 에러)
        if(!Pattern.matches(ID_REGEX,dto.getId())) throw new ValidationException("아이디는 5~20자의 영문 대소문자, 숫자로 이루어져야 합니다.");

        //이름 : 50자 이하의 이름을 입력해야함
        if(dto.getName().length() > MEMBER_MAX_LENGTH.NAME.getValue()) throw new ValidationException("이름은 50자 이하여야 합니다.");

        //이메일 : 이메일 형식
        if(!Pattern.matches(EMAIL_REGEX,dto.getEmail())) throw new ValidationException("유효한 이메일 형식이 아닙니다.");

        //비밀번호 : 8~16자의 소문자, 대문자, 숫자, 특수문자로 구성되어야 합니다.
        if(!Pattern.matches(PW_REGEX,dto.getPassword())) throw new ValidationException("비밀번호는 8~16자의 소문자, 대문자, 숫자, 특수문자로 이루어져야 합니다.");

        //비밀번호 확인 : 위에 입력한 비밀번호와 일치해야함
        if(!dto.getPassword().equals(dto.getPasswordCheck())) throw new ValidationException("비밀번호가 일치하지 않습니다.");
    }

    @Override
    @Transactional
    public SaveDTO.Response save(SaveDTO.Request dto) throws SaveException, AlreadyExistException, ValidationException {
        try {
            //약관동의 안했으면 throw
            if(!dto.isAgree()){
                throw new ValidationException("약관에 동의해주세요");
            }

            //입력 아이디, 이메일, 비밀번호, 비밀번호 확인, 약관동의 validation 진행
            saveDtoValidation(dto);

            //아이디와 이메일 중복 검사
            Optional<Member> byId = memberRepository.findById(dto.getId());
            if (byId.isPresent()) throw new AlreadyExistException("존재하는 아이디입니다.");

            Optional<Member> byEmail = memberRepository.findByEmail(dto.getEmail());
            if (byEmail.isPresent()) throw new AlreadyExistException("존재하는 이메일입니다.");

//            비밀번호를 encoding
            dto.setPassword(passwordEncoder.encode(dto.getPassword()));

            //최종적으로 save함
            return entityToSaveDTO(memberRepository.save(saveDTOToEntity(dto)));
        }catch(AlreadyExistException | ValidationException e){//Already와 Validation 예외는 그냥 던져줌
            throw e;
        }catch(Exception e){//나머지 예외는 SaveException으로 감싸서 throw
            throw new SaveException("회원가입 도중 오류가 발생했습니다.");
        }
    }

}

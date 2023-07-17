# ERD

![image](https://github.com/Hyeon-Uk/MyTodoList/assets/43038815/90e785a2-c2d0-4240-b773-3bac6f4cb9b9)
# Column 요약

### Member
- try_count : 로그인 시도 횟수.
- blocked_time : 계정의 block이 해제되는 시간
- description : 자기소개
- email : 이메일
- id : 사용자의 id
- img : 사용자의 profile src
- name : 사용자의 이름
- password : 비밀번호

### Authority
- name : 권한의 이름

### Todo
- complete : 완료 여부
- targetDate : 목표 날짜가 존재하면 설정
- content : todo의 내용

### category
- title : category의 타이틀

# 연관 관계 매핑
## Authority : Member = N : 1
- 여러가지 권한은 한 Member에 포함될 수 있기 때문에 N : 1
- 양방향 매핑으로 구현 ( 단방향 매핑으로 서로 참조할 수 있도록 )

## Todo : Member =  N : 1
- 여러가지 Todo는 한 Member에 포함될 수 있기 때문에 N : 1
- 단방향 매핑으로 구현

## Category : Member = N : 1
- 여러가지 Category는 한 Member의 소유가 될 수 있기때문에 N : 1
- 단방향 매핑으로 구현

## Todo : Category = 1 : N
- 여러개의 Todo는 한개의 Category에 포함될 수 있기때문에 N : 1
- 단방향 매핑으로 구현
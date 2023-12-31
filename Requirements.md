# 기능 / 비기능 요구사항 정리

## 🔨 기능 요구사항   

- 유저
  - <span id='login'>로그인</span>
    - 아이디입력, 비밀번호 입력, 비밀번호 찾기 링크, 회원가입 링크, 로그인버튼이 존재하는 페이지를 제공해야함
    - 비밀번호 찾기 링크를 클릭하면 <a href='#findPass'>비밀번호 찾기 서비스</a>를 제공하는 페이지를 제공해야함
    - 회원가입 링크를 클릭하면 <a href='#join'>회원가입 서비스</a>를 제공하는 페이지를 제공해야함
    - 아이디 혹은 비밀번호가 일치하지 않다면 "회원 정보가 일치하지 않습니다. 로그인 3회 실패 시 계정이 잠기게됩니다." 메세지를 출력해야함
    - 로그인 성공
      - 이메일 인증이 되지 않은 계정이라면 이메일인증이 되지않았다는 페이지와 인증메일 다시보내기 버튼이 존재하는 페이지로 이동
      - 이메일 인증이 된 계정이면 세션에 인증정보 저장 후 <a href='#main'>메인화면</a>으로 이동
  - <span id='join'>회원가입</span>
    - 회원가입 이용약관을 체크하는 Form이 존재해야함
    - 아이디 입력, 이메일 입력, 이름 입력, 비밀번호 입력, 비밀번호 확인 입력, 취소버튼, 가입하기 버튼이 존재하는 회원가입 페이지 제공
    - 아이디 : 5~20자의 영문 소문자, 대문자, 숫자만 사용이 가능합니다. 이미 존재하는 아이디일시 가입 불가
    - 이름 : 1~50자로 입력해야 합니다.
    - 이메일 : 가입되지않은 이메일이면서 존재하는 이메일로 가입해야합니다. (이메일로 소식 발송을 위함)
    - 비밀번호 : 8~16자의 소문자, 대문자, 숫자, 특수문자로 구성되어야 합니다. (모두 한번이상 포함해야함)
    - 비밀번호 확인 : 위에 입력한 비밀번호와 일치해야함
    - 취소버튼 : 로그인화면으로 이동해야함
    - 가입하기 버튼 : 약관 동의, 아이디, 이메일, 비밀번호, 비밀번호 확인의 Validation 후 서버로 가입 정보 전송. 서버측에서도 유효성 검사 필수
      - 가입 성공시 성공 안내문구와 함께 <a href='#login'>로그인화면</a>으로 이동
      - 가입 실패시 실패 안내 문구를 출력
  - <span id="main">메인화면</span>
    - 로그인된 사용자만 진입할 수 있음
    - 로그인된 사용자의 이름, 해당월에 해당하는 todo List들을 일별/카테고리별 그룹핑하여 데이터를 전달받음
    - 클라이언트에서는 사용자의 이름을 노출시킴
    - 달력에 이번달 달력을 만들어 출력시킨 뒤, 해당 일에 몇개의 투두리스트를 완료했는지 체크
    - 달력의 해당 날짜를 클릭하면 해당 날짜의 Todo가 출력됨
    - 이름 아래에는 선택된 날짜(default =  당일)에 해당하는 todoList가 그룹별로 존재함
    - 이름 아래 카테고리 추가 버튼을 누르면 카테고리를 추가할 수 있음
    - 카테고리 옆 + 버튼을 누르면 해당 카테고리에 Todo를 작성할 수 있음
    - 카테고리 옆 더보기 버튼을 누르면 삭제/수정 버튼이 나옴
      - 삭제 버튼을 클릭 시 카테고리가 삭제됨.
      - 카테고리를 삭제하며 하위의 Todo들은 모두 삭제됨
      - 수정 버튼 클릭 시 해당 카테고리 이름 수정할 Input칸이 활성화됨
      - 확인 혹은 엔터 클릭시 카테고리 이름 수정
    - Todo 옆 더보기 버튼을 누르면 삭제/수정 버튼이 나옴
      - 삭제 버튼을 클릭 시 해당 TODO가 삭제됨
      - 수정 버튼을 클릭 시 해당 TODO의 INPUT태그가 활성화 되며 수정 가능, 수정하기 버튼 생성
      - 수정하기 버튼 생성 or 엔터 클릭 시 해당 Todo가 수정됨

## ⛏ 비기능 요구사항
- 공통
  - XSS Filtering
    - 모든 입력값에 XSS Filtering 적용
    - 모든 출력값에 XSS Filtering 적용
- 유저
  - <span id='login_nf'>로그인</span>
    - 로그인 3회 실패 시
      - 해당 계정을 3분간 Block 및 가입된 계정으로 비정상적인 접근 감지 이메일 발송
      - 서버측 로그에 Block당하기 전 마지막 접근 IP주소를 남김
  - <span id='join_nf'>회원가입</span>
    - 회원가입 성공 시 입력받은 비밀번호를 BcrypPasswordEncoder를 이용하여 저장
    - Try Count(로그인 시도 횟수)를 0, BlockTime를 null로 초기값 셋팅
    - 위의 모든 과정을 한 트랜잭션안에서 실행해야함. 하나라도 실패시 회원가입 실패(Rollback 시킴)
- 입력값 Validation
  - 컨트롤러 레이어에서 받아올 때 뿐만 아니라 Service레이어에서도 함께 Validation진행
  - 파라미터를 통해 넘겨주기 전에 데이터의 변형이 일어나서 이전 Validation의 결과가 무의미해질 수 있기 때문에
- 수정/삭제 시 유저 인증정보 확인
  - 해당 Resource가 현재 행위를 진행하고 있는 유저 소유의 Resource인지 확인하는 Secure code 무조건 적용
- Exception 세분화
  - 무책임한 Exception.class Throw를 하지 않고, Custom Checked Exception을 상황에 맞게 생성하여 해당 Exception에 맞는 복구코드가 존재한다면 복구.

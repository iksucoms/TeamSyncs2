# 🔄 TeamSync

> **팀 단위 협업을 효율적으로 관리하기 위한 웹 기반 협업 플랫폼**

<br>

## 📌 프로젝트 소개

TeamSync는 팀원 간 실시간 소통, 일정 관리, 업무 추적, 파일 관리를 하나의 플랫폼에서 제공하는 웹 기반 협업 서비스입니다.
팀을 생성하거나 초대 코드로 참여할 수 있으며, 역할 기반 권한 관리를 통해 체계적인 팀 운영이 가능합니다.

<br>

## 👥 팀 구성

| 이름 | 담당 기능 |
|------|-----------|
| 팀원 1 | 채팅 / 보관함 |
| 팀원 2 | 캘린더 / 일정 |
| 팀원 3 | 칸반 보드 |
| 팀원 4 | 공지사항 / 알림 |
| 팀원 5 | 회의록 / 대시보드 |
| 팀원 6 | 회원 / 팀 관리 |

<br>

## 📅 개발 기간

**2026.06.10 ~ 2026.07.28**

<br>

## 🛠 기술 스택

### Backend
![Java](https://img.shields.io/badge/Java-007396?style=flat&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat&logo=springboot&logoColor=white)
![MyBatis](https://img.shields.io/badge/MyBatis-000000?style=flat&logoColor=white)
![Oracle](https://img.shields.io/badge/Oracle_21c-F80000?style=flat&logo=oracle&logoColor=white)

### Frontend
![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=flat&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=flat&logo=css3&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=flat&logo=javascript&logoColor=black)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=flat&logo=thymeleaf&logoColor=white)

### 실시간 통신
![WebSocket](https://img.shields.io/badge/WebSocket-STOMP/SockJS-brightgreen?style=flat)

### 보안
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat&logo=springsecurity&logoColor=white)

### 도구
![Git](https://img.shields.io/badge/Git-F05032?style=flat&logo=git&logoColor=white)
![DBeaver](https://img.shields.io/badge/DBeaver-382923?style=flat&logo=dbeaver&logoColor=white)

<br>

## 📋 주요 기능

### 💬 채팅
- **실시간 메시지 송수신** : WebSocket(STOMP/SockJS) 기반 채널별 실시간 통신
- **채널 생성/삭제** : 팀장·매니저 권한 기반 채널 관리, 기본 채널 삭제 방지
- **파일/이미지 첨부** : Spring MultipartFile 기반 업로드, 썸네일 미리보기 및 다운로드
- **답글(인용)** : 특정 메시지에 답글, 클릭 시 원본 메시지로 스크롤 이동
- **@멘션 자동완성** : 팀원 목록 기반 자동완성 팝업, HashSet 기반 중복 멘션 방지
- **안읽은 메시지 수 표시** : 채널별 실시간 뱃지 표시, 입장 시 읽음 처리
- **백그라운드 구독** : 페이지 로드 시 전체 채널 구독으로 다른 채널 메시지도 실시간 감지

### 🗂 보관함
- **계층형 폴더 구조** : Oracle CONNECT BY 계층 쿼리 기반 하위 폴더 지원
- **채팅방 폴더 자동 연동** : 채팅방 생성 시 동일 이름 폴더 자동 생성 (CHANNEL_NUM 기반 정확한 매핑)
- **파일 직접 업로드** : 보관함에서 직접 파일 업로드 (FILE_SOURCE='DRIVE')
- **이미지/파일 탭 구분** : FILE_TYPE 기반 탭 필터링
- **파일 다운로드/삭제** : 서버 파일 + DB 동시 삭제
- **채팅방 연동 폴더 보호** : IS_CHAT_FOLDER 컬럼으로 수동 삭제 방지

### 📅 캘린더 / 일정
- 팀 일정 생성·수정·삭제
- FullCalendar.js 기반 월간/주간/일간 뷰
- 칸반 보드 마감일 연동

### 📌 칸반 보드
- 할 일 / 진행 중 / 검토 중 / 완료 4단계 컬럼
- Drag & Drop 카드 이동
- 담당자 지정, 체크리스트, 댓글

### 📢 공지사항 / 알림
- 팀장·매니저 공지 작성 및 상단 고정
- Ajax Polling 기반 실시간 알림
- 알림 유형별 아이콘 구분

### 📊 대시보드
- Chart.js 기반 팀 활동 통계 시각화
- 월별 일정 수 / 팀원별 완료 카드 수 / 채널별 메시지 비율

### 🤖 AI 챗봇
- GROQ LLM API 기반 자연어 대화
- RAG(Retrieval-Augmented Generation) 기반 팀 데이터 조회

### 👤 회원 / 팀 관리
- 이메일 회원가입 / Google OAuth 2.0 소셜 로그인
- 초대 코드 / 이메일 초대 기반 팀 참여
- 팀장 / 매니저 / 팀원 역할 기반 권한 관리

<br>

## 🗄 데이터베이스 구조 (채팅/보관함)

```
CHAT_CHANNEL      채팅 채널 (is_default로 기본 채널 구분)
CHAT_MESSAGE      메시지 (parent_message_num 자기참조 → 답글)
CHAT_FILE         첨부 파일 (file_source로 채팅/드라이브 구분)
CHAT_READ_STATUS  채널별 읽음 상태 (last_read_message_num)
CHAT_MENTION      @멘션 기록
FILE_FOLDER       보관함 폴더 (parent_folder_num 자기참조 → 계층 구조)
```

<br>

## 🔥 기술적 도전 & 해결

### 1. 다중 채널 실시간 알림
**문제** : 채널 클릭 시에만 WebSocket을 구독하면 다른 채널 메시지를 놓침  
**해결** : 페이지 로드 시 모든 채널을 백그라운드 구독, `currentChannelId` 전역 변수로 현재 채널 추적하여 분기 처리

### 2. 보관함 폴더 매핑 오류
**문제** : 채팅방 이름으로 폴더를 매핑하면 같은 이름의 폴더가 여러 팀에 존재할 때 충돌  
**해결** : `FILE_FOLDER` 테이블에 `CHANNEL_NUM` 컬럼 추가, 이름 대신 고유 번호로 정확히 매핑

### 3. 채팅방 삭제 시 FK 제약 오류
**문제** : `file_folder` 자기참조 FK로 인해 하위 폴더가 있으면 부모 폴더 삭제 불가  
**해결** : Oracle `CONNECT BY`로 하위 폴더 전체 조회 후 파일 → 하위 폴더 → 부모 폴더 순서로 연쇄 삭제

### 4. Oracle NULL 타입 오류
**문제** : MyBatis에서 null 값 INSERT 시 `ORA-17004` 오류  
**해결** : `#{column, jdbcType=NUMERIC}` / `#{column, jdbcType=CHAR}` 명시

<br>

## 🚀 실행 방법

```bash
# 1. 레포지토리 클론
git clone https://github.com/iksucoms/TeamSyncs2.git

# 2. Oracle DB 설정
# application.properties에서 DB 연결 정보 수정
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:xe
spring.datasource.username=your_username
spring.datasource.password=your_password

# 3. DDL 실행
# /src/main/resources/sql/ 폴더의 SQL 파일 순서대로 실행

# 4. 서버 실행
./mvnw spring-boot:run
```

<br>

## 📁 프로젝트 구조

```
src/main/java/kr/spring/
├── chat/
│   ├── controller/    # ChatRestController, ChatPubSubController
│   ├── service/       # ChatService, ChatServiceImpl
│   ├── dao/           # ChatMapper
│   └── vo/            # ChatChannelVO, ChatMessageVO, ChatFileVO ...
├── storage/
│   ├── controller/    # StorageController
│   ├── service/       # StorageService, StorageServiceImpl
│   ├── dao/           # StorageMapper
│   └── vo/            # FileFolderVO
└── ...

src/main/resources/
├── templates/thviews/
│   ├── chat/          # list.html
│   └── storage/       # list.html
├── static/assets/
│   ├── css/chat/      # chatList.css
│   ├── css/storage/   # storage.css
│   ├── js/chat/       # chat.list.js
│   └── js/storage/    # storage.js
└── mapper/            # ChatMapper.xml, StorageMapper.xml
```

<br>

## 📸 화면 구성

| 채팅 | 보관함 |
|------|--------|
| ![chat](이미지경로) | ![storage](이미지경로) |

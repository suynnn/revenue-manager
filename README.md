# 📝 프로젝트 소개
### 소개
스트리밍 서비스의 크리에이터 정산 페이지 서비스 프로젝트 입니다. <br/> <br/>
크리에이터들이 이용할 수 있는 정산 및 통계 기능을 구현하였고, 크리에이터는 영상별 조회수, 재생 시간, 광고 종류, 광고 재생 횟수 등의 데이터를 바탕으로 정산을 수행할 수 있습니다. <br/> <br/>
크리에이터가 자신의 콘텐츠 성과를 쉽게 파악하고 수익을 효율적으로 관리할 수 있도록 돕는 것이 프로젝트의 목표입니다. <br/> <br/>

### 진행 기간
2024년 10월 16일 ~  <br/> <br/>

# ⚙️ 아키텍처
## ERD
![image](https://github.com/user-attachments/assets/88c8e214-98bf-4278-990c-5b9986922813)


<br />

# 📋 API 문서
[API 문서](https://colorful-math-aeb.notion.site/API-123ea62dd71780f5a56fc633106833e2?pvs=4) <br /> <br />

# 🛠️ 기술 스택
<img src="https://img.shields.io/badge/java 21-007396?style=for-the-badge&logo=OpenJDK&logoColor=white">

<img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">

<img src="https://img.shields.io/badge/spring batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white">

- 스프링 프로젝트와의 호환성이 높고, 대용량 데이터 처리에 적합하여 사용

<img src="https://img.shields.io/badge/Spring Security-6DB33F?style=for-the-badge&logo=Spring Security&logoColor=white">

- 스프링 생태계에서 보안에 필요한 기능들을 제공하므로 사용

<img src="https://img.shields.io/badge/spring data jpa-6DB33F?style=for-the-badge&logo=spring&logoColor=white">

- SQL 중심 개발이 아닌 객체 지향 원칙을 따르는 개발이 가능하므로 사용

<img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=Redis&logoColor=white"> 

- 빠른 데이터 접근과 일시적 데이터 저장에 적합해서 사용

<img src="https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white"> 

- Docker Compose를 작성해서 Redis 등 필요한 데이터베이스를 손쉽게 실행하고 관리

<br />


# 🧑‍💻 기술적 도전
- [스프링 배치로 대용량 시청 로그 데이터들 정산](https://colorful-math-aeb.notion.site/13fea62dd71780769512fbb90a584b84?pvs=4)

<br />

# ⌨️ 성능 최적화
- [스프링 배치 성능 개선을 위한 리팩토링](https://colorful-math-aeb.notion.site/13fea62dd7178035b29ff74fecade712?pvs=4) <br />
- [스프링 배치 파티셔닝으로 44% 성능 개선](https://colorful-math-aeb.notion.site/95-13fea62dd71780e38332e0075ecd09c9?pvs=4)

<br />

# 🚧 트러블 슈팅
- [파티셔닝 작업 중 데드락 발생](https://colorful-math-aeb.notion.site/13bea62dd7178043a38ad593cb3edcd6?pvs=4)


<br />

# 🕶️ 주요 기능
### 유저
- 스프링 시큐리티 및 oauth2(구글), JWT AccessToken을 사용하여 로그인, 로그아웃 구현
### 영상 스트리밍
- 레디스를 활용한 폴링 방식으로 영상 시청 시 레디스에 주기적으로 로그 데이터 저장
  - 유저가 영상 시청 중단하면, 스케줄러를 통해 레디스에 쌓인 데이터를 DB에 저장
- 영상 업로더, 중복된 아이디 및 IP를 통한 영상 시청 어뷰징 방지
### 통계 및 정산
- 전날 시청 로그 데이터를 기반으로 하루에 한 번 재생 시간, 조회수, 광고 조회수 통계 집계
- 집계된 통계 데이터를 바탕으로 조회수와 광고 조회수에 따른 금액 정산
- 조회수 및 재생 시간 상위 5개 영상 조회 API 구현
- 특정 영상의 1일, 1주, 1개월 정산 금액 조회 API 구현

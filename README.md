## MyPetLog-Backend
### 🐶🐱🐹🐰🐤🦎
<a target='_blank'><img width="400" height="400" src='https://github.com/ppp-team/my-pet-log/blob/develop/public/images/onboarding1.png' border='0'></a>

### 가족, 친구들과 함께 쓰는 반려동물 기록장
<br>
마이펫로그는 가족, 친구들과 함께 반려동물의 일상과 건강상태를 기록하고 공유하는 서비스입니다. <br>
강아지 고양이 뿐만 아니라 햄스터, 물고기, 도마뱀 등 다양한 반려동물을 위한 서비스입니다. <br>
해피가 어디로 산책을 다녀왔는지, 햄토리가 몇 시에 해바라기씨를 먹었는지 기록하고 공유해보세요! <br>

#### Team
**FE**
| 김선혜 | 손지은 | 주소희 | 신혜윤 | 이슬 |
</br>
**BE**
| 송원선 | 김주현 |
</br>
**Design**
| 허윤지 |

### Tech Stack
<img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=for-the-social&logo=Spring Boot&logoColor=white"> <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-social&logo=Gradle&logoColor=white"> <img src="https://img.shields.io/badge/Spring Data JPA-6DB33F?style=for-the-social&logo=Databricks&logoColor=white">
<br/>
<img src="https://img.shields.io/badge/Spring Security-6DB33F?style=for-the-social&logo=springsecurity&logoColor=white"> <img src="https://img.shields.io/badge/JSON Web Tokens-000000?style=for-the-social&logo=JSON Web Tokens&logoColor=white">
<br/>
<img src="https://img.shields.io/badge/MySQL-4479A1.svg?style=for-the-social&logo=MySQL&logoColor=white"> <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-social&logo=Redis&logoColor=white"> <img src="https://img.shields.io/badge/-Elasticsearch-005571?style=for-the-social&logo=elasticsearch&logoColor=white">
<br/>
<img src="https://img.shields.io/badge/JUnit5-25A162?style=for-the-social&logo=junit5&logoColor=white"> <img src="https://img.shields.io/badge/ Swagger-6DB33F?style=for-the-social&logo=swagger&logoColor=white">

- java 17
- Gradle 8.5
- Spring Boot 3.1.8

### Project Architecture
<img src="https://github.com/user-attachments/assets/8ecc95d1-3e14-45f1-978d-d30d5d603c6b" width="300" height="300">

- develop 브랜치의 코드가 git action 에 의해 개발 서버에 지속적 통합됩니다.

### Directory Structure

```
 📂 petlog-server
 ┣ 📂 petlog-api
 ┃ ┣ 📂src
 ┃ ┃  ┗ 📂main
 ┃ ┃    ┗ 📂resources
 ┃ ┃       ┣ 📜application-dev.yml
 ┃ ┃       ┗ 📜application-local.yml
 ┃ ┣ 📜Dockerfile
 ┃ ┗ 📜build.gradle.kts
 ┃ ┗ 📂test
 ┣ 📂 petlog-api
 ┃ ┣ 📂src
 ┃ ┃  ┗ 📂main
 ┃ ┃    ┗ 📂resources
 ┃ ┃       ┣ 📜application-dev.yml
 ┃ ┃       ┗ 📜application-local.yml
 ┃ ┣ 📜Dockerfile
 ┃ ┗ 📜build.gradle.kts
 ┃ ┗ 📂test
 ┣ 📂 petlog-common
 ┃  ┗ 📂src
 ┃  ┗ 📜build.gradle.kts
 ┣ 📂 petlog-domain
 ┃  ┗ 📂src
 ┃  ┗ 📜build.gradle.kts
 ┣ 📜settings.gradle.kts
 ┗ 📜.gitignore
```

</br>
펫로그 프로젝트는 두 개의 서버 모듈과 두 개의 라이브러리 모듈로 구성됩니다.
</br> </br>

- `📂petlog-api` : 서버 모듈로, 서비스의 모든 RESTFul 관련 엔드포인트 및 로직을 포함합니다.
- `📂petlog-chat` : 서버 모듈로, 실시간 통신에 필요한 웹소켓 관련 엔드포인트 및 로직을 포함합니다.
- `📂petlog-domain` : 라이브러리 모듈로, 프로젝트 전반에 사용되는 Entity 와 Repository 를 포함합니다.
- `📂petlog-common` : 라이브러리 모듈로, 여러 모듈에서 공통으로 활용하는 기능에 대한 로직을 포함합니다.
- `📂**/📜application-dev.yml` : 개발 환경에 반영될 환경변수를 세팅합니다.
- `📂**/📜application-local.yml` : 로컬 환경에 반영될 환경변수를 세팅합니다.
- `📜settings.gradle` : 하위 모듈을 선언합니다.
- `📜.gitignore` : git 에 올라가지 않아야 할 파일을 정의합니다.

### DataBase Schema

<a href='https://www.erdcloud.com/d/YdwDN5vJwLQWvNNne'><img width="700" height="400" src='https://github.com/user-attachments/assets/ef71a87a-9fa8-4726-a595-097a673731ec' border='0'></a>


### API Endpoint

| `💻Develop `                                                  |
|---------------------------------------------------------------|
| [개발 서버 REST API](http://13.124.44.0:8001/swagger-ui/index.html) | 




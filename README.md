# Backend Study

Spring Boot 기반 백엔드 기술을 학습하는 저장소입니다.

## 프로젝트 목록

| 프로젝트 | 기술 | 설명 |
|----------|------|------|
| [springdocs](#spring-rest-docs--swagger-springdocs) | Spring REST Docs, Swagger | API 문서 자동화 학습 |
| [virtual-thread-study](#virtual-thread-study) | Java 21 Virtual Thread | Virtual Thread vs Platform Thread 비교 학습 |

---

## Spring REST Docs & Swagger (springdocs)

Spring REST Docs와 Swagger(springdoc-openapi)를 통합하는 스터디 프로젝트입니다.
테스트 코드 기반으로 API 문서를 자동 생성하고, Swagger UI로 인터랙티브하게 확인할 수 있습니다.

### 기술 스택

| 항목 | 내용 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Build | Gradle |
| API 문서 (정적) | Spring REST Docs 4.0.0 + AsciiDoc |
| API 문서 (인터랙티브) | springdoc-openapi 2.8.8 (Swagger UI) |
| 테스트 | JUnit 5 + MockMvc |

### 프로젝트 구조

```
src/
├── main/
│   ├── java/com/sample/test/springdocs/
│   │   ├── SpringdocsApplication.java
│   │   └── book/
│   │       ├── BookController.java   # REST 컨트롤러
│   │       ├── BookService.java      # 비즈니스 로직 (인메모리 저장)
│   │       └── BookDto.java          # 요청/응답 DTO (record)
│   └── resources/
│       └── application.yml
├── test/
│   └── java/com/sample/test/springdocs/
│       └── book/
│           └── BookControllerDocsTest.java  # REST Docs 문서화 테스트
└── docs/asciidoc/
    └── index.adoc                           # HTML 문서 템플릿
```

### 실행 방법

#### 애플리케이션 실행

```bash
./gradlew bootRun
```

#### API 문서 생성 및 JAR 빌드

```bash
# 테스트 실행 → 스니펫 생성 → HTML 문서 생성 → JAR 패키징
./gradlew test asciidoctor bootJar
```

### 접속 URL

| 항목 | URL |
|------|-----|
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| REST Docs HTML | http://localhost:8080/docs/index.html |

> REST Docs HTML은 `./gradlew bootJar` 빌드 후 JAR 실행 시 접근 가능합니다.

### API 엔드포인트

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/books/{id}` | 도서 단건 조회 |
| GET | `/api/books?page=0&size=10` | 도서 목록 조회 |
| POST | `/api/books` | 도서 등록 |
| PUT | `/api/books/{id}` | 도서 수정 |
| DELETE | `/api/books/{id}` | 도서 삭제 |

### 문서 생성 흐름

```
1. 테스트 실행 (./gradlew test)
   └── MockMvc 테스트 → 스니펫 생성 (build/generated-snippets/)

2. AsciiDoc 변환 (./gradlew asciidoctor)
   └── index.adoc + 스니펫 → build/docs/asciidoc/index.html

3. JAR 패키징 (./gradlew bootJar)
   └── HTML 문서를 jar 내 static/docs 에 포함
```

---

## Virtual Thread Study

Java 21에서 도입된 Virtual Thread와 기존 Platform Thread를 비교하며 동시성 처리 방식의 차이를 학습하는 프로젝트입니다.

### 기술 스택

| 항목 | 내용 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Build | Gradle |
| 테스트 | JUnit 5 |

### 프로젝트 구조

```
src/
└── main/
    └── java/com/example/virtualthread/
        └── VirtualThreadStudyApplication.java
```

### 실행 방법

#### 애플리케이션 실행

```bash
./gradlew bootRun
```

#### 테스트 실행

```bash
./gradlew test
```

### 학습 내용

| 항목 | 설명 |
|------|------|
| Platform Thread | OS 스레드와 1:1 매핑되는 기존 Java 스레드 |
| Virtual Thread | JVM이 관리하는 경량 스레드 (Java 21 정식 도입) |
| 비교 포인트 | 생성 비용, 메모리 사용량, I/O 대기 시 동작 차이 |
| 적합한 사용처 | I/O 바운드 작업에서 Virtual Thread가 유리 |

# RAG 파이프라인 구현 가이드

Spring AI 2.0.0 + Google Gemini + pgvector로 구현한 RAG 파이프라인 실행 및 테스트 가이드.

---

## 기술 스택

| 항목 | 내용 |
|------|------|
| Framework | Spring Boot 4.1.0 |
| AI | Spring AI 2.0.0 |
| Chat 모델 | Google Gemini 2.5 Flash (`gemini-2.5-flash`) |
| Embedding 모델 | Google `gemini-embedding-001` (768차원) |
| Vector DB | PostgreSQL + pgvector |

---

## 프로젝트 구조

```
src/main/java/com/study/backend/aistudy/
├── config/
│   ├── ChatClientConfig.java          # ChatClient 빈 설정
│   └── GeminiEmbeddingModel.java      # 커스텀 EmbeddingModel (OpenAI 호환 API 직접 호출)
├── controller/
│   ├── IngestController.java          # POST /api/ingest - PDF 업로드
│   └── RagController.java             # POST /api/query  - 질문 응답
├── service/
│   ├── DocumentIngestionService.java  # PDF 읽기 → Chunking → Vector DB 저장
│   └── RagQueryService.java           # 유사도 검색 → 프롬프트 조합 → LLM 응답
└── dto/
    ├── QueryRequest.java
    └── QueryResponse.java
```

---

## Spring AI + Gemini 연동 구조

Spring AI 2.0.0에서 Gemini를 사용할 때 두 가지 문제가 있었고, 각각 다음과 같이 해결했다.

### Chat — OpenAI 호환 API 사용

Spring AI의 `spring-ai-starter-model-openai`에 Google의 OpenAI 호환 endpoint를 base URL로 설정한다.

```properties
spring.ai.openai.api-key=발급받은_Gemini_API_키
spring.ai.openai.base-url=https://generativelanguage.googleapis.com/v1beta/openai
spring.ai.openai.chat.options.model=gemini-2.5-flash
```

### Embedding — 커스텀 EmbeddingModel

Spring AI의 OpenAI 클라이언트는 base URL에 `/v1`을 자동으로 붙이기 때문에 Google의 embedding endpoint 경로가 꼬인다. 이를 우회하기 위해 `GeminiEmbeddingModel`에서 `RestClient`로 직접 호출한다.

```
호출 URL: https://generativelanguage.googleapis.com/v1beta/openai/embeddings
인증: Authorization: Bearer {API_KEY}
모델: gemini-embedding-001 (768차원)
```

`@Primary`로 등록해 Spring AI 자동 설정의 `OpenAiEmbeddingModel` 대신 사용된다.

---

## 환경 설정

### 1. Gemini API 키 발급 (무료)

1. [Google AI Studio](https://aistudio.google.com) 접속
2. **Get API key** → **Create API key** 클릭
3. 발급된 키를 `application.properties`의 `spring.ai.openai.api-key`에 설정

### 2. application.properties 설정

```properties
spring.ai.openai.api-key=발급받은_키_직접_입력_또는_환경변수_사용
spring.ai.openai.base-url=https://generativelanguage.googleapis.com/v1beta/openai
spring.ai.openai.chat.options.model=gemini-2.5-flash
spring.ai.openai.embedding.options.model=text-embedding-004

spring.datasource.url=jdbc:postgresql://localhost:5432/ragdb
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.ai.vectorstore.pgvector.initialize-schema=true
spring.ai.vectorstore.pgvector.dimensions=768
spring.ai.vectorstore.pgvector.distance-type=COSINE_DISTANCE
spring.ai.vectorstore.pgvector.index-type=HNSW
```

---

## 실행 방법

### 1. pgvector 실행 (Docker 필요)

```bash
docker compose up -d
```

> 최초 실행 시 `vector_store` 테이블이 자동 생성된다 (`initialize-schema=true`).

### 2. 앱 실행

```powershell
./gradlew bootRun
```

정상 실행 시 로그:
```
Started AiStudyApplication in X.XXX seconds
```

---

## API 테스트

### POST /api/ingest — PDF 문서 업로드

PDF 파일을 업로드하면 내부적으로 다음 과정이 실행된다:

```
PDF 파일 → PagePdfDocumentReader → TokenTextSplitter (청크 분할) → Embedding → pgvector 저장
```

**curl:**
```bash
curl -X POST http://localhost:8080/api/ingest \
  -F "file=@/경로/파일.pdf"
```

**PowerShell:**
```powershell
curl -X POST http://localhost:8080/api/ingest `
  -F "file=@C:\경로\파일.pdf"
```

**IntelliJ HTTP Client (`test.http`):**
```http
POST http://localhost:8080/api/ingest
Content-Type: multipart/form-data; boundary=boundary

--boundary
Content-Disposition: form-data; name="file"; filename="sample.pdf"
Content-Type: application/pdf

< C:\경로\파일.pdf
--boundary--
```

**응답 예시:**
```json
{
  "message": "문서 적재 완료",
  "filename": "sample.pdf",
  "chunkCount": 42
}
```

---

### POST /api/query — 질문하기

업로드한 문서 내용을 기반으로 질문하면, 유사한 문서 조각 5개를 검색해 Gemini가 답변한다:

```
질문 → Embedding → pgvector 유사도 검색 (Top 5) → 프롬프트 조합 → Gemini → 답변
```

**curl:**
```bash
curl -X POST http://localhost:8080/api/query \
  -H "Content-Type: application/json" \
  -d '{"question": "문서에서 궁금한 내용을 입력하세요"}'
```

**PowerShell:**
```powershell
$body = '{"question": "문서에서 궁금한 내용을 입력하세요"}'
Invoke-RestMethod -Method POST `
  -Uri http://localhost:8080/api/query `
  -ContentType "application/json" `
  -Body $body
```

**IntelliJ HTTP Client (`test.http`):**
```http
POST http://localhost:8080/api/query
Content-Type: application/json

{
  "question": "문서에서 궁금한 내용을 입력하세요"
}
```

**응답 예시:**
```json
{
  "answer": "문서에 따르면 ... (Gemini가 문서 기반으로 생성한 답변)"
}
```

> 문서에 근거가 없으면 `"문서에서 답을 찾을 수 없습니다"` 라고 응답한다.

---

## Postman으로 테스트

### 문서 업로드
- Method: `POST`
- URL: `http://localhost:8080/api/ingest`
- Body 탭 → `form-data` 선택
- Key: `file` (타입을 `File`로 변경), Value: PDF 파일 선택

### 질문
- Method: `POST`
- URL: `http://localhost:8080/api/query`
- Body 탭 → `raw` → `JSON` 선택
- Body:
```json
{
  "question": "여기에 질문 입력"
}
```

---

## 전체 흐름 요약

```
[사전 작업]
PDF 업로드 → 청크 분할(800토큰)
  → GeminiEmbeddingModel (/v1beta/openai/embeddings, gemini-embedding-001) 벡터화
  → pgvector 저장

[실시간 질문]
질문 입력 → 질문 벡터화 → pgvector 코사인 유사도 검색(Top 5)
  → 검색된 문서 조각 + 질문 → Gemini 2.5 Flash → 답변 반환
```

---

## 트러블슈팅

### Spring AI + Gemini embedding 연동 이슈

Spring AI 2.0.0 기준으로 Gemini embedding 연동 시 다음 문제들이 발생했다.

| 시도 | 문제 |
|------|------|
| `spring-ai-starter-model-google-genai` | `v1beta` 경로 하드코딩으로 `text-embedding-004` 404 발생 |
| `spring-ai-starter-model-openai` + `base-url=/v1beta/openai` | OpenAI 클라이언트가 base URL에 `/v1`을 자동 추가하여 경로 오류 |
| `GeminiEmbeddingModel` (커스텀) + Gemini 네이티브 API | `text-embedding-004`가 `embedContent` 미지원 |
| `GeminiEmbeddingModel` (커스텀) + OpenAI 호환 API | **성공** — `/v1beta/openai/embeddings` 직접 호출 |

### 회사 프록시 SSL 인터셉션 문제

Plantynet OfficeGuard CA 인증서가 HTTPS를 가로채는 환경에서 SSL 검증 실패가 발생한다.

**해결:** Java의 `cacerts`에 회사 인증서를 직접 등록한다.

```powershell
keytool -importcert -alias plantynet-ca `
  -file "C:\경로\plantynet.cer" `
  -keystore "C:\Program Files\Java\jdk-17\lib\security\cacerts" `
  -storepass changeit
```

---

## 참고

- 이론 학습: [rag-pipeline-study.md](./rag-pipeline-study.md)
- Spring AI 문서: https://docs.spring.io/spring-ai/reference/
- Google AI Studio: https://aistudio.google.com
- Google OpenAI 호환 API: https://ai.google.dev/gemini-api/docs/openai

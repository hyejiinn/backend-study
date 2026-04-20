package com.sample.test.springdocs.book;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │              Spring REST Docs 스터디 - 테스트 코드                    │
 * │                                                                     │
 * │  [STEP 1] MockMvc 테스트 실행 → 스니펫(.adoc) 자동 생성               │
 * │           build/generated-snippets/{identifier}/*.adoc              │
 * │                                                                     │
 * │  [STEP 2] AsciiDoc 문서로 조합                                       │
 * │           ./gradlew asciidoctor                                     │
 * │           → build/docs/asciidoc/index.html                         │
 * │                                                                     │
 * │  [STEP 3] Swagger UI 확인 (springdoc-openapi)                       │
 * │           ./gradlew bootRun 후                                      │
 * │           → http://localhost:8080/swagger-ui/index.html            │
 * │                                                                     │
 * │  [REST Docs vs Swagger 비교]                                        │
 * │   Spring REST Docs : 테스트 통과 시에만 문서 생성 → 정확성 보장        │
 * │   Swagger(OpenAPI) : 어노테이션 기반 자동 생성 → 인터랙티브 UI 제공    │
 * │   ✅ 두 도구를 함께 사용하면 정확성 + UI 편의성을 모두 얻을 수 있다.    │
 * └─────────────────────────────────────────────────────────────────────┘
 *
 * ─── 핵심 어노테이션 설명 ───────────────────────────────────────────────
 *
 * @ExtendWith(RestDocumentationExtension.class)
 *   JUnit 5에서 Spring REST Docs를 활성화하는 Extension.
 *   각 테스트 전후로 RestDocumentationContext를 생성/정리한다.
 *
 * @SpringBootTest(webEnvironment = MOCK)
 *   전체 애플리케이션 컨텍스트를 로드하되 실제 서버를 띄우지 않는다.
 *   MockMvc와 함께 사용한다.
 *
 *   [참고] Spring Boot 4.0에서 @WebMvcTest(슬라이스 테스트)가 제거되어
 *          @SpringBootTest를 사용한다.
 */
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class BookControllerDocsTest {

  @Autowired
  private WebApplicationContext context;

  // [개념] ObjectMapper를 직접 생성
  //   Spring Boot 4.0에서 Jackson 자동설정 구조가 변경되어
  //   테스트 컨텍스트에서 ObjectMapper Bean을 직접 주입받는 대신
  //   직접 생성하여 사용한다.
  private final ObjectMapper objectMapper = new ObjectMapper();

  private MockMvc mockMvc;

  /**
   * [개념] MockMvc에 documentationConfiguration() 적용
   *
   *   이 설정이 있어야 andDo(document(...)) 호출 시
   *   build/generated-snippets/{identifier}/ 아래에 스니펫이 생성된다.
   *
   *   기본 스니펫 출력 경로: build/generated-snippets/
   *   (build.gradle의 snippetsDir와 일치)
   */
  @BeforeEach
  void setUp(RestDocumentationContextProvider restDocumentation) {
    this.mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
        .build();
  }

  // ════════════════════════════════════════════════════════════════════
  // [STEP 1] Spring REST Docs 테스트
  //
  // 테스트 실행 후 생성되는 스니펫 파일 목록:
  //   build/generated-snippets/{identifier}/
  //     ├── http-request.adoc      실제 HTTP 요청 예시
  //     ├── http-response.adoc     실제 HTTP 응답 예시
  //     ├── curl-request.adoc      cURL 명령어 예시
  //     ├── path-parameters.adoc   경로 변수 설명 표
  //     ├── query-parameters.adoc  쿼리 파라미터 설명 표
  //     ├── request-fields.adoc    요청 Body 필드 설명 표
  //     └── response-fields.adoc   응답 Body 필드 설명 표
  // ════════════════════════════════════════════════════════════════════

  /**
   * GET /api/books/{id} - 단건 조회
   *
   * pathParameters(): URL 경로 변수({id}) 문서화
   * responseFields(): 응답 JSON 필드 문서화
   *
   * ⚠️ 주의: RestDocumentationRequestBuilders.get() 필수!
   *    MockMvcRequestBuilders.get()은 pathParameters()와 호환되지 않는다.
   *    경로 변수를 URL 템플릿({id})으로 전달해야 캡처된다.
   */
  @Test
  @DisplayName("GET /api/books/{id} - 단건 조회 문서화")
  void getBook() throws Exception {
    mockMvc.perform(
            RestDocumentationRequestBuilders.get("/api/books/{id}", 1L)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andDo(MockMvcRestDocumentation.document("book-get",
            // 경로 변수 문서화
            pathParameters(
                parameterWithName("id").description("조회할 책 ID")
            ),
            // 응답 JSON 필드 문서화
            // fieldWithPath()의 첫 번째 인자는 JSON 경로 (dot notation)
            responseFields(
                fieldWithPath("id").description("책 ID"),
                fieldWithPath("title").description("책 제목"),
                fieldWithPath("author").description("저자"),
                fieldWithPath("price").description("가격 (원)")
            )
        ));
  }

  /**
   * GET /api/books - 목록 조회
   *
   * queryParameters(): URL 쿼리 파라미터(?page=0&size=10) 문서화
   *
   * [Spring REST Docs 3.0+ 변경점]
   *   requestParameters() → queryParameters() / formParameters() 로 분리됨.
   *   Spring REST Docs 4.0에서 구 API 완전 제거.
   *
   * [배열 필드 표현]
   *   books[]     → 배열 자체
   *   books[].id  → 배열 요소의 id 필드
   */
  @Test
  @DisplayName("GET /api/books - 목록 조회 문서화")
  void getBooks() throws Exception {
    mockMvc.perform(
            RestDocumentationRequestBuilders.get("/api/books")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andDo(MockMvcRestDocumentation.document("book-list",
            // 쿼리 파라미터 문서화 (.optional() → 선택 파라미터 표시)
            queryParameters(
                parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
                parameterWithName("size").description("페이지 당 항목 수").optional()
            ),
            responseFields(
                fieldWithPath("totalCount").description("전체 책 수"),
                fieldWithPath("books[]").description("책 목록"),
                fieldWithPath("books[].id").description("책 ID"),
                fieldWithPath("books[].title").description("책 제목"),
                fieldWithPath("books[].author").description("저자"),
                fieldWithPath("books[].price").description("가격 (원)")
            )
        ));
  }

  /**
   * POST /api/books - 등록
   *
   * requestFields(): 요청 Body의 JSON 필드 문서화
   */
  @Test
  @DisplayName("POST /api/books - 등록 문서화")
  void createBook() throws Exception {
    BookDto.CreateRequest request = new BookDto.CreateRequest(
        "Refactoring", "Martin Fowler", 38000);

    mockMvc.perform(
            RestDocumentationRequestBuilders.post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andDo(MockMvcRestDocumentation.document("book-create",
            // 요청 Body 필드 문서화
            requestFields(
                fieldWithPath("title").description("책 제목"),
                fieldWithPath("author").description("저자"),
                fieldWithPath("price").description("가격 (원, 양수)")
            ),
            responseFields(
                fieldWithPath("id").description("생성된 책 ID"),
                fieldWithPath("title").description("책 제목"),
                fieldWithPath("author").description("저자"),
                fieldWithPath("price").description("가격 (원)")
            )
        ));
  }

  /**
   * PUT /api/books/{id} - 수정
   *
   * pathParameters + requestFields + responseFields 조합 예시
   */
  @Test
  @DisplayName("PUT /api/books/{id} - 수정 문서화")
  void updateBook() throws Exception {
    BookDto.UpdateRequest request = new BookDto.UpdateRequest(
        "Clean Code 2nd Edition", "Robert C. Martin", 40000);

    mockMvc.perform(
            RestDocumentationRequestBuilders.put("/api/books/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andDo(MockMvcRestDocumentation.document("book-update",
            pathParameters(
                parameterWithName("id").description("수정할 책 ID")
            ),
            requestFields(
                fieldWithPath("title").description("수정할 책 제목"),
                fieldWithPath("author").description("수정할 저자"),
                fieldWithPath("price").description("수정할 가격 (원)")
            ),
            responseFields(
                fieldWithPath("id").description("책 ID"),
                fieldWithPath("title").description("수정된 제목"),
                fieldWithPath("author").description("수정된 저자"),
                fieldWithPath("price").description("수정된 가격")
            )
        ));
  }

  /**
   * DELETE /api/books/{id} - 삭제
   *
   * 204 No Content 응답은 Body가 없으므로 responseFields 생략
   */
  @Test
  @DisplayName("DELETE /api/books/{id} - 삭제 문서화")
  void deleteBook() throws Exception {
    mockMvc.perform(
            RestDocumentationRequestBuilders.delete("/api/books/{id}", 1L))
        .andExpect(status().isNoContent())
        .andDo(MockMvcRestDocumentation.document("book-delete",
            pathParameters(
                parameterWithName("id").description("삭제할 책 ID")
            )
            // 204 No Content: 응답 Body가 없으므로 responseFields 생략
        ));
  }
}

package com.sample.test.springdocs.book;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Book REST 컨트롤러
 *
 * ─── Spring REST Docs vs Swagger 어노테이션 비교 ───────────────────────
 *
 * [Spring REST Docs]
 *   - 코드에 어노테이션을 추가하지 않는다.
 *   - 테스트 코드(BookControllerDocsTest)가 문서의 소스다.
 *   - 테스트가 통과해야만 문서가 생성 → 항상 정확한 문서 보장.
 *
 * [Swagger (springdoc-openapi)]
 *   - @Tag, @Operation, @Parameter 등의 어노테이션으로 문서를 작성한다.
 *   - 서버 실행 시 자동으로 Swagger UI 생성 → 인터랙티브한 API 테스트 가능.
 *   - 단점: 어노테이션이 실제 동작과 다를 수 있음 (테스트 검증 없음).
 *
 * ✅ 두 도구를 함께 사용하면:
 *    - 정확한 HTML 문서: ./gradlew asciidoctor → build/docs/asciidoc/index.html
 *    - 인터랙티브 UI: ./gradlew bootRun → http://localhost:8080/swagger-ui/index.html
 */
@Tag(name = "Book API", description = "책 관리 REST API")
@RestController
@RequestMapping("/api/books")
public class BookController {

  private final BookService bookService;

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  /** GET /api/books/{id} - 단건 조회 */
  @Operation(summary = "책 단건 조회", description = "ID로 특정 책 정보를 조회합니다.")
  @GetMapping("/{id}")
  public ResponseEntity<BookDto.BookResponse> getBook(
      @Parameter(description = "조회할 책 ID") @PathVariable Long id) {
    return ResponseEntity.ok(bookService.findById(id));
  }

  /** GET /api/books?page=0&size=10 - 목록 조회 */
  @Operation(summary = "책 목록 조회", description = "전체 책 목록을 조회합니다.")
  @GetMapping
  public ResponseEntity<BookDto.BookListResponse> getBooks(
      @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "페이지 당 항목 수") @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(bookService.findAll());
  }

  /** POST /api/books - 등록 */
  @Operation(summary = "책 등록", description = "새로운 책을 등록합니다.")
  @PostMapping
  public ResponseEntity<BookDto.BookResponse> createBook(
      @Valid @RequestBody BookDto.CreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(bookService.create(request));
  }

  /** PUT /api/books/{id} - 수정 */
  @Operation(summary = "책 수정", description = "기존 책 정보를 수정합니다.")
  @PutMapping("/{id}")
  public ResponseEntity<BookDto.BookResponse> updateBook(
      @Parameter(description = "수정할 책 ID") @PathVariable Long id,
      @Valid @RequestBody BookDto.UpdateRequest request) {
    return ResponseEntity.ok(bookService.update(id, request));
  }

  /** DELETE /api/books/{id} - 삭제 */
  @Operation(summary = "책 삭제", description = "책을 삭제합니다.")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBook(
      @Parameter(description = "삭제할 책 ID") @PathVariable Long id) {
    bookService.delete(id);
    return ResponseEntity.noContent().build();
  }

  // ── 예외 핸들러 ────────────────────────────────────────────────
  @ExceptionHandler(BookService.BookNotFoundException.class)
  public ResponseEntity<String> handleNotFound(BookService.BookNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
  }
}

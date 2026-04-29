package com.example.virtualthread;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Platform Thread vs Virtual Thread 비교 테스트
 *
 * Virtual Thread 핵심 특징:
 *   - JVM 내부에서 관리되는 경량 스레드 (OS 스레드 1:1 매핑 X)
 *   - 블로킹(I/O, sleep) 시 Carrier Thread를 점유하지 않고 반납 → 극소수 OS 스레드로 수백만 개 처리 가능
 *   - CPU-bound 작업에는 이점 없음
 */
public class VirtualThreadComparisonTest  {

  // -----------------------------------------------------------------------
  // 테스트 1: I/O 바운드 작업 처리 시간 비교
  // -----------------------------------------------------------------------

  @Test
  @DisplayName("I/O 바운드: Platform Thread(200 풀) vs Virtual Thread — 1000개 task")
  void compareIoBoundTask() throws InterruptedException {
    int taskCount = 1000;

    long platformMs = runWithPlatformThreadPool(taskCount, 200);
    long virtualMs  = runWithVirtualThread(taskCount);

    printResult("I/O 바운드 (1000 tasks, 50ms sleep each)", platformMs, virtualMs);

    // Virtual Thread 가 Platform Thread 보다 빠르거나 비슷해야 함
    // (풀 크기 200이면 5배 이상 배치가 생겨 Platform이 느림)
    assertThat(virtualMs).isLessThan(platformMs);
  }

  // -----------------------------------------------------------------------
  // 테스트 2: 스레드 생성 비용 비교
  // -----------------------------------------------------------------------

  @Test
  @DisplayName("스레드 생성 비용: Platform Thread vs Virtual Thread — 10,000개")
  void compareThreadCreationCost() throws InterruptedException {
    int count = 10_000;

    long platformMs = measurePlatformThreadCreation(count);
    long virtualMs  = measureVirtualThreadCreation(count);

    printResult("스레드 생성 비용 (10,000개)", platformMs, virtualMs);
  }

  // -----------------------------------------------------------------------
  // 테스트 3: Virtual Thread 특성 확인
  // -----------------------------------------------------------------------

  @Test
  @DisplayName("Virtual Thread 특성 확인 — isVirtual, 이름, carrier thread")
  void checkVirtualThreadProperties() throws InterruptedException {
    // isVirtual() 확인
    Thread vThread = Thread.ofVirtual().unstarted(() -> {});
    Thread pThread = Thread.ofPlatform().unstarted(() -> {});

    assertThat(vThread.isVirtual()).isTrue();
    assertThat(pThread.isVirtual()).isFalse();

    // 실행 중인 Virtual Thread 내부에서 carrier thread 확인
    Thread.ofVirtual().start(() -> {
      Thread current = Thread.currentThread();
      System.out.println("[Virtual Thread]");
      System.out.println("  isVirtual : " + current.isVirtual());
      System.out.println("  name      : " + current.getName());
      System.out.println("  threadId  : " + current.threadId());
    }).join();

    // Executor로 생성된 Virtual Thread 이름 패턴 확인
    AtomicInteger count = new AtomicInteger();
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      for (int i = 0; i < 3; i++) {
        executor.submit(() -> {
          System.out.println("  Executor Virtual Thread name: "
              + Thread.currentThread().getName() + " | count: " + count.incrementAndGet());
        });
      }
    }
  }

  // -----------------------------------------------------------------------
  // Helper 메서드
  // -----------------------------------------------------------------------

  /** Platform Thread 고정 풀로 N개 task(I/O 시뮬레이션) 실행 */
  private long runWithPlatformThreadPool(int taskCount, int poolSize) throws InterruptedException {
    long start = System.currentTimeMillis();
    var latch = new CountDownLatch(taskCount);

    try (var executor = Executors.newFixedThreadPool(poolSize)) {
      for (int i = 0; i < taskCount; i++) {
        executor.submit(() -> {
          simulateIo();
          latch.countDown();
        });
      }
      latch.await();
    }
    return System.currentTimeMillis() - start;
  }

  /** Virtual Thread Executor로 N개 task(I/O 시뮬레이션) 실행 */
  private long runWithVirtualThread(int taskCount) throws InterruptedException {
    long start = System.currentTimeMillis();
    var latch = new CountDownLatch(taskCount);

    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      for (int i = 0; i < taskCount; i++) {
        executor.submit(() -> {
          simulateIo();
          latch.countDown();
        });
      }
      latch.await();
    }
    return System.currentTimeMillis() - start;
  }

  /** N개 Platform Thread 생성·종료 시간 측정 */
  private long measurePlatformThreadCreation(int count) throws InterruptedException {
    long start = System.currentTimeMillis();
    var latch = new CountDownLatch(count);
    for (int i = 0; i < count; i++) {
      Thread.ofPlatform().start(latch::countDown);
    }
    latch.await();
    return System.currentTimeMillis() - start;
  }

  /** N개 Virtual Thread 생성·종료 시간 측정 */
  private long measureVirtualThreadCreation(int count) throws InterruptedException {
    long start = System.currentTimeMillis();
    var latch = new CountDownLatch(count);
    for (int i = 0; i < count; i++) {
      Thread.ofVirtual().start(latch::countDown);
    }
    latch.await();
    return System.currentTimeMillis() - start;
  }

  /** I/O 작업 시뮬레이션 (50ms blocking) */
  private void simulateIo() {
    try {
      Thread.sleep(50);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void printResult(String scenario, long platformMs, long virtualMs) {
    System.out.println("\n========== " + scenario + " ==========");
    System.out.printf("  Platform Thread : %,d ms%n", platformMs);
    System.out.printf("  Virtual Thread  : %,d ms%n", virtualMs);
    System.out.printf("  속도 차이       : %.1fx%n",
        platformMs > 0 ? (double) platformMs / virtualMs : 1.0);
  }
}

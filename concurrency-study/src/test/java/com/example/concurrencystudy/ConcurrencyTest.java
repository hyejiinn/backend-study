package com.example.concurrencystudy;

import com.example.concurrencystudy.entity.Stock;
import com.example.concurrencystudy.repository.StockRepository;
import com.example.concurrencystudy.service.PlainService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConcurrencyTest {

    @Autowired StockRepository stockRepository;
    @Autowired PlainService plainService;

    @BeforeEach
    void init() {
        stockRepository.save(Stock.builder().id(1L).qty(3000).version(0).build());
    }

    /**
     * threads개의 스레드를 도잇에 시작시키고, 각 스레드가 calls번씩 runnable 호출
     * @param threads
     * @param calls
     * @param runnable
     * @throws InterruptedException
     */
    private void run(int threads, int calls, Runnable runnable) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i<threads; i++) {
            pool.submit(() -> {
                try {
                    start.await(); // 모든 스레드 대기
                    for (int j = 0; j < calls; j++) runnable.run();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }finally {
                    done.countDown(); // 스레드 종료 시 완료 알림
                }
            });
        }

        start.countDown(); // 동시에 출발 신호
        done.await(); // 모든 스레드 끝날 때까지 대기
        pool.shutdown(); // 스레드 풀 종료
    }


    @Test
    void Z_race_without_lock() throws InterruptedException {
        // 3000 재고 중 2000 차감 시도
        run(50, 40, () -> plainService.decrease(1L, 1));

        int actual = stockRepository.findById(1L).orElseThrow().getQty();
        System.out.printf("[Race] expected = %d, actual = %d\n", 3000- 2000, actual);
        Assertions.assertThat(actual).isNotEqualTo(1000);
    }
}

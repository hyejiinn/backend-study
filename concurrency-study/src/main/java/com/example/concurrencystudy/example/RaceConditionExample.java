package com.example.concurrencystudy.example;

public class RaceConditionExample {
    static int count = 0;

    static void work() {
        for (int i = 0; i < 1000000; i++) {
            count++;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(RaceConditionExample::work);
        Thread t2 = new Thread(RaceConditionExample::work);

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("최종 count = " + count);
    }
}

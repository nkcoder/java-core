package org.nkcoder.j8.concurrency.thread;

public class ThreadExample {
  public static void main(String[] args) {
    Thread t1 = new Thread() {
      @Override
      public void run() {
        System.out.print("in a new thread.");
      }
    };

    t1.start();

  }
}

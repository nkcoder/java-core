package org.nkcoder.j8.concurrency.thread;

public class ThreadGroupExample implements Runnable {

  private static volatile boolean done = false;

  public static void main(String[] args) throws InterruptedException {
    ThreadGroup threadGroup = new ThreadGroup("readAndWriteToList-group");

    Thread t1 = new Thread(threadGroup, new ThreadGroupExample(), "t1");
    Thread t2 = new Thread(threadGroup, new ThreadGroupExample(), "t2");

    t1.start();
    t2.start();

    threadGroup.list();
    System.out.println("activeCount: " + threadGroup.activeCount());

    Thread.sleep(200);

    done = true;

  }

  @Override
  public void run() {
    while (!done) {
      String name = Thread.currentThread().getThreadGroup().getName() + ": "
          + Thread.currentThread().getName();
      System.out.println("I'm: " + name);
    }

  }
}

package org.nkcoder.j8.concurrency.thread;

public class VolatileExample {

  private volatile boolean initDone = false;

  public void initWorkDone() {
    this.initDone = true;
    System.out.println("init done.");
  }

  /**
   * start to work.
   */
  public void startToWork() {
    while (true) {
      if (initDone) {
        System.out.println("init done, start to work");
        break;
      }
    }
  }

  /**
   * entry.
   */
  public static void main(String[] args) throws InterruptedException {
    VolatileExample volatileExample = new VolatileExample();
    Thread t1 = new Thread(volatileExample::startToWork);

    Thread t2 = new Thread(volatileExample::initWorkDone);

    t1.start();
    t2.start();
    t1.join();;
    t2.join();
  }
}

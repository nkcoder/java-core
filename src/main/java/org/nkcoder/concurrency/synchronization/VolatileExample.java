package org.nkcoder.concurrency.synchronization;

/**
 * {@code volatile}: Guarantees visibility across threads (no CPU cache stale values).
 *
 * <ul>
 *   <li>Writes flush to main memory, reads always from main memory</li>
 *   <li>Does NOT provide atomicity (can't use for {@code i++})</li>
 *   <li>Use for simple flags when one thread writes, others read</li>
 * </ul>
 */
public class VolatileExample {
  private volatile boolean initDone = false;

  public void initWorkDone() {
    this.initDone = true;
    System.out.println("init done.");
  }

  public void startToWork() {
    while (true) {
      if (initDone) {
        System.out.println("init done, start to work");
        break;
      }
    }
  }

  public static void main(String[] args) throws InterruptedException {
    VolatileExample volatileExample = new VolatileExample();

    Thread t1 = new Thread(volatileExample::startToWork);
    Thread t2 = new Thread(volatileExample::initWorkDone);

    t1.start();
    t2.start();
    t1.join();
    t2.join();
  }
}

package org.nkcoder.concurrency.thread;

/**
 * Thread Interrupt: Cooperative mechanism to request thread termination.
 *
 * <ul>
 *   <li>{@code interrupt()} sets the flag (doesn't stop thread)</li>
 *   <li>{@code isInterrupted()} checks flag; {@code interrupted()} checks AND clears</li>
 *   <li>{@code sleep()}/{@code wait()}/{@code join()} throw InterruptedException and CLEAR the flag</li>
 * </ul>
 *
 * <p>Always restore interrupt status after catching InterruptedException.
 */
public class InterruptExample {
  public static void main(String[] args) throws InterruptedException {
    Thread t1 = new Thread(new MyRunnable());
    t1.start();
    Thread.sleep(3000);
    t1.interrupt();
  }

  public static class MyRunnable implements Runnable {
    @Override
    public void run() {
      while (true) {
        if (Thread.currentThread().isInterrupted()) {
          System.out.println("I'm interrupted, exit.");
          break;
        }

        try {
          Thread.sleep(2000);
        } catch (InterruptedException ex) {
          System.out.println("I'm interrupted when sleeping.");
          Thread.currentThread().interrupt();  // Restore interrupt status
        }
      }
    }
  }
}

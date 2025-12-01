package org.nkcoder.concurrency.thread;

/**
 * Daemon Thread: Background thread that doesn't prevent JVM exit.
 *
 * <ul>
 *   <li>JVM exits when only daemon threads remain</li>
 *   <li>Must call {@code setDaemon(true)} BEFORE {@code start()}</li>
 *   <li>Daemon threads are terminated abruptly (no cleanup)</li>
 * </ul>
 *
 * <p>Don't use for I/O or critical tasks that need cleanup.
 */
public class DaemonThreadExample extends Thread {

  public static void main(String[] args) {
    Thread daemonThread = new DaemonThreadExample();
    daemonThread.setDaemon(true);  // Must be BEFORE start()
    daemonThread.start();

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    // JVM exits here - daemon thread terminated abruptly
  }

  @Override
  public void run() {
    while (true) {
      System.out.println("I'm alive.");
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}

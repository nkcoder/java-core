package org.nkcoder.j8.concurrency.thread;

public class DaemonThreadExample extends Thread {

  public static void main(String[] args) {

    Thread daemonThread = new DaemonThreadExample();
    daemonThread.setDaemon(true);
    daemonThread.start();

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

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

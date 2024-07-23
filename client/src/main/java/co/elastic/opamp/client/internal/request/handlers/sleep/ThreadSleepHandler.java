package co.elastic.opamp.client.internal.request.handlers.sleep;

/** Utility to lock the polling thread between loops for a period of time. */
public interface ThreadSleepHandler {
  /**
   * If the thread is locked, release it right away. If the thread isn't locked, then ignore the
   * next call to {@link #sleep()}.
   */
  void awakeOrIgnoreNextSleep();

  /**
   * Locks the thread for a period of time or until {@link #awakeOrIgnoreNextSleep()} is called.
   *
   * @throws InterruptedException When the thread is interrupted while locked.
   */
  void sleep() throws InterruptedException;
}

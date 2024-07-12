package co.elastic.opamp.client.internal.state;

import java.util.ArrayList;
import java.util.List;

public class Observable {
  private final List<Observer> observers = new ArrayList<>();

  public synchronized void addObserver(Observer observer) {
    if (!observers.contains(observer)) {
      observers.add(observer);
    }
  }

  public synchronized void removeObserver(Observer observer) {
    observers.remove(observer);
  }

  protected synchronized void notifyObservers() {
    for (Observer observer : observers) {
      observer.update(this);
    }
  }
}

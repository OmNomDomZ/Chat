package ru.nsu.rabetskii.patternobserver;

public interface Observable {
    void notifyObservers(String message);
    void addObserver(Observer observer);
}

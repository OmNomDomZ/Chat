package ru.nsu.rabetskii.model;

import ru.nsu.rabetskii.patternobserver.Observable;
import ru.nsu.rabetskii.patternobserver.Observer;

import java.util.ArrayList;
import java.util.List;

public class ChatObservable implements Observable {
    private final List<Observer> observers = new ArrayList<>();

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }

    public void sendMessage(String message) {
        notifyObservers(message);
    }
}

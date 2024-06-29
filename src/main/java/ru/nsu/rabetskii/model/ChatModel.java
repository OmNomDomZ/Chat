package ru.nsu.rabetskii.model;

import ru.nsu.rabetskii.patternobserver.Observable;
import ru.nsu.rabetskii.patternobserver.Observer;

import java.util.ArrayList;
import java.util.List;

public class ChatModel implements Observable {
    private List<Observer> observers = new ArrayList<>();

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

    public void receiveMessage(String message) {
        notifyObservers(message);
    }
}

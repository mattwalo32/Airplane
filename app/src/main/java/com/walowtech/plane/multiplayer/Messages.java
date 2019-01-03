package com.walowtech.plane.multiplayer;

public enum Messages {
    START_GAME("START_GAME"),
    READY_TO_START("READY_TO_START"),
    START_NOW("START_NOW");

    String name;

    Messages(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

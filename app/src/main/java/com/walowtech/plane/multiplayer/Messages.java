package com.walowtech.plane.multiplayer;

/** Enum contains many messages that may be sent and received to opponents
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2019-01-07
 */
public enum Messages {
    START_GAME("START_GAME"),
    STARTING_TOP_LEFT("TL"),
    READY_TO_START("READY_TO_START"),
    START_NOW("START_NOW"),
    COLLIDED("COLLIDED"),
    BOTH_COLLIDED("BOTH_COLLIDED"),
    PLAY_AGAIN("PLAY_AGAIN");

    String name;

    Messages(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

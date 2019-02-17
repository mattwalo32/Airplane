package com.walowtech.plane.multiplayer;

/** Enum containing all possible multiplayer events that
 * players should be notified about.
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2019-01-07
 */
public enum EventType {

    MOVE_STRAIGHT(0),
    START_TURN_LEFT(1),
    START_TURN_RIGHT(2),
    END_TURN(3),
    UPDATE_NOW(4);

    private int code;

    EventType(int code){
        this.code = code;
    }

    public int getCode(){
        return code;
    }
}

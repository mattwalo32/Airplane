package com.walowtech.plane.multiplayer;

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

package com.walowtech.plane.multiplayer;

public enum EventType {

    MOVE_STRAIGHT(Byte.parseByte("000")),
    START_TURN(Byte.parseByte("001")),
    END_TURN(Byte.parseByte("010"));

    private byte code;

    EventType(byte code){
        this.code = code;
    }

    public byte getBits(){
        return code;
    }
}

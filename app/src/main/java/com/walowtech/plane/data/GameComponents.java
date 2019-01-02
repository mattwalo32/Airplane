package com.walowtech.plane.data;

public enum GameComponents {

    PLAYER_MANAGER("playerManager"),
    GRAPHICS("gameGraphics"),
    COLLISION_DETECTION("collisionDetection");

    private String mName;

    GameComponents(String pName){
        mName = pName;
    }

    public String getName(){
        return mName;
    }
}

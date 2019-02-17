package com.walowtech.plane.data;

/** Enum contains all game componenets
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2018-08-09
 */
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

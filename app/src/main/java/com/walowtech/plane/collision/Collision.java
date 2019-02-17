package com.walowtech.plane.collision;

/**
 * Object to store data regarding collisions
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2019-01-07
 */
public class Collision {
    private boolean mCollision;
    private CollisionType mType;

    public Collision(){
        mCollision = false;
        mType = CollisionType.NONE;
    }

    public void setCollision(CollisionType pType){
        mType = pType;
        mCollision = mType != CollisionType.NONE;
    }

    public boolean isCollision(){
        return mCollision;
    }

    public CollisionType getCollisionType(){
        return mType;
    }
}

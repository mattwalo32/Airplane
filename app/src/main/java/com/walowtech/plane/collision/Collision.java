package com.walowtech.plane.collision;

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

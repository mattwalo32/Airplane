package com.walowtech.plane.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.DisplayMetrics;
import android.util.Log;

import com.walowtech.plane.R;
import com.walowtech.plane.game.GameLoop;
import com.walowtech.plane.util.CodeIntegrityUtils;
import com.walowtech.plane.util.ConversionUtils;
import com.walowtech.plane.util.GraphicUtils;

import java.io.ByteArrayOutputStream;

/**
 * Class contains information about the plane object
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2018-08-09
 */
public class Plane {

    private float dp;

    public boolean mTurn;
    public boolean mTurnRight;
    private boolean mMovingX;
    private boolean mMovingY;
    private double mDeltaX;
    private double mDeltaY;
    private float mXCoord;
    private float mYCoord;
    private float mRealX;
    private float mRealY;
    private float mWidth;
    private float mHeight;
    private float mHeading;
    private int relativeMargin;
    private RectF mRelativeBounds;
    private RectF mScreenBounds;
    private Bitmap mPlaneSprite;

    private ConversionUtils convert;
    private DisplayMetrics displayMetrics;
    private Tail mTail = new Tail();

    public Plane(Context pContext){
        CodeIntegrityUtils.checkNotNull(pContext, "Context must not be null");
        displayMetrics = pContext.getResources().getDisplayMetrics();
        convert = new ConversionUtils(pContext);

        dp = convert.dpToPx(1);
        mPlaneSprite = BitmapFactory.decodeResource(pContext.getResources(), R.drawable.icons8_fighter_jet_96);
        mPlaneSprite = Bitmap.createScaledBitmap(mPlaneSprite, (int)(60*dp), (int)(60*dp), true);

    }

    public void init(){
        mWidth = mPlaneSprite.getWidth();
        mHeight = mPlaneSprite.getHeight();
        mRealX = mXCoord = 100*dp;
        mRealY = mYCoord = 100*dp;

        relativeMargin = 2 * displayMetrics.widthPixels / 5;
        mRelativeBounds = new RectF(relativeMargin, 2 * relativeMargin, displayMetrics.widthPixels - relativeMargin, displayMetrics.heightPixels - 2 * relativeMargin);
        mScreenBounds = new RectF(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);

        //TODO: Change to 90
        mHeading = 180;
        mTurn = false;
        mTurnRight = false;

        mTail.setTailColor(Color.RED);
    }

    /**
     * Moves the plane at the specified speed in the specified heading. This
     * method takes cycleTime into account, so that speed is independent of cycle time.
     *
     * @param pSpeed The rate, in DIPs per second, to move the plane.
     * @param pHeading The angle, in degrees, to move towards.
     * @param pCycleTime The time, in ms, of the last cycle.
     */
    public void moveForward(float pSpeed, float pHeading, float pCycleTime){
        mDeltaX = ((pCycleTime * pSpeed) / 1000) * Math.cos(Math.toRadians(pHeading));
        mDeltaY = ((pCycleTime * pSpeed) / 1000) * Math.sin(Math.toRadians(pHeading));

        // The relative coordinates should be moved if the plane is near the center of the screen or
        // if the plane is not near the center of the screen but the next movement will bring it closer in either dimension.
        mMovingX = (mTurn || inRelativeBounds() || (
                (getX() + mPlaneSprite.getWidth() <= mRelativeBounds.left && getX() - mDeltaX >= getX())
                        || (getX() >= mRelativeBounds.right && getX() - mDeltaX <= getX())
                        || (getX() + mPlaneSprite.getWidth() > mRelativeBounds.left && getX()< mRelativeBounds.right)
                        || (getX() - mDeltaX <= getX() && mScreenBounds.left <= PlayerManager.GAME_BOUNDS.left)
                        || (getX() - mDeltaX >= getX() && mScreenBounds.right >= PlayerManager.GAME_BOUNDS.right)));
        mMovingY = (mTurn || inRelativeBounds() || (
                (getY() + mPlaneSprite.getHeight() <= mRelativeBounds.top && getY() - mDeltaY > getY())
                        || (getY() >= mRelativeBounds.bottom && getY() - mDeltaY <= getY())
                        || (getY() + mPlaneSprite.getHeight() > mRelativeBounds.top && getY() < mRelativeBounds.bottom)
                        || (getY() - mDeltaY <= getY() && mScreenBounds.top <= PlayerManager.GAME_BOUNDS.top)
                        || (getY() - mDeltaY >= getY() && mScreenBounds.bottom >= PlayerManager.GAME_BOUNDS.bottom)));

        if(mMovingX) {
            mXCoord -= mDeltaX;
        }else{
            mScreenBounds.right -= mDeltaX;
            mScreenBounds.left -= mDeltaX;
        }

        if(mMovingY) {
            mYCoord -= mDeltaY;
        }else{
            mScreenBounds.top -= mDeltaY;
            mScreenBounds.bottom -= mDeltaY;
        }

        mRealX -= mDeltaX;
        mRealY -= mDeltaY;
    }

    /**
     * Changes the heading of the plane at the specified speed. This method
     * takes cycleTime into account, so turn speed is independent of cycle time.
     *
     * @param pSpeed The speed, in DIPs per second, to move the plane.
     * @param pCycleTime The time, in ms, of the last cycle.
     */
    public void turn(float pSpeed, float pCycleTime){
        float angle = pCycleTime * pSpeed / 1000;
        mHeading += angle;
        //mHeading %= 360;

//        Matrix matrix = new Matrix();
//        matrix.setRotate(angle);
//        mPlaneSprite = Bitmap.createBitmap(mPlaneOriginalSprite, 0, 0, mPlaneOriginalSprite.getWidth(), mPlaneOriginalSprite.getHeight(), matrix, true);
    }

    public void setTurning(boolean pTurn, boolean pDirection){
        mTurn = pTurn;
        mTurnRight = pDirection;

        if(!mTurn)
            GameLoop.getCore().getPlayerManager().getLocalPlayer().setStraightTailGenerated(mTurn);
        else
            GameLoop.getCore().getPlayerManager().getLocalPlayer().setTurnTailGenerated(!mTurn);
    }

    private boolean inRelativeBounds(){
        return RectF.intersects(mRelativeBounds, new RectF(getX(), getY(), getX() + mPlaneSprite.getWidth(), getY() + mPlaneSprite.getHeight()));
    }

    public boolean isMovingX(){
        return mMovingX;
    }

    public boolean isMovingY(){
        return mMovingY;
    }

    public float getX(){
        return mXCoord;
    }

    public float getY(){
        return mYCoord;
    }

    public double getDeltaX() {
        return mDeltaX;
    }

    public double getDeltaY() {
        return mDeltaY;
    }

    public float getTailX(int offset){
        return (float)((mXCoord + mPlaneSprite.getWidth() / 2) + ((mPlaneSprite.getWidth() / 2 - offset) * Math.cos(Math.toRadians(360 - mHeading))));
    }

    public float getTailY(int offset){
        return (float)((mYCoord + mPlaneSprite.getHeight() / 2) - ((mPlaneSprite.getWidth() / 2 - offset) * Math.sin(Math.toRadians(360 - mHeading))));
    }

    public float getHeadX(int offset){
        return (float)((mXCoord + mPlaneSprite.getWidth() / 2) + ((mPlaneSprite.getWidth() / 2 - offset) * Math.cos(Math.toRadians(180 - mHeading))));
    }

    public float getHeadY(int offset){
        return (float)((mYCoord + mPlaneSprite.getHeight() / 2) - ((mPlaneSprite.getWidth() / 2 - offset) * Math.sin(Math.toRadians(180 - mHeading))));
    }

    public float getLeftWingX(int offset){
        return (float)((mXCoord + mPlaneSprite.getWidth() / 2) + ((mPlaneSprite.getWidth() / 2 - offset) * Math.cos(Math.toRadians(290 - mHeading))));
    }

    public float getLeftWingY(int offset){
        return (float)((mYCoord + mPlaneSprite.getHeight() / 2) - ((mPlaneSprite.getWidth() / 2 - offset) * Math.sin(Math.toRadians(290 - mHeading))));
    }

    public float getRightWingX(int offset){
        return (float)((mXCoord + mPlaneSprite.getWidth() / 2) + ((mPlaneSprite.getWidth() / 2 - offset) * Math.cos(Math.toRadians(70 - mHeading))));
    }

    public float getRightWingY(int offset){
        return (float)((mYCoord + mPlaneSprite.getHeight() / 2) - ((mPlaneSprite.getWidth() / 2 - offset) * Math.sin(Math.toRadians(70 - mHeading))));
    }

    public float getScreenX(){
        return mScreenBounds.left;
    }

    public float getScreenY(){
        return mScreenBounds.top;
    }

    public float getRealX(){
        return mRealX;
    }

    public float getRealY(){
        return mRealY;
    }

    public float getHeading(){
        return mHeading;
    }

    public float getPlaneWidthDIP(){
        return mWidth;
    }

    public float getPlaneHeightDIP(){
        return mHeight;
    }

    public Bitmap getPlaneSprite(){
        return mPlaneSprite;
    }

    public Tail getTail(){
        return mTail;
    }
}

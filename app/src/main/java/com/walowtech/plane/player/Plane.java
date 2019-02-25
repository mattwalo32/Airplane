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

import com.google.android.gms.games.Game;
import com.walowtech.plane.R;
import com.walowtech.plane.data.TailDataPoint;
import com.walowtech.plane.game.GameLoop;
import com.walowtech.plane.multiplayer.EventType;
import com.walowtech.plane.multiplayer.MessageUtils;
import com.walowtech.plane.multiplayer.MultiplayerAccess;
import com.walowtech.plane.util.CodeIntegrityUtils;
import com.walowtech.plane.util.ConversionUtils;
import com.walowtech.plane.util.GraphicUtils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import static com.walowtech.plane.player.PlayerManager.MAX_DISPLAY_PLANES;

/**
 * Class contains information about the plane object
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2018-08-09
 */
public class Plane {

    private float dp;
    private int mPlayerId;

    public boolean mTurn;
    public boolean mTurnRight;
    private boolean mMovingX;
    private boolean mMovingY;
    private boolean mIsLocal;
    private boolean mDisplayMode;

    private double mDeltaX;
    private double mDeltaY;

    private float mXCoord;
    private float mYCoord;
    private float mRealX;
    private float mRealY;
    private float mWidth;
    private float mHeight;
    private float mHeading;
    private float mScreenWidth;
    private float mScreenHeight;

    private int relativeMargin;
    private RectF mRelativeBounds;
    private RectF mScreenBounds;
    private ArrayList<Point> mHitboxPoints = new ArrayList<>();
    private Bitmap mPlaneSprite;

    private ConversionUtils convert;
    private DisplayMetrics displayMetrics;
    private Tail mTail;
    private GameLoop mGameLoop;

    /**
     * Creates plane object
     * @param pContext Context from which plane was created
     * @param pGameLoop The GameLoop to which this plane belongs to
     * @param pLocal True if player is local, false if player is opponent
     * @param pId Number identifying player
     * @param pDisplayMode True if Plane is in display mode
     */
    public Plane(Context pContext, GameLoop pGameLoop, boolean pLocal, int pId, boolean pDisplayMode){
        CodeIntegrityUtils.checkNotNull(pContext, "Context must not be null");
        displayMetrics = pContext.getResources().getDisplayMetrics();
        convert = new ConversionUtils(pContext);
        mIsLocal = pLocal;
        mPlayerId = pId;
        mGameLoop = pGameLoop;
        mDisplayMode = pDisplayMode;

        dp = convert.dpToPx(1);
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;
        mPlaneSprite = BitmapFactory.decodeResource(pContext.getResources(), R.drawable.icons8_fighter_jet_96);
        mPlaneSprite = Bitmap.createScaledBitmap(mPlaneSprite, (int)(120*dp), (int)(120*dp), true);

        mTail = new Tail(mGameLoop, pLocal, mDisplayMode, mPlayerId);
    }

    public void init(){

        mTail.getTailData().clear();

        boolean startLeft = false;

        if(mGameLoop.getCore().getMultiplayerAccess() != null)
            startLeft =  mIsLocal == MultiplayerAccess.sStartingTopLeft;

        mWidth = mPlaneSprite.getWidth();
        mHeight = mPlaneSprite.getHeight();

        relativeMargin = 3 * displayMetrics.widthPixels / 7;

        mScreenBounds = new RectF(0, (PlayerManager.GAME_BOUNDS.bottom - displayMetrics.heightPixels) / 2, displayMetrics.widthPixels, (PlayerManager.GAME_BOUNDS.bottom + displayMetrics.heightPixels) / 2);

        if(mIsLocal)
        {
            if(!startLeft && !mDisplayMode)
            {
                mScreenBounds.left = PlayerManager.GAME_BOUNDS.right - displayMetrics.widthPixels;
                mScreenBounds.right = PlayerManager.GAME_BOUNDS.right;
            }

            mXCoord = startLeft ? 200 : displayMetrics.widthPixels - 200;
            mRealX = startLeft ? 200 : PlayerManager.GAME_BOUNDS.right - 200;
        }
        else
        {
            mXCoord = mRealX = startLeft ? 200 : PlayerManager.GAME_BOUNDS.right - 200;
        }

        mRelativeBounds = new RectF(relativeMargin, 2 * relativeMargin, displayMetrics.widthPixels - relativeMargin, displayMetrics.heightPixels - 2 * relativeMargin);

        mYCoord = (displayMetrics.heightPixels - mPlaneSprite.getHeight()) / 2.0f;
        mRealY = (PlayerManager.GAME_BOUNDS.bottom - mPlaneSprite.getHeight()) / 2.0f;

        mHeading =  startLeft ? 180 : 0;
        mTurn = false;
        mTurnRight = false;
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

        mRealX -= mDeltaX;
        mRealY -= mDeltaY;

        if(!mIsLocal && !mDisplayMode) return;

        // The relative coordinates should be moved if the plane is near the center of the screen or
        // if the plane is not near the center of the screen but the next movement will bring it closer in either dimension.
        mMovingX = (mDisplayMode || mTurn || inRelativeBounds() || (
                (getX() + mPlaneSprite.getWidth() <= mRelativeBounds.left && getX() - mDeltaX >= getX())
                        || (getX() >= mRelativeBounds.right && getX() - mDeltaX <= getX())
                        || (getX() + mPlaneSprite.getWidth() > mRelativeBounds.left && getX()< mRelativeBounds.right)
                        || (getX() - mDeltaX <= getX() && mScreenBounds.left <= PlayerManager.GAME_BOUNDS.left)
                        || (getX() - mDeltaX >= getX() && mScreenBounds.right >= PlayerManager.GAME_BOUNDS.right)));
        mMovingY = (mDisplayMode || mTurn || inRelativeBounds() || (
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

        if(mDisplayMode)
        {
            if(getRealX() + mWidth + 1 < 0 || getRealX() > mScreenWidth + mWidth + 1 || getRealY() + mHeight + 1 < 0 || getRealY() > mScreenHeight + mHeight + 1)
            {
                mHeading = (int)(Math.random() * 360);
                //Start Left
                if(mHeading > 135 && mHeading <= 225)
                {
                    mXCoord = mRealX = -mWidth;
                    mYCoord = mRealY = (int)(Math.random() * mScreenHeight);
                }
                //Start Bottom
                else if(mHeading > 45 && mHeading <= 135)
                {
                    mYCoord = mRealY = mScreenHeight + mHeight;
                    mXCoord = mRealX = (int)(Math.random() * mScreenWidth);
                }
                //Start Right
                else if(mHeading > 315 || mHeading <= 45)
                {
                    mXCoord = mRealX = mScreenWidth + mWidth;
                    mYCoord = mRealY = (int)(Math.random() * mScreenHeight);

                }
                //Start Top
                else
                {
                    mYCoord = mRealY = -mHeight;
                    mXCoord = mRealX = (int)(Math.random() * mScreenWidth);
                }

                mGameLoop.getCore().getPlayerManager().getPlayers().get(mPlayerId).setStraightTailGenerated(false);
            }
        }
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

    /**
     * Sets the plane to turn
     *
     * @param pTurn True if starting turn, false if ending turn
     * @param pDirection True if turning right, false if turning left
     */
    public void setTurning(boolean pTurn, boolean pDirection){
        mTurn = pTurn;
        mTurnRight = pDirection;

        if(!mTurn)
            mGameLoop.getCore().getPlayerManager().getPlayers().get(mPlayerId).setStraightTailGenerated(mTurn);
        else
            mGameLoop.getCore().getPlayerManager().getPlayers().get(mPlayerId).setTurnTailGenerated(!mTurn);
    }

    /**
     * Sends a message to all of the game participants telling the current status of the plane
     */
    public void notifyLocationToAll()
    {
        mGameLoop.getCore().getMultiplayerAccess().sendToAll(MessageUtils.composeMessage(EventType.UPDATE_NOW, (int)mRealX, (int)mRealY, (int)mHeading));
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

    public float getRealTailX(int offset){
        return (float)((mRealX + mPlaneSprite.getWidth() / 2) + ((mPlaneSprite.getWidth() / 2 - offset) * Math.cos(Math.toRadians(360 - mHeading))));
    }

    public float getRealTailY(int offset){
        return (float)((mRealY + mPlaneSprite.getHeight() / 2) - ((mPlaneSprite.getWidth() / 2 - offset) * Math.sin(Math.toRadians(360 - mHeading))));
    }

    public float getHeadX(int offset){
        return (float)((mXCoord + mPlaneSprite.getWidth() / 2) + ((mPlaneSprite.getWidth() / 2 - offset) * Math.cos(Math.toRadians(180 - mHeading))));
    }

    public float getRealHeadX(int offset){
        return (float)((mRealX + mPlaneSprite.getWidth() / 2) + ((mPlaneSprite.getWidth() / 2 - offset) * Math.cos(Math.toRadians(180 - mHeading))));
    }

    public float getHeadY(int offset){
        return (float)((mYCoord + mPlaneSprite.getHeight() / 2) - ((mPlaneSprite.getWidth() / 2 - offset) * Math.sin(Math.toRadians(180 - mHeading))));
    }

    public float getRealHeadY(int offset){
        return (float)((mRealY + mPlaneSprite.getHeight() / 2) - ((mPlaneSprite.getWidth() / 2 - offset) * Math.sin(Math.toRadians(180 - mHeading))));
    }

    public float getLeftWingX(int offset){
        return (float)((mXCoord + mPlaneSprite.getWidth() / 2) + ((mPlaneSprite.getWidth() / 2 - offset) * Math.cos(Math.toRadians(290 - mHeading))));
    }

    public float getRealLeftWingX(int offset){
        return (float)((mRealX + mPlaneSprite.getWidth() / 2) + ((mPlaneSprite.getWidth() / 2 - offset) * Math.cos(Math.toRadians(290 - mHeading))));
    }

    public float getLeftWingY(int offset){
        return (float)((mYCoord + mPlaneSprite.getHeight() / 2) - ((mPlaneSprite.getWidth() / 2 - offset) * Math.sin(Math.toRadians(290 - mHeading))));
    }

    public float getRealLeftWingY(int offset){
        return (float)((mRealY + mPlaneSprite.getHeight() / 2) - ((mPlaneSprite.getWidth() / 2 - offset) * Math.sin(Math.toRadians(290 - mHeading))));
    }

    public float getRightWingX(int offset){
        return (float)((mXCoord + mPlaneSprite.getWidth() / 2) + ((mPlaneSprite.getWidth() / 2 - offset) * Math.cos(Math.toRadians(70 - mHeading))));
    }

    public float getRealRightWingX(int offset){
        return (float)((mRealX + mPlaneSprite.getWidth() / 2) + ((mPlaneSprite.getWidth() / 2 - offset) * Math.cos(Math.toRadians(70 - mHeading))));
    }

    public float getRightWingY(int offset){
        return (float)((mYCoord + mPlaneSprite.getHeight() / 2) - ((mPlaneSprite.getWidth() / 2 - offset) * Math.sin(Math.toRadians(70 - mHeading))));
    }

    public float getRealRightWingY(int offset){
        return (float)((mRealY + mPlaneSprite.getHeight() / 2) - ((mPlaneSprite.getWidth() / 2 - offset) * Math.sin(Math.toRadians(70 - mHeading))));
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

    public void setRealX(float pX){
        mRealX = pX;
    }

    public void setRealY(float pY){
        mRealY = pY;
    }

    public void setHeading(float pHeading){
        mHeading = pHeading;
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

    public ArrayList<Point> getHitboxPoints()
    {
        return mHitboxPoints;
    }

    public Bitmap getPlaneSprite(){
        return mPlaneSprite;
    }

    public Tail getTail(){
        return mTail;
    }

    public boolean isLocal(){
        return mIsLocal;
    }

    public boolean inDisplayMode()
    {
        return mDisplayMode;
    }
}

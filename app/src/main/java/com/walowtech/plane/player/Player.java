package com.walowtech.plane.player;

import android.content.Context;
import android.graphics.RectF;
import android.util.Log;

import com.walowtech.plane.data.TailDataPoint;
import com.walowtech.plane.game.GameLoop;
import com.walowtech.plane.util.CodeIntegrityUtils;
import com.walowtech.plane.util.ConversionUtils;

/**
 * Object for a player.
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2018-08-09
 */
public class Player {

    private Plane mPlane;

    private int mPlayerId;
    private float mPlayerSpeed;
    private float mPlayerTurnSpeed;
    private boolean mPlayerIsLocal;
    private boolean turnTailGenerated;
    private boolean straightTailGenerated;
    private boolean mDisplayMode;

    /**
     * Player object constructor
     * @param pContext Calling context
     * @param pGameLoop The GameLoop that owns this player
     * @param pId Id of player
     * @param pPlayerIsLocal True if player is local, false if opponent
     * @param pDisplayMode True if plane is in display mode
     */
    public Player(Context pContext, GameLoop pGameLoop, int pId, boolean pPlayerIsLocal, boolean pDisplayMode){
        CodeIntegrityUtils.checkNotNull(pContext, "Context cannot be null");

        mPlayerId = pId;
        mPlayerIsLocal = pPlayerIsLocal;
        mDisplayMode = pDisplayMode;
        mPlane = new Plane(pContext, pGameLoop, pPlayerIsLocal, mPlayerId, pDisplayMode);
    }

    /**
     * Initializes player for game
     */
    public void init(){
        mPlane.init();
        turnTailGenerated = false;
        straightTailGenerated = false;
    }

    /**
     * Updates player position and tail
     */
    public void update(){
        mPlane.moveForward(mPlayerSpeed, mPlane.getHeading(), GameLoop.sActualCycleTime);

        if(mPlane.mTurn){
            mPlane.turn(mPlane.mTurnRight ? mPlayerTurnSpeed : -mPlayerTurnSpeed, GameLoop.sActualCycleTime);
            if(!turnTailGenerated){
                mPlane.getTail().addDataPoint(
                        new TailDataPoint(new RectF(mPlane.getTailX(0), mPlane.getTailY(0), mPlane.getTailX(0), mPlane.getTailY(0)), mPlane.getHeading(),
                                ConversionUtils.headingToArc(mPlane.getHeading(), mPlane.mTurnRight), 0));
                turnTailGenerated = true;
            }else{
                //mPlane.getTail().updateCurrentCurve(mPlane.getTailX(0), mPlane.getTailY(0),  mPlane.getHeading() - mPlane.getTail().getCurrentDataPoint().getStartHeading());
            }
        }else{
            if(!straightTailGenerated){
                float x = mPlane.isLocal() || mPlane.inDisplayMode() ? mPlane.getTailX(0) : mPlane.getRealTailX(0);
                float y = mPlane.isLocal() || mPlane.inDisplayMode() ? mPlane.getTailY(0) : mPlane.getRealTailY(0);

                mPlane.getTail().addDataPoint(new TailDataPoint(x, y, x, y));
                straightTailGenerated = true;
            }else{
                mPlane.getTail().updateCurrentLine();
            }
        }
    }

    public void stop(){

    }

    public void setPlayerSpeed(float pSpeed){
        mPlayerSpeed = pSpeed;
    }

    public void setPlayerTurnSpeed(float pSpeed){
        mPlayerTurnSpeed = pSpeed;
    }

    public void setTurnTailGenerated(boolean pGenerated){
        turnTailGenerated = pGenerated;
    }

    public void setStraightTailGenerated(boolean pGenerated){
        straightTailGenerated = pGenerated;
    }

    public Plane getPlane(){
        return mPlane;
    }

    public int getPlayerId(){
        return mPlayerId;
    }

    public boolean isPlayerLocal(){
        return mPlayerIsLocal;
    }
}

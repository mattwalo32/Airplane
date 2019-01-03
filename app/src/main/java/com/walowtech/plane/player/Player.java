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

    public Player(Context pContext, int pId, boolean pPlayerIsLocal){
        CodeIntegrityUtils.checkNotNull(pContext, "Context cannot be null");

        mPlayerId = pId;
        mPlayerIsLocal = pPlayerIsLocal;
        mPlane = new Plane(pContext);
    }

    public void init(){
        mPlane.init();
        turnTailGenerated = false;
        straightTailGenerated = false;
    }

    public void update(){
        mPlane.moveForward(mPlayerSpeed, mPlane.getHeading(), GameLoop.sActualCycleTime);

        if(mPlane.mTurn){
            mPlane.turn(mPlane.mTurnRight ? mPlayerTurnSpeed : -mPlayerTurnSpeed, GameLoop.TARGET_CYCLE_TIME);
            if(!turnTailGenerated){
                mPlane.getTail().addDataPoint(
                        new TailDataPoint(new RectF(mPlane.getTailX(0), mPlane.getTailY(0), mPlane.getTailX(0), mPlane.getTailY(0)), mPlane.getHeading(),
                                ConversionUtils.headingToArc(mPlane.getHeading(), mPlane.mTurnRight), 0));
                turnTailGenerated = true;
            }else{
                mPlane.getTail().updateCurrentCurve(mPlane.getTailX(0), mPlane.getTailY(0),  mPlane.getHeading() - mPlane.getTail().getCurrentDataPoint().getStartHeading());
            }
        }else{
            if(!straightTailGenerated){
                mPlane.getTail().addDataPoint(
                        new TailDataPoint(mPlane.getTailX(0), mPlane.getTailY(0), mPlane.getTailX(0), mPlane.getTailY(0))
                );
                straightTailGenerated = true;
            }else{
                mPlane.getTail().updateCurrentLine();
            }
        }
    }

    public void stop(){
        mPlane.getTail().getTailData().clear();
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

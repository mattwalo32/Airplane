package com.walowtech.plane.game;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Button;

import com.walowtech.plane.activity.GameActivity;
import com.walowtech.plane.activity.MainActivity;
import com.walowtech.plane.multiplayer.EventType;
import com.walowtech.plane.multiplayer.MessageUtils;
import com.walowtech.plane.multiplayer.Messages;
import com.walowtech.plane.multiplayer.MultiplayerAccess;
import com.walowtech.plane.player.Plane;
import com.walowtech.plane.player.PlayerManager;
import com.walowtech.plane.util.CodeIntegrityUtils;

/**
 * This class contains the main game loop that schedules the
 * execution of the game.
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2018-08-09
 */
public class GameLoop implements Runnable{

    private final String TAG = getClass().getName();
    private static final int TARGET_CYCLE_TIME = 1000 / 60;
    private GameCore CORE;

    private long mCycleStartTime;
    private long cycleTime;
    public static long sActualCycleTime = 0;
    public boolean mRunning;
    private boolean opponentShown = false;
    private boolean mMultiplayerMode;
    private boolean mDisplayMode;

    private Thread gameThread;
    private Context mContext;
    private Activity mActivity;

    /**
     * Constructor for single player
     * @param pContext Context of calling activity
     * @param pActivity Calling activity
     * @param pDisplayMode True if loop should be in display mode
     */
    public GameLoop(Context pContext, Activity pActivity, boolean pDisplayMode){
        CodeIntegrityUtils.checkNotNull(pContext, "Context cannot be null");
        mContext = pContext;
        mActivity = pActivity;
        CORE = new GameCore(pContext, pActivity, this, pDisplayMode);
        mMultiplayerMode = false;
        mDisplayMode = pDisplayMode;
    }

    /**
     * Constructor for multiplayer
     * @param pContext Context of calling activity
     * @param pActivity Calling activity
     * @param pMultiplayerAccess Object to access multiplayer data and send messages
     */
    public GameLoop(Context pContext, Activity pActivity, MultiplayerAccess pMultiplayerAccess){
        CodeIntegrityUtils.checkNotNull(pContext, "Context cannot be null");
        CodeIntegrityUtils.checkNotNull(pMultiplayerAccess, "Multiplayer Access cannot be null");
        mContext = pContext;
        mActivity = pActivity;
        CORE = new GameCore(pContext, pActivity, this, pMultiplayerAccess);
        mMultiplayerMode = true;
    }

    /**
     * This is the main game loop that runs periodically.
     * All game components update for every loop
     */
    @Override
    public void run(){
        CORE.init();
        CORE.executeUpdate();

        // Wait for player to be ready
        if(mMultiplayerMode)
        {
            while (!MultiplayerAccess.sOpponentReady || !MultiplayerAccess.sClientReady) {
                try {
                    if(MultiplayerAccess.sOpponentReady && !opponentShown)
                    {
                        if (mActivity instanceof GameActivity)
                        {
                            mActivity.runOnUiThread(()->((GameActivity) mActivity).onOpponentReady());
                            opponentShown = true;
                        }
                    }

                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Remove indicator if it is up
            mActivity.runOnUiThread(()->{
                if(mActivity instanceof GameActivity)if(mActivity instanceof GameActivity)
                        ((GameActivity)mActivity).hideReadyLayout();
            });
        }

        mRunning = true;
        while(mRunning){
            try {
                mCycleStartTime = System.currentTimeMillis();
                CORE.executeUpdate();
                cycleTime = mCycleStartTime - System.currentTimeMillis();

                //If there is still time left in the cycle, sleep for the remaining time
                if(cycleTime < TARGET_CYCLE_TIME) {
                    //Log.v(TAG, "Sleep Time: " + (cycleTime - TARGET_CYCLE_TIME));
                    Thread.sleep(TARGET_CYCLE_TIME - cycleTime);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            sActualCycleTime = System.currentTimeMillis() - mCycleStartTime;
        }
        //CORE.stop();

        //restartGame();
    }

    /**
     * Starts the game thread
     */
    public void startGame(){
        mRunning = false;
        gameThread = new Thread(this);
        gameThread.start();
    }

    /**
     * Restarts the game by hiding buttons and creating new thread
     */
    public void restartGame(){
        MultiplayerAccess.sClientPlayAgain = false;
        MultiplayerAccess.sOpponentPlayAgain = false;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mActivity instanceof GameActivity)
                    ((GameActivity)mActivity).hideEndgameButtons();
            }
        });
        startGame();
    }

    /**
     * Stops game for all participants
     */
    public void stopGame(){
        mRunning = false;
    }


    public GameCore getCore(){
        return CORE;
    }
}

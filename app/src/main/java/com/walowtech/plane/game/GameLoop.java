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
    private static final int MAX_CYCLES_TO_SEND = 30;
    private static GameCore CORE;

    private long mCycleStartTime;
    private long cycleTime;
    public static long sActualCycleTime = 0;
    public static boolean mRunning;
    private boolean mMultiplayerMode;

    private Thread gameThread;
    private Context mContext;
    private Activity mActivity;

    /**
     * Constructor for single player
     * @param pContext Context of calling activity
     * @param pActivity Calling activity
     */
    public GameLoop(Context pContext, Activity pActivity){
        CodeIntegrityUtils.checkNotNull(pContext, "Context cannot be null");
        mContext = pContext;
        mActivity = pActivity;
        CORE = new GameCore(pContext, pActivity);
        mMultiplayerMode = false;
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
        CORE = new GameCore(pContext, pActivity, pMultiplayerAccess);
        mMultiplayerMode = true;
    }

    /**
     * This is the main game loop that runs periodically.
     * All game components update for every loop
     */
    @Override
    public void run(){
        int cyclesUntilUpdate = 0;

        CORE.init();

        // Wait for player to be ready
        if(mMultiplayerMode)
        {
            while (!MultiplayerAccess.mOpponentReady || !MultiplayerAccess.mClientReady) {
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Remove indicator if it is up
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mActivity instanceof GameActivity)
                        ((GameActivity)mActivity).hideReadyLayout();
                }
            });
        }

        while(mRunning){
            try {
                mCycleStartTime = System.currentTimeMillis();
                CORE.executeUpdate();
                cycleTime = mCycleStartTime - System.currentTimeMillis();

                //If there is still time left in the cycle, sleep for the remaining time
                if(cycleTime < TARGET_CYCLE_TIME) {
                    Log.v(TAG, "Sleep Time: " + (TARGET_CYCLE_TIME - cycleTime));
                    Thread.sleep(TARGET_CYCLE_TIME - cycleTime);
                }

                //TODO: Clean this up, take out of loop
                cyclesUntilUpdate--;
                if(cyclesUntilUpdate <= 0)
                {
                    if(getCore().getMultiplayerAccess() != null)
                        getCore().getPlayerManager().getLocalPlayer().getPlane().notifyLocationToAll();
                }
                cyclesUntilUpdate = MAX_CYCLES_TO_SEND;
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
        mRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    /**
     * Restarts the game by hiding buttons and creating new thread
     */
    public void restartGame(){
        MultiplayerAccess.mClientPlayAgain = false;
        MultiplayerAccess.mOpponentPlayAgain = false;
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
    public static void stopGame(){
        mRunning = false;
    }


    public static GameCore getCore(){
        return CORE;
    }
}

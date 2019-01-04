package com.walowtech.plane.game;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Button;

import com.walowtech.plane.activity.GameActivity;
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

    public static final int TARGET_CYCLE_TIME = 1000 / 60;
    private static GameCore CORE;

    private long mCycleStartTime;
    private long cycleTime;
    public static long sActualCycleTime = 0;
    public static boolean mRunning;

    private Thread gameThread;
    private Context mContext;
    private Activity mActivity;


    public GameLoop(Context pContext, Activity pActivity){
        CodeIntegrityUtils.checkNotNull(pContext, "Context cannot be null");
        mContext = pContext;
        mActivity = pActivity;
        CORE = new GameCore(pContext, pActivity);
    }

    public GameLoop(Context pContext, Activity pActivity, MultiplayerAccess pMultiplayerAccess){
        CodeIntegrityUtils.checkNotNull(pContext, "Context cannot be null");
        CodeIntegrityUtils.checkNotNull(pMultiplayerAccess, "Multiplayer Access cannot be null");
        mContext = pContext;
        mActivity = pActivity;
        CORE = new GameCore(pContext, pActivity, pMultiplayerAccess);
    }

    @Override
    public void run(){
        CORE.init();
        while(mRunning){
            try {
                //Log.i("TEST", "Running Loop");
                mCycleStartTime = System.currentTimeMillis();

                CORE.executeUpdate();
                cycleTime = mCycleStartTime - System.currentTimeMillis();

                //If there is still time left in the cycle, sleep for the remaining time
                if(cycleTime < TARGET_CYCLE_TIME) {
                    //Log.i("TEST", "Sleep Time: " + (TARGET_CYCLE_TIME - cycleTime));
                    Thread.sleep(TARGET_CYCLE_TIME - cycleTime);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            sActualCycleTime = System.currentTimeMillis() - mCycleStartTime;
        }
        CORE.stop();

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GameActivity.createButtons(mContext);
            }
        });

        //restartGame();
    }

    public void startGame(){
        mRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void restartGame(){
        startGame();
    }

    public static void stopGame(){
        mRunning = false;

        if(getCore().getMultiplayerAccess() != null){
            getCore().getMultiplayerAccess().sendToAllReliably(Messages.COLLIDED.toString());

        }
    }

    public void showButtons(){
        Button btnRestart = new Button(mContext);
        btnRestart.setText("Play Again");
    }

    public static GameCore getCore(){
        return CORE;
    }
}

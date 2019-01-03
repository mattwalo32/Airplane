package com.walowtech.plane.game;

import android.content.Context;
import android.util.Log;

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
    private static boolean mRunning;

    private Thread gameThread;


    public GameLoop(Context pContext){
        CodeIntegrityUtils.checkNotNull(pContext, "Context cannot be null");
        CORE = new GameCore(pContext);
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
        restartGame();
    }

    public void startGame(){
        mRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    private void restartGame(){
        startGame();
    }

    public static void stopGame(){
        mRunning = false;
    }

    public static GameCore getCore(){
        return CORE;
    }
}

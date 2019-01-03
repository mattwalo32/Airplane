package com.walowtech.plane.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.games.multiplayer.realtime.Room;
import com.walowtech.plane.Input.GameClickListener;
import com.walowtech.plane.game.GameLoop;
import com.walowtech.plane.multiplayer.Messages;
import com.walowtech.plane.multiplayer.MultiplayerAccess;

/**
 * Activity that contains the game screen view
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2018-08-09
 */
public class GameActivity extends Activity {

    private GameLoop gameLoop;
    private GameClickListener clickListener;
    private String mClientParticipationID;
    private Room mRoom;
    private MultiplayerAccess mMultiplayerAccess;
    private boolean mMuliplayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMuliplayer = false;

        gameLoop = new GameLoop(this);
        setContentView(GameLoop.getCore().getGraphics());

        clickListener = new GameClickListener(this);
        GameLoop.getCore().getGraphics().setOnTouchListener(clickListener);

        mClientParticipationID = getIntent().getStringExtra("USERID");
        Log.i("MULTIPLAYER", "Got ID Extra: " + mClientParticipationID);
        mRoom = getIntent().getParcelableExtra("ROOM");

        if(mClientParticipationID != null){
            Log.i("MULTIPLAYER", "In multiplayer mode");
            // Multi Player
            mMuliplayer = true;
            mMultiplayerAccess = new MultiplayerAccess(this, this, mRoom, mClientParticipationID);
            new Thread(new opponentChecker()).start();
        }else {
            Log.i("MULTIPLAYER", "In singleplayer mode");
            // Single Player
            gameLoop.startGame();
        }
    }

    private class opponentChecker implements Runnable {

        private boolean mRunning;

        @Override
        public void run() {
            mRunning = true;
            mMultiplayerAccess.setClientReady(true);
            mMultiplayerAccess.sendToAllReliably(Messages.READY_TO_START.toString());
            while(mRunning){

                //Log.i("MULTIPLAYER", "WAITING TO START");

                if(mMultiplayerAccess.isOpponentReady()){
                    Log.i("MULTIPLAYER", "BOTH ARE READY");
                    mRunning = false;
                }

                try{
                    Thread.sleep(50);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            gameLoop.startGame();
        }
    }
}

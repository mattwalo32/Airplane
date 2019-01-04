package com.walowtech.plane.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.games.multiplayer.realtime.Room;
import com.walowtech.plane.Input.GameClickListener;
import com.walowtech.plane.R;
import com.walowtech.plane.game.GameLoop;
import com.walowtech.plane.multiplayer.EventType;
import com.walowtech.plane.multiplayer.MessageUtils;
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
    public static RelativeLayout mRoot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMuliplayer = false;

        mClientParticipationID = getIntent().getStringExtra("USERID");
        mRoom = getIntent().getParcelableExtra("ROOM");

        if(mClientParticipationID != null){
            Log.i("MULTIPLAYER", "In multiplayer mode");
            // Multi Player
            mMuliplayer = true;
            mMultiplayerAccess = new MultiplayerAccess(this, this, mRoom, mClientParticipationID);
            gameLoop = new GameLoop(this, this, mMultiplayerAccess);
            new Thread(new opponentChecker()).start();
        }else{
            Log.i("MULTIPLAYER", "In singleplayer mode");
            // Single Player
            gameLoop = new GameLoop(this, this);
            gameLoop.startGame();
        }

        setContentView(R.layout.activity_game);
        mRoot = findViewById(R.id.root);
        mRoot.addView(GameLoop.getCore().getGraphics());
        clickListener = new GameClickListener(this);
        GameLoop.getCore().getGraphics().setOnTouchListener(clickListener);


        if(mMuliplayer){
            mMultiplayerAccess.sendToAllReliably(Messages.READY_TO_START.toString());
        }
    }

    public static void createButtons(Context context){
        View btns = LayoutInflater.from(context).inflate(R.layout.endgame_buttons, mRoot, false);

        mRoot.addView(btns);
    }

    public void playAgain(View v){
        mMultiplayerAccess.sendToAllReliably(Messages.PLAY_AGAIN.toString());
        MultiplayerAccess.mClientPlayAgain = true;
        final Room room = mMultiplayerAccess.getRoom();

        if(MultiplayerAccess.mOpponentPlayAgain){
            gameLoop.restartGame();
        }else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(MultiplayerAccess.mOpponentPlayAgain)
                        gameLoop.restartGame();
                    else if(mMultiplayerAccess.shouldCancelGame(room))
                        mMultiplayerAccess.leaveRoom();

                    try{
                        Thread.sleep(50);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public void quit(View v){
        MultiplayerAccess.mClientPlayAgain = false;
        mMultiplayerAccess.leaveRoom();
    }

    private class opponentChecker implements Runnable {

        private boolean mRunning;

        @Override
        public void run() {
            mRunning = true;
            while(mRunning){

                //Log.i("MULTIPLAYER", "WAITING TO START");

                if(MultiplayerAccess.mOpponentReady){
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

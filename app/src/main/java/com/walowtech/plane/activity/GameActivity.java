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
 * Activity that contains the game screen view.
 * Everything game-related is painted onto the canvas on this page
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

        // Set up view so buttons can be added on top later
        setContentView(R.layout.activity_game);
        mRoot = findViewById(R.id.root);
        mRoot.addView(GameLoop.getCore().getGraphics());
        View btns = LayoutInflater.from(this).inflate(R.layout.endgame_buttons, mRoot, false);
        mRoot.addView(btns);

        clickListener = new GameClickListener(this);
        GameLoop.getCore().getGraphics().setOnTouchListener(clickListener);


        if(mMuliplayer){
            mMultiplayerAccess.sendToAllReliably(Messages.READY_TO_START.toString());
        }
    }

    /**
     * Shows buttons so that users can request to play again.
     */
    public static void showButtons(){
        Button btnPlayAgain = mRoot.findViewById(R.id.play_again);
        btnPlayAgain.setVisibility(View.VISIBLE);
        Button btnQuit = mRoot.findViewById(R.id.quit);
        btnQuit.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the "play again" and "quit" buttons from view
     */
    public static void hideButtons(){
        Button btnPlayAgain = mRoot.findViewById(R.id.play_again);
        btnPlayAgain.setVisibility(View.GONE);
        Button btnQuit = mRoot.findViewById(R.id.quit);
        btnQuit.setVisibility(View.GONE);
    }

    /**
     * Called when the user requests to play again.
     * Sends replay request and listens for response.
     *
     * @param v View that invoked method
     */
    public void playAgain(View v){
        Log.i("TEST", "Play Again: " + v.getId());
        GameLoop.getCore().getMultiplayerAccess().sendToAllReliably(Messages.PLAY_AGAIN.toString());
        MultiplayerAccess.mClientPlayAgain = true;
        final Room room = mMultiplayerAccess.getRoom();

        if(MultiplayerAccess.mOpponentPlayAgain){
            gameLoop.restartGame();
        }else{
            // Thread checks if opponent wants to play again
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean checking = true;
                    while(checking) {
                        if (MultiplayerAccess.mOpponentPlayAgain)
                            gameLoop.restartGame();
                        else if (false) { //TODO: Update to check if player left
                            checking = false;
                            mMultiplayerAccess.leaveRoom();
                        }

                        try {
                            Thread.sleep(50);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }

    /**
     * Called when user requests to leave game
     *
     * @param v View that invoked the method
     */
    public void quit(View v){
        Log.i("TEST", "Quit: " + v.getId());
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

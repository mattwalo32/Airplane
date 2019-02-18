package com.walowtech.plane.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
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
            MultiplayerAccess.mClientReady = false;
            gameLoop = new GameLoop(this, this, mMultiplayerAccess);
        }else{
            Log.i("MULTIPLAYER", "In singleplayer mode");
            // Single Player
            gameLoop = new GameLoop(this, this);
        }

        // Set up view so buttons can be added on top later
        setContentView(R.layout.activity_game);
        mRoot = findViewById(R.id.root);
        mRoot.addView(GameLoop.getCore().getGraphics());
        View btns = LayoutInflater.from(this).inflate(R.layout.endgame_buttons, mRoot, false);
        View readyBtns = LayoutInflater.from(this).inflate(R.layout.ready_to_start_layout, mRoot, false);
        mRoot.addView(btns);
        mRoot.addView(readyBtns);

        clickListener = new GameClickListener(this);
        GameLoop.getCore().getGraphics().setOnTouchListener(clickListener);

        gameLoop.startGame();
        showReadyLayout();
    }

    /**
     * Leave current match by killing current activity
     */
    public void exitMatch()
    {
        Toast.makeText(this, "Game has ended because players left", Toast.LENGTH_LONG).show();
        finish();
    }

    /**
     * Shows buttons so that users can request to play again.
     */
    public static void showEndgameButtons(){
        Button btnPlayAgain = mRoot.findViewById(R.id.play_again);
        btnPlayAgain.setVisibility(View.VISIBLE);
        Button btnQuit = mRoot.findViewById(R.id.quit);
        btnQuit.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the "play again" and "quit" buttons from view
     */
    public static void hideEndgameButtons(){
        Button btnPlayAgain = mRoot.findViewById(R.id.play_again);
        btnPlayAgain.setVisibility(View.GONE);
        Button btnQuit = mRoot.findViewById(R.id.quit);
        btnQuit.setVisibility(View.GONE);
    }

    /**
     * Shows buttons so that users can ready up
     */
    public static void showReadyLayout(){
        Button btnPlayAgain = mRoot.findViewById(R.id.ready);
        ProgressBar indicator = mRoot.findViewById(R.id.loading_indicator);
        btnPlayAgain.setVisibility(View.VISIBLE);
        indicator.setVisibility(View.GONE);
    }

    /**
     * Hides the ready button from the user
     */
    public static void hideReadyLayout(){
        Button btnPlayAgain = mRoot.findViewById(R.id.ready);
        ProgressBar indicator = mRoot.findViewById(R.id.loading_indicator);
        btnPlayAgain.setVisibility(View.GONE);

        if(MultiplayerAccess.mOpponentReady)
            indicator.setVisibility(View.GONE);
        else
            indicator.setVisibility(View.VISIBLE);
    }

    /**
     * Notifies opponent that player is ready
     * @param v View that invoked method
     */
    public void onReady(View v)
    {
        mMultiplayerAccess.sendToAllReliably(Messages.READY_TO_START.toString());
        MultiplayerAccess.mClientReady = true;
        hideReadyLayout();
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
                        {
                            gameLoop.restartGame();
                            checking = false;
                        }
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

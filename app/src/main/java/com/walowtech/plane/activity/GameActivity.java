package com.walowtech.plane.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.multiplayer.realtime.Room;
import com.walowtech.plane.Input.GameClickListener;
import com.walowtech.plane.R;
import com.walowtech.plane.game.GameLoop;
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
    private RelativeLayout mRoot;
    private View mReplayLayout;
    private View mReadyLayout;


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

        // Set up screen
        setContentView(R.layout.activity_game);
        mRoot = findViewById(R.id.root);
        mRoot.addView(GameLoop.getCore().getGraphics());

        mReadyLayout = findViewById(R.id.ready_layout);
        mReplayLayout = findViewById(R.id.gameover_layout);

        clickListener = new GameClickListener(this);
        GameLoop.getCore().getGraphics().setOnTouchListener(clickListener);

        gameLoop.startGame();

        if(mMuliplayer)
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
     * @param pMessage The message to display
     */
    public void showEndgameButtons(String pMessage){
        TextView txtWinner = mReplayLayout.findViewById(R.id.winner_msg);
        txtWinner.setText(pMessage);
        mReplayLayout.setVisibility(LinearLayout.VISIBLE);
        mReplayLayout.bringToFront();
    }

    /**
     * Hides the "play again" and "quit" buttons from view
     */
    public void hideEndgameButtons(){
        mReplayLayout.setVisibility(View.GONE);
    }

    /**
     * Shows buttons so that users can ready up
     */
    public void showReadyLayout(){
        ProgressBar indicator = mReadyLayout.findViewById(R.id.loading_indicator);
        mReadyLayout.setVisibility(LinearLayout.VISIBLE);
        indicator.setVisibility(View.GONE);
        mReadyLayout.bringToFront();
    }

    /**
     * Hides the ready button from the user
     */
    public void hideReadyLayout(){
        View layout = mReadyLayout.findViewById(R.id.ready);
        ProgressBar indicator = mRoot.findViewById(R.id.loading_indicator);
        layout.setVisibility(View.GONE);

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
        if(mMultiplayerAccess != null)
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

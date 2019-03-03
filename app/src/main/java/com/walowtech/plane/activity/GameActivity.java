package com.walowtech.plane.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.multiplayer.realtime.Room;
import com.walowtech.plane.App;
import com.walowtech.plane.Input.GameClickListener;
import com.walowtech.plane.R;
import com.walowtech.plane.game.GameLoop;
import com.walowtech.plane.multiplayer.Messages;
import com.walowtech.plane.multiplayer.MultiplayerAccess;

import java.util.Locale;

import static com.walowtech.plane.util.GraphicUtils.animateView;

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
            mMultiplayerAccess = new MultiplayerAccess(this, this, null, mRoom, mClientParticipationID);
            gameLoop = new GameLoop(this, this, mMultiplayerAccess);
            mMultiplayerAccess.setGameLoop(gameLoop);
            MultiplayerAccess.sClientReady = false;
        }else{
            Log.i("MULTIPLAYER", "In singleplayer mode");
            // Single Player
            gameLoop = new GameLoop(this, this, false);
        }

        // Set up screen
        setContentView(R.layout.activity_game);
        mRoot = findViewById(R.id.root);
        mRoot.addView(gameLoop.getCore().getGraphics());
        ((TextView) mRoot.findViewById(R.id.winner_msg)).setTypeface(App.getFont(this));
        ((TextView) mRoot.findViewById(R.id.ready_msg)).setTypeface(App.getFont(this));
        ((TextView) mRoot.findViewById(R.id.play_again)).setTypeface(App.getFont(this));
        ((TextView) mRoot.findViewById(R.id.quit)).setTypeface(App.getFont(this));
        ((TextView) mRoot.findViewById(R.id.txt_start)).setTypeface(App.getFont(this));
        ((TextView) mRoot.findViewById(R.id.txt_ready)).setTypeface(App.getFont(this));

        mReadyLayout = findViewById(R.id.ready_layout);
        mReplayLayout = findViewById(R.id.gameover_layout);

        clickListener = new GameClickListener(this, gameLoop);
        gameLoop.getCore().getGraphics().setOnTouchListener(clickListener);

        gameLoop.startGame();

        Log.i("TEST", "SIZE: " + gameLoop.getCore().getPlayerManager().getPlayers().size());

        new Thread(() ->
        {
            while(!gameLoop.getCore().getGraphics().hasBeenDrawn())
            {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showReadyLayout();
                }
            });
        }).start();
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
        animateView(mReplayLayout.findViewById(R.id.play_again), 0);
        animateView(mReplayLayout.findViewById(R.id.winner_msg), 0);
        animateView(mReplayLayout.findViewById(R.id.quit), 0);
        mReplayLayout.bringToFront();
        ((TextView) mReplayLayout.findViewById(R.id.play_again)).setText(R.string.play_again);
    }

    /**
     * Hides the "play again" and "quit" buttons from view
     */
    public void hideEndgameButtons(){
        animateView(mReplayLayout.findViewById(R.id.play_again), (int)getResources().getDimension(R.dimen.message_small_x_start));
        animateView(mReplayLayout.findViewById(R.id.winner_msg), (int)getResources().getDimension(R.dimen.message_large_x_start));
        animateView(mReplayLayout.findViewById(R.id.quit), (int)getResources().getDimension(R.dimen.message_small_x_start));
        animateView(mReplayLayout.findViewById(R.id.ready_msg), (int)getResources().getDimension(R.dimen.message_large_x_start));
    }

    /**
     * Shows buttons so that users can ready up
     */
    public void showReadyLayout(){
        mReadyLayout.setVisibility(LinearLayout.VISIBLE);
        mReadyLayout.bringToFront();
        TextView startTxt = mReadyLayout.findViewById(R.id.txt_start);
        animateView(startTxt, 0);
    }

    /**
     * Hides the ready button from the user
     */
    public void hideReadyLayout(){
        TextView startTxt = mReadyLayout.findViewById(R.id.txt_start);
        TextView readyTxt = mReadyLayout.findViewById(R.id.txt_ready);
        animateView(readyTxt, (int)(getResources().getDimension(R.dimen.message_large_x_start)));
        animateView(startTxt, (int)(getResources().getDimension(R.dimen.message_small_x_start)));
    }

    /**
     * Notifies opponent that player is ready
     * @param v View that invoked method
     */
    public void onReady(View v)
    {
        if(mMultiplayerAccess != null)
            mMultiplayerAccess.sendToAllReliably(Messages.READY_TO_START.toString());

        MultiplayerAccess.sClientReady = true;

        // If opponent ready start
        if(MultiplayerAccess.sOpponentReady)
        {
            hideReadyLayout();
        }
        else
        {
            TextView readyTxt = mReadyLayout.findViewById(R.id.txt_ready);
            String opponentName = mMultiplayerAccess.getOpponnetName().length() <= 15 ? mMultiplayerAccess.getOpponnetName() : "OPPONENT";
            ((TextView) mReadyLayout.findViewById(R.id.txt_start)).setText(R.string.waiting);
            animateView(readyTxt, 0);
            readyTxt.setText(String.format(Locale.US, "%s IS NOT READY", opponentName.toUpperCase()));
        }

    }

    /**
     * Called when opponent is ready. Hides the ready layout if client is
     * ready, else the client is notified that the opponent is ready.
     */
    public void onOpponentReady()
    {
        if(MultiplayerAccess.sClientReady)
        {
            hideReadyLayout();
        }
        else
        {
            TextView readyTxt = mReadyLayout.findViewById(R.id.txt_ready);
            readyTxt.bringToFront();
            String opponentName = mMultiplayerAccess.getOpponnetName().length() <= 15 ? mMultiplayerAccess.getOpponnetName() : "OPPONENT";
            animateView(readyTxt, 0);
            readyTxt.setText(String.format(Locale.US, "%s IS READY", opponentName.toUpperCase()));
        }
    }

    /**
     * Called when the user requests to play again.
     * Sends replay request and listens for response.
     *
     * @param v View that invoked method
     */
    public void playAgain(View v){
        gameLoop.getCore().getMultiplayerAccess().sendToAllReliably(Messages.PLAY_AGAIN.toString());
        MultiplayerAccess.sClientPlayAgain = true;

        if (v instanceof TextView)
            ((TextView) v).setText(R.string.waiting);

        if(MultiplayerAccess.sOpponentPlayAgain){
            gameLoop.restartGame();
        }else{
            // Thread checks if opponent wants to play again
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean checking = true;
                    while(checking) {
                        if (MultiplayerAccess.sOpponentPlayAgain)
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
        MultiplayerAccess.sClientPlayAgain = false;
        gameLoop.stopGame();
        mMultiplayerAccess.leaveRoom();
    }

    private class opponentChecker implements Runnable {

        private boolean mRunning;

        @Override
        public void run() {
            mRunning = true;
            while(mRunning){

                //Log.i("MULTIPLAYER", "WAITING TO START");

                if(MultiplayerAccess.sOpponentReady){
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

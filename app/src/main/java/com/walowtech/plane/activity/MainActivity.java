package com.walowtech.plane.activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.games.Games;
import com.walowtech.plane.R;
import com.walowtech.plane.game.GameLoop;
import com.walowtech.plane.multiplayer.MultiplayerAccess;

import static com.walowtech.plane.util.GraphicUtils.animateBounce;
import static com.walowtech.plane.util.GraphicUtils.animateView;

/**
 * Activity that contains the main menu.
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2018-08-09
 */
public class MainActivity extends Activity {

    private static final int RC_SIGN_IN = 5;
    private static final int RC_SELECT_PLAYERS = 9006;
    private static final int RC_WAITING_ROOM = 9007;

    private MultiplayerAccess mMultiplayer;
    private RelativeLayout mRoot;
    private GameLoop backgroundLoop;

    private Thread mAnimationThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        backgroundLoop = new GameLoop(this, this, true);

        mMultiplayer = new MultiplayerAccess(this, this, backgroundLoop);
        mRoot = findViewById(R.id.main_activity_root);
        mRoot.addView(backgroundLoop.getCore().getGraphics());
        mRoot.findViewById(R.id.homescreen_btns).bringToFront();

        if(!mMultiplayer.isSignedIn())
            mMultiplayer.startSignInIntent();

        backgroundLoop.startGame();

        new Thread(() ->
        {
            while(!backgroundLoop.getCore().getGraphics().hasBeenDrawn())
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
                    animateView(mRoot.findViewById(R.id.homescreen_btns), 0);
                }
            });
        }).start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(MultiplayerAccess.sMustBeInitialized)
        {
            mMultiplayer = new MultiplayerAccess(this, this, backgroundLoop);
            MultiplayerAccess.sMustBeInitialized = false;
        }

        if(!mMultiplayer.isSignedIn())
            mMultiplayer.silentlySignIn();

        backgroundLoop.restartGame();
        ((TextView) mRoot.findViewById(R.id.btn_quickplay)).setText(mMultiplayer.getRoom() == null ? R.string.quick : R.string.cancel);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN) {
            mMultiplayer.onSignInResult(data);
        }

        if (requestCode == RC_SELECT_PLAYERS) {
            mMultiplayer.onSelectPlayersResult(resultCode, data);
        }

        if (requestCode == RC_WAITING_ROOM) {
            mMultiplayer.onWaitingRoomResult(resultCode);
        }
    }

    /**
     * Launches the game screen
     * @param v View that invoked method
     */
    public void startGame(View v){
        backgroundLoop.stopGame();
        Intent gameIntent = new Intent(this, GameActivity.class);
        startActivity(gameIntent);
    }

    /**
     * Starts multiplayer process by creating a multiplayer room
     * @param v VIew that invoked method
     */
    public void quickGame(View v){
        if(mMultiplayer.getRoom() == null)
        {
            mMultiplayer.createRoom(null, 1, 1);

            if(v instanceof TextView)
            {
                animateBounce(this, v, (int)getResources().getDimension(R.dimen.message_width_xsmall) * -1, 0, R.string.cancel);
            }

        }
        else
        {
            mMultiplayer.leaveRoom();

            if(v instanceof TextView)
            {
                animateBounce(this, v, (int) getResources().getDimension(R.dimen.message_width_xsmall) * -1, 0, R.string.quick);
            }
        }
    }

    /**
     * Launches activity to invite players
     * @param v View that invoked method
     */
    public void invitePlayers(View v){
        Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .getSelectOpponentsIntent(1, 1, true)
                .addOnSuccessListener(intent -> startActivityForResult(intent, RC_SELECT_PLAYERS));
    }

    public void viewInvites(View v){
        //TODO: Implement view invites
    }

    public GameLoop getLoop()
    {
        return backgroundLoop;
    }

}

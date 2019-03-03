package com.walowtech.plane.activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnSuccessListener;
import com.walowtech.plane.App;
import com.walowtech.plane.R;
import com.walowtech.plane.game.GameLoop;
import com.walowtech.plane.multiplayer.MultiplayerAccess;
import com.walowtech.plane.util.GraphicUtils;

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
    private static final int
            RC_WAITING_ROOM = 9007;
    private static final int RC_INVITATION_INBOX = 9008;

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

        ((Button) mRoot.findViewById(R.id.btn_quickplay)).setTypeface(App.getFont(this));
        ((Button) mRoot.findViewById(R.id.account_btn)).setTypeface(App.getFont(this));
        ((Button) mRoot.findViewById(R.id.btn_view_invites)).setTypeface(App.getFont(this));
        ((Button) mRoot.findViewById(R.id.btn_invite)).setTypeface(App.getFont(this));

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
    protected void onPause() {
        super.onPause();
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


        // Result of google sign in intent
        if (requestCode == RC_SIGN_IN) {
            mMultiplayer.onSignInResult(data);
        }

        // Result of invite players intent
        if (requestCode == RC_SELECT_PLAYERS) {

            // Error occurred or was cancelled
            if(resultCode != Activity.RESULT_OK)
                return;

            mMultiplayer.onSelectPlayersResult(resultCode, data);
        }

        // Result of waiting room intent
        if (requestCode == RC_WAITING_ROOM) {
            mMultiplayer.onWaitingRoomResult(resultCode);
        }

        // Result of invitation intent
        if (requestCode == RC_INVITATION_INBOX)
        {
            if(resultCode != Activity.RESULT_OK)
                return;

            mMultiplayer.onInvitationResult(data);
        }
    }

    /**
     * Called when the user is signed out. Bounce-Animates the button
     * to show a sign in button instead of sign out.
     */
    public void onSignedOut()
    {
        View view = findViewById(R.id.account_btn);
        GraphicUtils.animateBounce(this, view, -view.getWidth(), 0, R.string.sign_in);
    }

    /**
     * Called when the user is signed in. Bounce-Animates the button
     * to show a sign out button instead of sign in.
     */
    public void onSignedIn()
    {
        View view = findViewById(R.id.account_btn);
        GraphicUtils.animateBounce(this, view, -view.getWidth(), 0, R.string.sign_out);
    }

    /**
     * Launches the game screen in single-player. Only useful for debugging/developing
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
        if(App.isSignedIn() && mMultiplayer.getRoom() == null)
        {
            mMultiplayer.createRoom(null, 1, 1);

            if(v instanceof TextView)
            {
                animateBounce(this, v, (int)getResources().getDimension(R.dimen.message_width_xsmall) * -1, 0, R.string.cancel);
            }

        }
        else if(App.isSignedIn() && mMultiplayer != null)
        {
            mMultiplayer.leaveRoom();

            if(v instanceof TextView)
            {
                animateBounce(this, v, (int) getResources().getDimension(R.dimen.message_width_xsmall) * -1, 0, R.string.quick);
            }
        }
        else
        {
            if(mMultiplayer != null)
                mMultiplayer.startSignInIntent();
        }
    }

    /**
     * Launches activity to invite players
     * @param v View that invoked method
     */
    public void invitePlayers(View v){
        if(App.isSignedIn())
        {
            Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .getSelectOpponentsIntent(1, 1, true)
                    .addOnSuccessListener(intent -> startActivityForResult(intent, RC_SELECT_PLAYERS));
        }
        else
        {
            if(mMultiplayer != null)
                mMultiplayer.startSignInIntent();
        }
    }

    /**
     * Signs out the player from Google Games if signed in.
     * Signs player in if currently signed out.
     * @param v View that invoked method
     */
    public void signOut(View v)
    {
        if(mMultiplayer != null)
        {
            if(App.isSignedIn())
                mMultiplayer.signOut();
            else
                mMultiplayer.startSignInIntent();
        }
    }

    public void viewInvites(View v){
        if(App.isSignedIn())
        {
            Games.getInvitationsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .getInvitationInboxIntent()
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            startActivityForResult(intent, RC_INVITATION_INBOX);
                        }
                    });
        }
        else
        {
            if(mMultiplayer != null)
                mMultiplayer.startSignInIntent();
        }
    }

    public GameLoop getLoop()
    {
        return backgroundLoop;
    }

}

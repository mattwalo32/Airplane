package com.walowtech.plane.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnSuccessListener;
import com.walowtech.plane.R;
import com.walowtech.plane.multiplayer.MultiplayerAccess;

/**
 * Activity that contains the main menu.
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2018-08-09
 */
public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 5;
    private static final int RC_SELECT_PLAYERS = 9006;
    private static final int RC_WAITING_ROOM = 9007;

    MultiplayerAccess mMultiplayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mMultiplayer = new MultiplayerAccess(this, this);

        if(!mMultiplayer.isSignedIn())
            mMultiplayer.startSignInIntent();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!mMultiplayer.isSignedIn())
            mMultiplayer.silentlySignIn();
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
        Intent gameIntent = new Intent(this, GameActivity.class);
        startActivity(gameIntent);
    }

    /**
     * Starts multiplayer process by creating a multiplayer room
     * @param v VIew that invoked method
     */
    public void quickGame(View v){
       mMultiplayer.createRoom(null, 1, 1);
    }

    /**
     * Launches activity to invite players
     * @param v View that invoked method
     */
    public void invitePlayers(View v){
        Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .getSelectOpponentsIntent(1, 1, true)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_SELECT_PLAYERS);
                    }
                });
    }

    public void viewInvites(View v){
        //TODO: Implement view invites
    }

}

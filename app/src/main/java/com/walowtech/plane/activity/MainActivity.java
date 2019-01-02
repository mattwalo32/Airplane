package com.walowtech.plane.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.walowtech.plane.R;

import java.util.List;

/**
 * Activity that contains the main menu.
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2018-08-09
 */
public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 5;
    private static final int RC_WAITING_ROOM = 9007;
    private RealTimeMultiplayerClient client;
    private RoomConfig mJoinedRoomConfig;
    private Room mRoom;

    private RoomUpdateCallback mRoomUpdateCallback;
    private RoomStatusUpdateCallback mRoomStatusCallbackHandler;
    private OnRealTimeMessageReceivedListener mMessageReceivedHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!isSignedIn())
            startSignInIntent();

        initHandlers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isSignedIn())
            silentlySignIn();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()) {
                GoogleSignInAccount signedInAccount = result.getSignInAccount();
                client = Games.getRealTimeMultiplayerClient(this, signedInAccount);
            }else{
                String message = result.getStatus().getStatusMessage();
                if(message == null || message.isEmpty()){
                    message = "An error occurred while signing in";
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        }
        if (requestCode == RC_WAITING_ROOM) {

            // Look for finishing the waiting room from code, for example if a
            // "start game" message is received.  In this case, ignore the result.
            //if (mWaitingRoomFinishedFromCode) {
            //    return;
            //}

            if (resultCode == Activity.RESULT_OK) {
                // Start the game!
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Waiting room was dismissed with the back button. The meaning of this
                // action is up to the game. You may choose to leave the room and cancel the
                // match, or do something else like minimize the waiting room and
                // continue to connect in the background.

                // in this example, we take the simple approach and just leave the room:
                Games.getRealTimeMultiplayerClient(this,
                        GoogleSignIn.getLastSignedInAccount(this))
                        .leave(mJoinedRoomConfig, mRoom.getRoomId());
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player wants to leave the room.
                Games.getRealTimeMultiplayerClient(this,
                        GoogleSignIn.getLastSignedInAccount(this))
                        .leave(mJoinedRoomConfig, mRoom.getRoomId());
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    private void silentlySignIn(){
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if(task.isSuccessful()){
                            GoogleSignInAccount signedInAccount = task.getResult();
                            client = Games.getRealTimeMultiplayerClient(MainActivity.this, signedInAccount);
                        }else{
                            startSignInIntent();
                        }
                    }
                });
    }

    private void startSignInIntent(){
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    public void startGame(View v){
        Intent gameIntent = new Intent(this, GameActivity.class);
        startActivity(gameIntent);
    }

    public void quickGame(View v){
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(1, 1, 0);

        RoomConfig roomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mMessageReceivedHandler)
                .setRoomStatusUpdateCallback(mRoomStatusCallbackHandler)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mJoinedRoomConfig = roomConfig;

        Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this)).create(roomConfig);
    }

    public void invitePlayers(View v){

    }

    public void viewInvites(View v){

    }

    public void initHandlers(){
        mRoomUpdateCallback = new RoomUpdateCallback() {
            @Override
            public void onRoomCreated(int code, @Nullable Room room) {
                if(code == GamesCallbackStatusCodes.OK && room != null){
                    Log.i("MULTIPLAYER", "Room created");
                    mRoom = room;
                }else{
                    Log.i("MULTIPLAYER", "Error creating room");
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }

            @Override
            public void onJoinedRoom(int code, @Nullable Room room) {
                if (code == GamesCallbackStatusCodes.OK && room != null) {
                    Log.i("MULTIPLAYER", "Room " + room.getRoomId() + " joined.");
                } else {
                    Log.i("MULIPLAYER", "Error joining room: " + code);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                }
            }

            @Override
            public void onLeftRoom(int code, @NonNull String s) {
                Log.w("MULIPLAYER", "Left Room");
            }

            @Override
            public void onRoomConnected(int code, @Nullable Room room) {
                if (code == GamesCallbackStatusCodes.OK && room != null) {
                    showWaitingRoom(room, 2);
                } else {
                    Log.i("MULTIPLAYER", "Error connecting to room: " + code);
                    // let screen go to sleep
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                }
            }
        };

        mMessageReceivedHandler = new OnRealTimeMessageReceivedListener() {
            @Override
            public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {

            }
        };

        mRoomStatusCallbackHandler = new RoomStatusUpdateCallback() {
            @Override
            public void onRoomConnecting(@Nullable Room room) {

            }

            @Override
            public void onRoomAutoMatching(@Nullable Room room) {

            }

            @Override
            public void onPeerInvitedToRoom(@Nullable Room room, @NonNull List<String> list) {

            }

            @Override
            public void onPeerDeclined(@Nullable Room room, @NonNull List<String> list) {

            }

            @Override
            public void onPeerJoined(@Nullable Room room, @NonNull List<String> list) {

            }

            @Override
            public void onPeerLeft(@Nullable Room room, @NonNull List<String> list) {

            }

            @Override
            public void onConnectedToRoom(@Nullable Room room) {

            }

            @Override
            public void onDisconnectedFromRoom(@Nullable Room room) {

            }

            @Override
            public void onPeersConnected(@Nullable Room room, @NonNull List<String> list) {

            }

            @Override
            public void onPeersDisconnected(@Nullable Room room, @NonNull List<String> list) {

            }

            @Override
            public void onP2PConnected(@NonNull String s) {

            }

            @Override
            public void onP2PDisconnected(@NonNull String s) {

            }
        };
    }

    private void showWaitingRoom(Room room, int maxPlayersToStartGame){
        Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .getWaitingRoomIntent(room, maxPlayersToStartGame)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_WAITING_ROOM);
                    }
                });
    }

    public boolean isSignedIn(){
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }
}

package com.walowtech.plane.multiplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.Game;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.base.Charsets;
import com.walowtech.plane.activity.GameActivity;
import com.walowtech.plane.game.GameLoop;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/** This class manages the all the multiplayer aspect of the game.
 *
 * This class exposes methods to sign in users, create rooms,
 * handle room callbacks, invite players, leave rooms, and send messages
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2019-01-07
 */
public class MultiplayerAccess {

    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 2;
    private static final int RC_SIGN_IN = 5;
    private static final int RC_SELECT_PLAYERS = 9006;
    private static final int RC_WAITING_ROOM = 9007;

    private Context mContext;
    private static Activity mActivity;

    private String mClientParticipantId;
    private static RoomConfig mJoinedRoomConfig;
    private Room mRoom;

    private RoomUpdateCallback mRoomUpdateCallback;
    private RoomStatusUpdateCallback mRoomStatusCallbackHandler;
    private OnRealTimeMessageReceivedListener mMessageReceivedHandler;
    private RealTimeMultiplayerClient.ReliableMessageSentCallback mHandleMessageSentCallback;

    public static boolean mPlaying = false;
    public static boolean mClientReady = false;
    public static boolean mOpponentReady = false;
    public static boolean mStartingTopLeft = false;
    public static boolean mClientPlayAgain = false;
    public static boolean mOpponentPlayAgain = false;
    private boolean mWaitingRoomFinishedFromCode = false;

    public MultiplayerAccess(Context pContext, Activity pActivity){
        mContext = pContext;
        mActivity = pActivity;

        initHandlers();
    }

    public MultiplayerAccess(Context pContext, Activity pActivity, Room pRoom, String pClientParticipationId){
        this(pContext, pActivity);

        mRoom = pRoom;
        mClientParticipantId = pClientParticipationId;
    }

    public void silentlySignIn(){
        GoogleSignInClient signInClient = GoogleSignIn.getClient(mContext,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.silentSignIn().addOnCompleteListener(mActivity,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(mContext, "Signed In", Toast.LENGTH_SHORT).show();
                        }else{
                            startSignInIntent();
                        }
                    }
                });
    }

    public void startSignInIntent(){
        GoogleSignInClient signInClient = GoogleSignIn.getClient(mContext,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Intent intent = signInClient.getSignInIntent();
        mActivity.startActivityForResult(intent, RC_SIGN_IN);
    }

    private void initHandlers(){
        mRoomUpdateCallback = new RoomUpdateCallback() {
            @Override
            public void onRoomCreated(int code, @Nullable Room room) {
                if(code == GamesCallbackStatusCodes.OK && room != null){
                    Toast.makeText(mContext, "Room Created", Toast.LENGTH_SHORT).show();
                    mRoom = room;
                }else{
                    Toast.makeText(mContext, "Error creating room, please try again.", Toast.LENGTH_SHORT).show();
                    mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }

            @Override
            public void onJoinedRoom(int code, @Nullable Room room) {
                if (code == GamesCallbackStatusCodes.OK && room != null) {
                    Toast.makeText(mContext, "Joined Room " + room.getRoomId(), Toast.LENGTH_SHORT).show();
                    showWaitingRoom(room, 2);
                } else {
                    Toast.makeText(mContext, "Error joining room " + room.getRoomId() + ", please try again.", Toast.LENGTH_SHORT).show();
                    mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }

            @Override
            public void onLeftRoom(int code, @NonNull String s) {
                Toast.makeText(mContext, "Room has been left.", Toast.LENGTH_SHORT).show();

                Log.i("TEST", "INSTANCE OF" + (mActivity instanceof GameActivity));

                if(mActivity instanceof GameActivity)
                    ((GameActivity) mActivity).exitMatch();
            }

            @Override
            public void onRoomConnected(int code, @Nullable Room room) {
                if (code == GamesCallbackStatusCodes.OK && room != null) {
                    Toast.makeText(mContext, "Connected to room", Toast.LENGTH_SHORT).show();
                    showWaitingRoom(mRoom, 2);
                } else {
                    Toast.makeText(mContext, "Error connecting to room: " + code, Toast.LENGTH_SHORT).show();
                    mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                }
            }
        };

        mMessageReceivedHandler = new OnRealTimeMessageReceivedListener() {
            @Override
            public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
                byte[] bytes = realTimeMessage.getMessageData();
                String message = new String(bytes, Charsets.UTF_8);
                Log.i("MULTIPLAYER", "Message Recieved: " + message);

                if(!mPlaying && message.equals(Messages.START_GAME.toString())){
                    // Other device started the game
                    onStartGameMessage();
                }else if(message.equals(Messages.READY_TO_START.toString())){
                    // Other device has loaded screen and is ready to start
                    mOpponentReady = true;
                    if(mClientReady){
                        // This device has loaded screen and is ready to start
                        Log.i("MULTIPLAYER", "Telling to start now");
                        sendToAllReliably(Messages.START_NOW.toString());
                    }else{
                        Log.i("MULTIPLAYER", "Client is not ready");
                    }
                }else if(message.equals(Messages.START_NOW.toString())){
                    // Both devices are ready, the other one is starting
                    mOpponentReady = true;
                    //mClientReady = true;
                }else if(message.equals(Messages.PLAY_AGAIN.toString())) {
                    mOpponentPlayAgain = true;
                }else if(message.equals(Messages.COLLIDED.toString())){
                    if(GameLoop.mRunning)
                        GameLoop.getCore().stop();
                }else if(message.matches("[[-]*[0-9]+,]*")){
                    MessageUtils.parseMessage(message, GameLoop.getCore().getPlayerManager().getPlayers().get(1).getPlane());
                }
            }
        };

        mRoomStatusCallbackHandler = new RoomStatusUpdateCallback() {
            @Override
            public void onRoomConnecting(@Nullable Room room) {
                //TODO: Add loading dialog
                Toast.makeText(mContext, "Connecting to a multiplayer room", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRoomAutoMatching(@Nullable Room room) {
                //TODO: Add loading dialog
                Toast.makeText(mContext, "Auto matching to room", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPeerInvitedToRoom(@Nullable Room room, @NonNull List<String> list) {
                Toast.makeText(mContext, "Invite Sent", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPeerDeclined(@Nullable Room room, @NonNull List<String> list) {
                Toast.makeText(mContext, "Your invite was declined", Toast.LENGTH_LONG).show();
                if(!mPlaying && shouldCancelGame(room)) {
                    leaveRoom();
                }
            }

            @Override
            public void onPeerJoined(@Nullable Room room, @NonNull List<String> list) {
                Toast.makeText(mContext, "Friend Joined", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPeerLeft(@Nullable Room room, @NonNull List<String> list) {
                Toast.makeText(mContext, "Friend Left", Toast.LENGTH_SHORT).show();
                if(!mPlaying && shouldCancelGame(room)) {
                    leaveRoom();
                }
            }

            @Override
            public void onConnectedToRoom(@Nullable Room room) {
                Log.i("MULTIPLAYER", "Connected to room");
                mRoom = room;
                Games.getPlayersClient(mActivity, GoogleSignIn.getLastSignedInAccount(mActivity))
                        .getCurrentPlayerId().addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String playerId) {
                        mClientParticipantId = mRoom.getParticipantId(playerId);
                        Log.i("MULTIPLAYER", "USER ID: " + mClientParticipantId);
                        startGame();
                    }
                });
            }

            @Override
            public void onDisconnectedFromRoom(@Nullable Room room) {
                leaveRoom();
                mRoom = null;
                mJoinedRoomConfig = null;
            }

            @Override
            public void onPeersConnected(@Nullable Room room, @NonNull List<String> list) {
                if(mPlaying){
                    //TODO: Handle if player joins ongoing game
                }else if(shouldStartGame(room)){
                    startGame();
                    sendToAllReliably(Messages.START_GAME.toString());
                }
            }

            @Override
            public void onPeersDisconnected(@Nullable Room room, @NonNull List<String> list) {
                if(mPlaying){
                    //TODO: Handle player leaving mid-game
                    if(shouldCancelGame(room)){
                        leaveRoom();
                    }
                } else if(shouldCancelGame(room)){
                    leaveRoom();
                }

            }

            @Override
            public void onP2PConnected(@NonNull String s) {

            }

            @Override
            public void onP2PDisconnected(@NonNull String s) {

            }
        };
    }

    /**
     * Leave current room
     */
    public void leaveRoom(){
        try
        {
            Games.getRealTimeMultiplayerClient(mActivity, GoogleSignIn.getLastSignedInAccount(mActivity))
                    .leave(mJoinedRoomConfig, mRoom.getRoomId());
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
        }
    }

    public void onSignInResult(Intent data){
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if(result.isSuccess()) {
            GoogleSignInAccount signedInAccount = result.getSignInAccount();
            Toast.makeText(mContext, "Signed In", Toast.LENGTH_SHORT).show();
        }else{
            String message = result.getStatus().getStatusMessage();
            if(message == null || message.isEmpty()){
                message = "An error occurred while signing in";
            }
            new AlertDialog.Builder(mContext).setMessage(message)
                    .setNeutralButton(android.R.string.ok, null).show();
        }
    }

    public void onSelectPlayersResult(int resultCode, Intent data){
        if (resultCode != Activity.RESULT_OK) {
            // TODO: Handle error/display message
            return;
        }

        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        int minAutoPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

        createRoom(invitees, 1, maxAutoPlayers);
    }

    public void onWaitingRoomResult(int resultCode){
        // Look for finishing the waiting room from code, for example if a
        // "start game" message is received.  In this case, ignore the result.
        if (mWaitingRoomFinishedFromCode) {
            startGame();
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            startGame();
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // Waiting room was dismissed with the back button. The meaning of this
            // action is up to the game. You may choose to leave the room and cancel the
            // match, or do something else like minimize the waiting room and
            // continue to connect in the background.

            // in this example, we take the simple approach and just leave the room:
            leaveRoom();
            Toast.makeText(mContext, "Match Cancelled back button pressed", Toast.LENGTH_SHORT).show();
        } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
            // player wants to leave the room.
            leaveRoom();
            Toast.makeText(mContext, "Match Cancelled cancel pressed", Toast.LENGTH_SHORT).show();
        }
    }

    public void createRoom(ArrayList<String> pInvitees, int pMinAutoPlayers, int pMaxAutoPlayers){
        // Create the room configuration.

        RoomConfig.Builder roomBuilder = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mMessageReceivedHandler)
                .setRoomStatusUpdateCallback(mRoomStatusCallbackHandler);

        if(pInvitees != null)
                roomBuilder.addPlayersToInvite(pInvitees);

        if (pMinAutoPlayers > 0) {
            roomBuilder.setAutoMatchCriteria(
                    RoomConfig.createAutoMatchCriteria(pMinAutoPlayers, pMaxAutoPlayers, 0));
        }

        mJoinedRoomConfig = roomBuilder.build();
        Games.getRealTimeMultiplayerClient(mActivity, GoogleSignIn.getLastSignedInAccount(mActivity))
        .create(mJoinedRoomConfig);
    }

    private void showWaitingRoom(Room room, int maxPlayersToStartGame){
        Games.getRealTimeMultiplayerClient(mActivity, GoogleSignIn.getLastSignedInAccount(mActivity))
        .getWaitingRoomIntent(room, maxPlayersToStartGame)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        mActivity.startActivityForResult(intent, RC_WAITING_ROOM);
                    }
                });
    }

    public void sendToAllReliably(final String pMessage){
        byte[] bytes = pMessage.getBytes(Charsets.UTF_8);

        for(String participantId : mRoom.getParticipantIds()){
            if(!participantId.equals(mClientParticipantId)){
                Task<Integer> task = Games.
                        getRealTimeMultiplayerClient(mActivity, GoogleSignIn.getLastSignedInAccount(mActivity))
                        .sendReliableMessage(bytes, mRoom.getRoomId(), participantId, null).addOnCompleteListener(new OnCompleteListener<Integer>() {
                            @Override
                            public void onComplete(@NonNull Task<Integer> task) {
                            }
                        });
            }
        }
    }

    public void sendToAll(final String pMessage){
        byte[] bytes = pMessage.getBytes(Charsets.UTF_8);

        Games.getRealTimeMultiplayerClient(mActivity, GoogleSignIn.getLastSignedInAccount(mActivity))
                        .sendUnreliableMessageToOthers(bytes, mRoom.getRoomId());

    }

    private void onStartGameMessage(){
        mWaitingRoomFinishedFromCode = true;
        mActivity.finishActivity(RC_WAITING_ROOM);
    }

    private void startGame(){
        if(!mPlaying && mClientParticipantId != null) {
            Toast.makeText(mContext, "Starting Match", Toast.LENGTH_SHORT).show();
            determineStartinPosition();
            Intent gameIntent = new Intent(mContext, GameActivity.class);
            gameIntent.putExtra("ROOM", mRoom);
            gameIntent.putExtra("USERID", mClientParticipantId);
            mActivity.startActivity(gameIntent);
            mPlaying = true;
        }
    }

    /**
     * Determines where players start based on their unique player ID
     */
    private void determineStartinPosition()
    {
        char[] opponentID = null;
        char[] clientID = mClientParticipantId.toCharArray();
        ArrayList<String> playerIDs = mRoom.getParticipantIds();

        for(String id : playerIDs)
        {
            if(!id.equals(mClientParticipantId))
                opponentID = id.toCharArray();
        }

        if(opponentID == null)
        {
            mStartingTopLeft = true;
            return;
        }

        if(clientID.length == opponentID.length)
        {
            for (int i = 0; i < clientID.length; i++) {
                if (Character.getNumericValue(opponentID[i]) != Character.getNumericValue(clientID[i]))
                {
                    mStartingTopLeft = Character.getNumericValue(opponentID[i]) > Character.getNumericValue(clientID[i]);
                }
            }
        }
        else
        {
            mStartingTopLeft = clientID.length > opponentID.length;
        }
    }

    public boolean isSignedIn(){
        return GoogleSignIn.getLastSignedInAccount(mContext) != null;
    }

    public Room getRoom(){
        return mRoom;
    }

    private boolean shouldStartGame(Room room){
        int connectedPlayers = 0;
        for(Participant p : room.getParticipants()){
            if(p.isConnectedToRoom()){
                ++connectedPlayers;
            }
        }

        return connectedPlayers >= MIN_PLAYERS && !mPlaying;
    }

    public  boolean shouldCancelGame(Room room) {

        int activeParticipants = 0;

        for(Participant p : room.getParticipants()){
            if(p.getStatus() == Participant.STATUS_JOINED || p.getStatus() == Participant.STATUS_INVITED){
                ++activeParticipants;
            }
        }

        //TODO: Add cancel button to UI
        return activeParticipants >= MIN_PLAYERS;
    }
}

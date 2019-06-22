package com.walowtech.plane.multiplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import com.google.android.gms.games.multiplayer.Invitation;
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
import com.walowtech.plane.App;
import com.walowtech.plane.activity.GameActivity;
import com.walowtech.plane.activity.MainActivity;
import com.walowtech.plane.game.GameLoop;
import com.walowtech.plane.game.GameResult;

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

    public static boolean sPlaying = false;
    public static boolean sClientReady = false;
    public static boolean sOpponentReady = false;
    public static boolean sStartingTopLeft = false;
    public static boolean sClientPlayAgain = false;
    public static boolean sOpponentPlayAgain = false;
    public static boolean sMustBeInitialized = false;

    private Context mContext;
    private static Activity mActivity;

    private String mClientParticipantId;
    private static RoomConfig mJoinedRoomConfig;
    private Room mRoom;
    private static GameLoop mGameLoop;

    private RoomUpdateCallback mRoomUpdateCallback;
    private RoomStatusUpdateCallback mRoomStatusCallbackHandler;
    private OnRealTimeMessageReceivedListener mMessageReceivedHandler;
    private RealTimeMultiplayerClient.ReliableMessageSentCallback mHandleMessageSentCallback;
    private boolean mWaitingRoomFinishedFromCode = false;

    /**
     * Default constructor
     * @param pContext Calling context
     * @param pActivity Calling activity
     * @param pGameLoop GameLoop that owns this object
     */
    public MultiplayerAccess(Context pContext, Activity pActivity, GameLoop pGameLoop){
        mContext = pContext;
        mActivity = pActivity;
        mGameLoop = pGameLoop;

        initHandlers();
    }

    /**
     * Constructor for a room that is already created
     * @param pContext Calling context
     * @param pActivity Calling activity
     * @param pGameLoop GameLoop that owns this object
     * @param pRoom The multiplayer room object that is currently in use
     * @param pClientParticipationId The client participation DI for the match in progress
     */
    public MultiplayerAccess(Context pContext, Activity pActivity, GameLoop pGameLoop, Room pRoom, String pClientParticipationId){
        this(pContext, pActivity, pGameLoop);

        mRoom = pRoom;
        mClientParticipantId = pClientParticipationId;
    }

    /**
     * Signs in the user in the background without notifying them
     */
    public void silentlySignIn(){
        GoogleSignInClient signInClient = GoogleSignIn.getClient(mContext,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);

        signInClient.silentSignIn().addOnCompleteListener(mActivity, task -> {
            if(task.isSuccessful()){
                Toast.makeText(mContext, "Signed In", Toast.LENGTH_SHORT).show();
                App.setSignedIn(true);
                checkForInvitation();
            }else{
                startSignInIntent();
            }
        });
    }

    /**
     * Signs in the user with a dialog
     */
    public void startSignInIntent(){
        GoogleSignInClient signInClient = GoogleSignIn.getClient(mContext,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);

        Intent intent = signInClient.getSignInIntent();
        mActivity.startActivityForResult(intent, RC_SIGN_IN);
    }

    /**
     * Checks if the user was invited to a room and prompts user to accept
     */
    public void checkForInvitation()
    {
        Games.getGamesClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                .getActivationHint()
                .addOnSuccessListener(
                        new OnSuccessListener<Bundle>() {
                            @Override
                            public void onSuccess(Bundle bundle) {
                                if(bundle != null)
                                {
                                    Invitation invitation = bundle.getParcelable(Multiplayer.EXTRA_INVITATION);
                                    if (invitation != null) {
                                        RoomConfig.Builder builder = RoomConfig.builder(mRoomUpdateCallback)
                                                .setInvitationIdToAccept(invitation.getInvitationId());
                                        mJoinedRoomConfig = builder.build();
                                        Games.getRealTimeMultiplayerClient(mActivity,
                                                GoogleSignIn.getLastSignedInAccount(mActivity))
                                                .join(mJoinedRoomConfig);
                                        // prevent screen from sleeping during handshake
                                        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                    }
                                }
                            }
                        }
                );
    }

    /**
     * Creates the handlers and callbacks associated with multiplayer events.
     */
    private void initHandlers(){
        mRoomUpdateCallback = new RoomUpdateCallback() {
            @Override
            public void onRoomCreated(int code, @Nullable Room room) {
                if(code == GamesCallbackStatusCodes.OK && room != null){
                    mRoom = room;
                    Toast.makeText(mContext, "Room Created", Toast.LENGTH_SHORT).show();
                    showWaitingRoom(room, MAX_PLAYERS);
                }else{
                    Toast.makeText(mContext, "Error creating room, please try again.", Toast.LENGTH_SHORT).show();
                    mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }

            @Override
            public void onJoinedRoom(int code, @Nullable Room room) {
                if (code == GamesCallbackStatusCodes.OK && room != null) {
                    Toast.makeText(mContext, "Joined Room " + room.getRoomId(), Toast.LENGTH_SHORT).show();
                    showWaitingRoom(room, MAX_PLAYERS);
                } else {
                    Toast.makeText(mContext, "Error joining room " + room.getRoomId() + ", please try again.", Toast.LENGTH_SHORT).show();
                    mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }

            @Override
            public void onLeftRoom(int code, @NonNull String s) {
                Toast.makeText(mContext, "Room has been left", Toast.LENGTH_SHORT).show();

                if(mActivity instanceof GameActivity)
                {
                    ((GameActivity) mActivity).exitMatch();
                    leaveRoom();
                    resetState();
                }
            }

            @Override
            public void onRoomConnected(int code, @Nullable Room room) {
                if (code == GamesCallbackStatusCodes.OK && room != null) {
                    Toast.makeText(mContext, "Connected to room", Toast.LENGTH_SHORT).show();
                    showWaitingRoom(mRoom, MAX_PLAYERS);
                } else {
                    Toast.makeText(mContext, "Error connecting to room: " + code, Toast.LENGTH_SHORT).show();
                    mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }
        };

        mMessageReceivedHandler = realTimeMessage -> {
            byte[] bytes = realTimeMessage.getMessageData();
            String message = new String(bytes, Charsets.UTF_8);

            Log.i("MULTIPLAYER", "Message Recieved: " + message);

            if(!sPlaying && message.equals(Messages.START_GAME.toString())){
                // Other device started the game
                onStartGameMessage();
            }else if(message.equals(Messages.READY_TO_START.toString())){
                // Other device has loaded screen and is ready to start
                sOpponentReady = true;
                if(sClientReady){
                    // This device has loaded screen and is ready to start
                    Log.i("MULTIPLAYER", "Telling to start now");
                    sendToAllReliably(Messages.START_NOW.toString());
                }else{
                    Log.i("MULTIPLAYER", "Client is not ready");
                }
            }else if(message.equals(Messages.START_NOW.toString())){
                // Both devices are ready, the other one is starting
                sOpponentReady = true;
            }else if(message.equals(Messages.PLAY_AGAIN.toString())) {
                sOpponentPlayAgain = true;
            }else if(message.equals(Messages.COLLIDED.toString())){
                if(mGameLoop.mRunning)
                    mGameLoop.getCore().stop(GameResult.WON);
            }else if(message.equals(Messages.BOTH_COLLIDED.toString())){
                if(mGameLoop.mRunning)
                    mGameLoop.getCore().stop(GameResult.TIE);
            }else if(message.matches("[[-]*[0-9]+,]*")){
                Log.i("MULTIPLAYER", "SIZE " + mGameLoop.getCore().getPlayerManager().getPlayers().size());
                MessageUtils.parseMessage(message, mGameLoop.getCore().getPlayerManager().getPlayers().get(1).getPlane());
            }
        };

        mRoomStatusCallbackHandler = new RoomStatusUpdateCallback() {
            @Override
            public void onRoomConnecting(@Nullable Room room) {
                //TODO: Add loading dialog
                Toast.makeText(mContext, "Connecting to a multiplayer room", Toast.LENGTH_LONG).show();
                mRoom = room;
            }

            @Override
            public void onRoomAutoMatching(@Nullable Room room) {
                //TODO: Add loading dialog
                Toast.makeText(mContext, "Auto matching to room", Toast.LENGTH_LONG).show();
                mRoom = room;
            }

            @Override
            public void onPeerInvitedToRoom(@Nullable Room room, @NonNull List<String> list) {
                Toast.makeText(mContext, "Invite Sent", Toast.LENGTH_SHORT).show();
                mRoom = room;
            }

            @Override
            public void onPeerDeclined(@Nullable Room room, @NonNull List<String> list) {
                Toast.makeText(mContext, "Your invite was declined", Toast.LENGTH_LONG).show();
                mRoom = room;
                if(!sPlaying && shouldCancelGame(room)) {
                    leaveRoom();
                }
            }

            @Override
            public void onPeerJoined(@Nullable Room room, @NonNull List<String> list) {
                Toast.makeText(mContext, "Friend Joined", Toast.LENGTH_SHORT).show();
                mRoom = room;
            }

            @Override
            public void onPeerLeft(@Nullable Room room, @NonNull List<String> list) {
                Toast.makeText(mContext, "Friend Left", Toast.LENGTH_SHORT).show();
                mRoom = room;
                if(!sPlaying && shouldCancelGame(room)) {
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
                mRoom = room;
                leaveRoom();
                mRoom = null;
                mJoinedRoomConfig = null;
            }

            @Override
            public void onPeersConnected(@Nullable Room room, @NonNull List<String> list) {
                Toast.makeText(mActivity, "Friend Joined Game", Toast.LENGTH_LONG).show();
                mRoom = room;
                if(sPlaying){
                    //TODO: Handle if player joins ongoing game
                }else if(shouldStartGame(room)){
                    startGame();
                    sendToAllReliably(Messages.START_GAME.toString());
                }
            }

            @Override
            public void onPeersDisconnected(@Nullable Room room, @NonNull List<String> list) {
                mRoom = room;
                if(sPlaying){
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
            mRoom = null;
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This should be called when the user has been logged in successfully. The user
     * will be notified of their login status and any game invitation will be checked for.
     * @param data The data from the intent result
     */
    public void onSignInResult(Intent data){
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if(result.isSuccess()) {
            GoogleSignInAccount signedInAccount = result.getSignInAccount();
            App.setSignedIn(true);

            if(mActivity instanceof MainActivity)
                ((MainActivity) mActivity).onSignedIn();

            Toast.makeText(mContext, "Signed In", Toast.LENGTH_SHORT).show();
            checkForInvitation();
        }else{
//            String message = result.getStatus().getStatusMessage();
//            if(message == null || message.isEmpty()){
//                message = "An error occurred while signing in";
//            }
//            new AlertDialog.Builder(mContext).setMessage(message)
//                    .setNeutralButton(android.R.string.ok, null).show();
        }
    }

    /**
     * This should be called after the user has been returned from the invitation intent.
     * The user will be connected to the game if they accepted an invitation.
     * @param data The data from the intent result
     */
    public void onInvitationResult(Intent data)
    {
        Invitation invitation = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);
        if (invitation != null)
        {
            RoomConfig.Builder builder = RoomConfig.builder(mRoomUpdateCallback)
                    .setOnMessageReceivedListener(mMessageReceivedHandler)
                    .setRoomStatusUpdateCallback(mRoomStatusCallbackHandler)
                    .setInvitationIdToAccept(invitation.getInvitationId());
            mJoinedRoomConfig = builder.build();
            Games.getRealTimeMultiplayerClient(mActivity,
                    GoogleSignIn.getLastSignedInAccount(mContext))
                    .join(mJoinedRoomConfig);
            // prevent screen from sleeping during handshake
            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * Called to invite players to a room.
     * @param resultCode The result code of the invitation intent
     * @param data The data from the invitation intent
     */
    public void onSelectPlayersResult(int resultCode, Intent data){

        //Get invitees
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        int minAutoPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

        createRoom(invitees, minAutoPlayers, maxAutoPlayers);
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
        if(!sPlaying && mClientParticipantId != null) {
            Toast.makeText(mContext, "Starting Match", Toast.LENGTH_SHORT).show();
            determineStartinPosition();
            if(mActivity instanceof MainActivity)
                ((MainActivity) mActivity).getLoop().stopGame();
            Intent gameIntent = new Intent(mContext, GameActivity.class);
            gameIntent.putExtra("ROOM", mRoom);
            gameIntent.putExtra("USERID", mClientParticipantId);
            mActivity.startActivity(gameIntent);
            sPlaying = true;
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
            sStartingTopLeft = true;
            return;
        }

        if(clientID.length == opponentID.length)
        {
            for (int i = 0; i < clientID.length; i++) {
                if (Character.getNumericValue(opponentID[i]) != Character.getNumericValue(clientID[i]))
                {
                    sStartingTopLeft = Character.getNumericValue(opponentID[i]) > Character.getNumericValue(clientID[i]);
                }
            }
        }
        else
        {
            sStartingTopLeft = clientID.length > opponentID.length;
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

        return connectedPlayers >= MIN_PLAYERS && !sPlaying;
    }

    /**
     * Signs out the current user
     */
    public void signOut()
    {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(mContext,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.signOut().addOnCompleteListener(mActivity,
                (task -> {
                    App.setSignedIn(false);
                    Toast.makeText(mContext, "Signed Out", Toast.LENGTH_SHORT).show();
                    if(mActivity instanceof MainActivity)
                        ((MainActivity) mActivity).onSignedOut();
                }));
    }

    public boolean shouldCancelGame(Room room) {

        int activeParticipants = 0;

        for(Participant p : room.getParticipants()){
            if(p.getStatus() == Participant.STATUS_JOINED || p.getStatus() == Participant.STATUS_INVITED){
                ++activeParticipants;
            }
        }

        //TODO: Add cancel button to UI
        return activeParticipants >= MIN_PLAYERS;
    }

    private void resetState()
    {
        sMustBeInitialized = true;
        mRoom = null;
        sPlaying = false;
        mJoinedRoomConfig = null;
        sClientPlayAgain = false;
        sOpponentPlayAgain = false;
        sClientReady = false;
        sOpponentReady = false;
        sStartingTopLeft = true;
        mWaitingRoomFinishedFromCode = false;
        mClientParticipantId = null;
        mActivity = null;
    }

    /**
     * Grabs the opponent's display name
     * @return Display name of opponent
     */
    public String getOpponnetName()
    {
        for(Participant participant : mRoom.getParticipants())
        {
            if(!participant.getParticipantId().equals(mClientParticipantId))
                return participant.getDisplayName();
        }

        return null;
    }

    /**
     * Grabs the client's display name
     * @return Display name of client
     */
    public String getClientName()
    {
        return mRoom.getParticipant(mClientParticipantId).getDisplayName();
    }

    public void setGameLoop(GameLoop mGameLoop) {
        this.mGameLoop = mGameLoop;
    }
}

package com.walowtech.plane.game;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.walowtech.plane.activity.GameActivity;
import com.walowtech.plane.collision.CollisionDetector;
import com.walowtech.plane.data.GameComponents;
import com.walowtech.plane.multiplayer.Messages;
import com.walowtech.plane.multiplayer.MultiplayerAccess;
import com.walowtech.plane.player.PlayerManager;
import com.walowtech.plane.util.CodeIntegrityUtils;

import java.util.ArrayList;

/**
 * Manages the execution of the GameComponents.
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2018-08-09
 */
public class GameCore {

    private ArrayList<GameComponent> mGameComponents = new ArrayList<>();
    private MultiplayerAccess mMultiplayerAccess;
    private Activity mCallingActivity;
    private boolean mDisplayMode;

    public GameCore(Context pContext, Activity pActivity, boolean pDisplayMode){
        CodeIntegrityUtils.checkNotNull(pContext, "Context cannot be null");
        mCallingActivity = pActivity;
        mDisplayMode = pDisplayMode;
        addGameComponent(new PlayerManager(pContext, pDisplayMode));
        addGameComponent(new GameGraphics(pContext, pActivity));
    }

    public GameCore(Context pContext, Activity pActivity, MultiplayerAccess pMultiplayerAccess){
        this(pContext, pActivity, false);
        mMultiplayerAccess = pMultiplayerAccess;
    }

    /**
     * Initializes all game components
     */
    public void init(){
        for(GameComponent component : new ArrayList<>(mGameComponents))
            component.init();

        if(!mDisplayMode)
        {
            addGameComponent(new CollisionDetector(getPlayerManager().getPlayers().get(0).getPlane()));
            mGameComponents.get(mGameComponents.size() - 1).init();
            MultiplayerAccess.sClientPlayAgain = false;
            MultiplayerAccess.sOpponentPlayAgain = false;
        }
    }

    /**
     * Updates all game components
     */
    public void executeUpdate(){
        for(GameComponent component : mGameComponents)
            component.update();
    }

    /**
     * Stops all game components and game loop
     * @param pResult Result of game (ie. won, lost, tied)
     */
    public void stop(final GameResult pResult){
        String winner = "";

        for(GameComponent component : mGameComponents) {
            component.stop();
        }

        GameLoop.stopGame();

        if(getMultiplayerAccess() != null){
            if(pResult.getCode() == GameResult.TIE.getCode())
            {
                getMultiplayerAccess().sendToAllReliably(Messages.BOTH_COLLIDED.toString());
                winner = "You both crashed!";
            }
            else
            {
                getMultiplayerAccess().sendToAllReliably(Messages.COLLIDED.toString());
                winner = (GameResult.WON.getCode() == pResult.getCode() ? mMultiplayerAccess.getClientName() : mMultiplayerAccess.getOpponnetName()) + " won!";
            }
        }

        final String winnerMessage = winner.toUpperCase();

        mCallingActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mCallingActivity instanceof GameActivity)
                    ((GameActivity)mCallingActivity).showEndgameButtons(winnerMessage);
            }
        });
        //Log.i("TEST", "Result: " + (pResult.getCode() == GameResult.WON.getCode() ? getMultiplayerAccess().getClientName() : getMultiplayerAccess().getOpponnetName()) + " won");
    }

    /**
     * Adds components to list of game components
     * @param pComponent Component to add
     */
    public void addGameComponent(GameComponent pComponent){
        CodeIntegrityUtils.checkNotNull(pComponent, "Component must not be null");
        mGameComponents.add(pComponent);
    }

    public PlayerManager getPlayerManager(){
        return (PlayerManager) get(GameComponents.PLAYER_MANAGER);
    }

    public GameGraphics getGraphics(){
        return (GameGraphics) get(GameComponents.GRAPHICS);
    }

    public Activity getCallingActivity() {
        return mCallingActivity;
    }

    /**
     * Loops through all components and retrieves desired component
     * @param component Component to get
     * @return Component that was requested
     */
    private GameComponent get(GameComponents component)
    {
        for(GameComponent gameComponent : mGameComponents){
            if(gameComponent.getName().equals(component.getName()))
                return gameComponent;
        }
        throw new NullPointerException(component.getName() + " not found");
    }

    public MultiplayerAccess getMultiplayerAccess() {
        return mMultiplayerAccess;
    }
}

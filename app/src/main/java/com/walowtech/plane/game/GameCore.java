package com.walowtech.plane.game;

import android.app.Activity;
import android.content.Context;

import com.walowtech.plane.collision.CollisionDetector;
import com.walowtech.plane.data.GameComponents;
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

    public GameCore(Context pContext, Activity pActivity){
        CodeIntegrityUtils.checkNotNull(pContext, "Context cannot be null");
        addGameComponent(new PlayerManager(pContext));
        addGameComponent(new GameGraphics(pContext, pActivity));
        //TODO: Add components
    }

    public GameCore(Context pContext, Activity pActivity, MultiplayerAccess pMultiplayerAccess){
        this(pContext, pActivity);
        mMultiplayerAccess = pMultiplayerAccess;
    }

    /**
     * Initializes all game components
     */
    public void init(){
        for(GameComponent component : mGameComponents)
            component.init();

        addGameComponent(new CollisionDetector(getPlayerManager().getPlayers().get(0).getPlane()));
        mGameComponents.get(mGameComponents.size() - 1).init();
        MultiplayerAccess.mClientPlayAgain = false;
        MultiplayerAccess.mOpponentPlayAgain = false;
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
     */
    public void stop(){
        for(GameComponent component : mGameComponents) {
            component.stop();
        }

        GameLoop.stopGame();
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

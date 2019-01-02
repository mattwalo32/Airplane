package com.walowtech.plane.game;

import android.content.Context;
import android.util.Log;

import com.walowtech.plane.collision.CollisionDetector;
import com.walowtech.plane.data.GameComponents;
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

    public GameCore(Context pContext){
        CodeIntegrityUtils.checkNotNull(pContext, "Context cannot be null");
        addGameComponent(new PlayerManager(pContext));
        addGameComponent(new GameGraphics(pContext));
        //TODO: Add components
    }

    public void init(){
        for(GameComponent component : mGameComponents)
            component.init();

        addGameComponent(new CollisionDetector(getPlayerManager().getPlayers().get(0).getPlane()));
        mGameComponents.get(mGameComponents.size() - 1).init();
    }

    public void executeUpdate(){
        for(GameComponent component : mGameComponents)
            component.update();
    }

    public void stop(){
        for(GameComponent component : mGameComponents)
            component.stop();

        GameLoop.stopGame();
    }

    public void addGameComponent(GameComponent pComponent){
        CodeIntegrityUtils.checkNotNull(pComponent, "Component must not be null");
        mGameComponents.add(pComponent);
    }

    public PlayerManager getPlayerManager(){
        for(GameComponent component : mGameComponents){
            if(component.getName().equals(GameComponents.PLAYER_MANAGER.getName()))
                return (PlayerManager) component;
        }
        throw new NullPointerException("Player Manager not found");
    }

    public GameGraphics getGraphics(){
        for(GameComponent component : mGameComponents){
            if(component.getName().equals(GameComponents.GRAPHICS.getName()))
                return (GameGraphics) component;
        }
        throw new NullPointerException("Player Manager not found");
    }

//    public GameComponent getGameComponent(String componentName){
//        CodeIntegrityUtils.checkNotEmpty(componentName, "Component name must be provided");
//        for(GameComponent component : mGameComponents){
//            if(component.getName().equals(componentName))
//                return component;
//        }
//
//        throw new NullPointerException("There is no game component with the name, " + componentName);
//    }
}

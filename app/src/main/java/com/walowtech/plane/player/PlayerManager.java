package com.walowtech.plane.player;

import android.content.Context;
import android.graphics.RectF;

import com.walowtech.plane.game.GameComponent;
import com.walowtech.plane.data.GameComponents;
import com.walowtech.plane.util.CodeIntegrityUtils;
import com.walowtech.plane.util.ConversionUtils;

import java.util.ArrayList;

/**
 * Manages all players on the screen.
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2018-08-09
 */
public class PlayerManager implements GameComponent {

    public static final int BASE_PLAYER_SPEED = 180;
    public static final int BASE_PLAYER_TURN_SPEED = 130;
    public static final int BASE_TAIL_WIDTH = 3;
    public static final int RIGHT_GAME_BOUND = 1000;
    public static final int BOTTOM_GAME_BOUND = 1500;
    public static RectF GAME_BOUNDS;
    private int dp;

    private ArrayList<Player> mPlayers = new ArrayList<>();
    private ConversionUtils conversionUtils;

    public PlayerManager(Context pContext){
        CodeIntegrityUtils.checkNotNull(pContext, "Context must not be null");
        conversionUtils = new ConversionUtils(pContext);
        dp = conversionUtils.dpToPx(1);
        GAME_BOUNDS = new RectF(0, 0, RIGHT_GAME_BOUND*dp, BOTTOM_GAME_BOUND*dp);
        addPlayer(new Player(pContext,0, true));
        //TODO: How to add bluetooth players
    }

    @Override
    public void init() {
        for(Player player : mPlayers) {
            player.setPlayerSpeed(BASE_PLAYER_SPEED*dp);
            player.setPlayerTurnSpeed(BASE_PLAYER_TURN_SPEED*dp);
            player.getPlane().getTail().setTailWidth(BASE_TAIL_WIDTH*dp);
            player.init();
        }
    }

    @Override
    public void update() {
        for(Player player : mPlayers)
            player.update();
    }

    @Override
    public void stop() {
        for(Player player : mPlayers)
            player.stop();
    }

    @Override
    public String getName() {
        return GameComponents.PLAYER_MANAGER.getName();
    }

    public void addPlayer(Player pPlayer){
        CodeIntegrityUtils.checkNotNull(pPlayer, "Player must not be null");
        mPlayers.add(pPlayer);
    }

    public ArrayList<Player> getPlayers() {
        return mPlayers;
    }

    public Player getLocalPlayer(){
        for(Player player : mPlayers){
            if(player.isPlayerLocal())
                return player;
        }

        throw new NullPointerException("There is no local player");
    }
}

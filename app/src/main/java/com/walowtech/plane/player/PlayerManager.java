package com.walowtech.plane.player;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;

import com.walowtech.plane.game.GameComponent;
import com.walowtech.plane.data.GameComponents;
import com.walowtech.plane.game.GameLoop;
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

    public static final int BASE_PLAYER_SPEED = 400;
    public static final int BASE_PLAYER_TURN_SPEED = 190;
    public static final int BASE_TAIL_WIDTH = 4;
    public static final int RIGHT_GAME_BOUND = 2000;
    public static final int BOTTOM_GAME_BOUND = 3000;
    private static final int MAX_DISPLAY_PLANES = 10;
    public static RectF GAME_BOUNDS;
    private boolean mDisplayMode;
    private int dp;
    private Context mContext;

    private ArrayList<Player> mPlayers = new ArrayList<>();
    private ConversionUtils conversionUtils;

    public PlayerManager(Context pContext, boolean pDisplayMode){
        CodeIntegrityUtils.checkNotNull(pContext, "Context must not be null");
        mContext = pContext;
        mDisplayMode = pDisplayMode;
        conversionUtils = new ConversionUtils(mContext);
        dp = conversionUtils.dpToPx(1);
        GAME_BOUNDS = new RectF(0, 0, RIGHT_GAME_BOUND*dp, BOTTOM_GAME_BOUND*dp);
        Player p = new Player(mContext,0, true, pDisplayMode);
        p.getPlane().getTail().setTailColor(Color.RED);
        addPlayer(p);
    }

    @Override
    public void init() {
        // If this is multiplayer and a plane is not already added
        if(GameLoop.getCore().getMultiplayerAccess() != null && mPlayers.size() < 2){
            Player p = new Player(mContext, 1, false, false);
            p.getPlane().getTail().setTailColor(Color.MAGENTA);
            addPlayer(p);
        }

        // If in display mode, add a bunch of planes to screen
        if(mDisplayMode)
        {
            for(int i = 0; i < MAX_DISPLAY_PLANES; i++)
            {
                if(getPlayers().size() < MAX_DISPLAY_PLANES)
                {
                    Player p = new Player(mContext, i + 1, true, true);
                    p.getPlane().getTail().setTailColor(Color.rgb((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
                    addPlayer(p);
                }
            }
        }

        for(Player player : new ArrayList<>(mPlayers)) {
            if(mDisplayMode)
                player.setPlayerSpeed(BASE_PLAYER_SPEED * 3);
            else
                player.setPlayerSpeed(BASE_PLAYER_SPEED*dp);
            player.setPlayerTurnSpeed(BASE_PLAYER_TURN_SPEED*dp);
            player.getPlane().getTail().setTailWidth(BASE_TAIL_WIDTH*dp);
            player.init();
        }
    }

    @Override
    public void update() {
        for(Player player : new ArrayList<>(mPlayers))
            player.update();
    }

    @Override
    public void stop() {
        for(Player player : new ArrayList<>(mPlayers))
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
        return new ArrayList<>(mPlayers);
    }

    public Player getLocalPlayer(){
        for(Player player : mPlayers){
            if(player.isPlayerLocal())
                return player;
        }

        throw new NullPointerException("There is no local player");
    }
}

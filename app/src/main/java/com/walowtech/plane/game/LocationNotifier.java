package com.walowtech.plane.game;

import android.util.Log;

/**
 * This class is responsible for periodically notifying the
 * opponent of the clients location.
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2019-2-24
 */
public class LocationNotifier implements GameComponent {

    private final int CYCLES_BETWEEN_UPDATE = 10;

    private GameLoop mGameLoop;
    private int mCyclesSinceUpdate;

    /**
     * Default contructor for LocationNotifier
     * @param pGameLoop The game loop that owns the notifier
     */
    public LocationNotifier(GameLoop pGameLoop)
    {
        mGameLoop = pGameLoop;
    }

    @Override
    public void init() {
        mCyclesSinceUpdate = 0;
    }

    @Override
    public void update() {
        mCyclesSinceUpdate++;
        if(mCyclesSinceUpdate >= CYCLES_BETWEEN_UPDATE)
        {
            if(mGameLoop.getCore().getMultiplayerAccess() != null)
            {
                mGameLoop.getCore().getPlayerManager().getLocalPlayer().getPlane().notifyLocationToAll();
                mCyclesSinceUpdate = 0;
            }
            mCyclesSinceUpdate = 0;
        }
    }

    @Override
    public void stop() {
        mCyclesSinceUpdate = 0;
    }

    @Override
    public String getName() {
        return "Location Notifier";
    }
}

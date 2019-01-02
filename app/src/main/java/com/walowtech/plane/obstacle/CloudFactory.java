package com.walowtech.plane.obstacle;

import com.walowtech.plane.data.TailDataPoint;
import com.walowtech.plane.game.GameComponent;

import java.util.concurrent.CopyOnWriteArrayList;

public class CloudFactory implements GameComponent {

    CopyOnWriteArrayList<Cloud> mClouds = new CopyOnWriteArrayList<>();

    @Override
    public void init() {
        mClouds.clear();
    }

    @Override
    public void update() {

    }

    @Override
    public void stop() {

    }

    @Override
    public String getName() {
        return "Cloud Factory";
    }
}

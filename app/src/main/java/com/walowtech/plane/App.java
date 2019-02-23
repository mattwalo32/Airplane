package com.walowtech.plane;

import android.app.Application;

import com.walowtech.plane.multiplayer.MultiplayerAccess;

public class App extends Application {

    private MultiplayerAccess sMultiplayerAccess;

    @Override
    public void onCreate() {
        super.onCreate();

    }
}

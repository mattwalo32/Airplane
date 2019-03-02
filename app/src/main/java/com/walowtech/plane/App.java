package com.walowtech.plane;

import android.app.Application;

import com.walowtech.plane.multiplayer.MultiplayerAccess;

public class App extends Application {

    private MultiplayerAccess sMultiplayerAccess;
    private static boolean signedIn;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static boolean isSignedIn() {
        return signedIn;
    }

    public static void setSignedIn(boolean pSignedIn) {
        signedIn = pSignedIn;
    }
}

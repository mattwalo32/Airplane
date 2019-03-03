package com.walowtech.plane;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;

import com.walowtech.plane.multiplayer.MultiplayerAccess;

/**
 * Base application that is used to store global variables and methods.
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2019-03-02
 */
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

    /**
     * Returns the font used throughout the app.
     * @param pContext Context from which typeface is retrieved
     * @return The typeface for the font
     */
    public static Typeface getFont(Context pContext)
    {
        return ResourcesCompat.getFont(pContext, R.font.airside_sans);
    }
}

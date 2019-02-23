package com.walowtech.plane.Input;

import android.content.Context;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.walowtech.plane.activity.GameActivity;
import com.walowtech.plane.game.GameLoop;
import com.walowtech.plane.multiplayer.EventType;
import com.walowtech.plane.multiplayer.MessageUtils;
import com.walowtech.plane.multiplayer.MultiplayerAccess;
import com.walowtech.plane.player.Plane;
import com.walowtech.plane.player.PlayerManager;
import com.walowtech.plane.util.CodeIntegrityUtils;
import com.walowtech.plane.util.ConversionUtils;

/**
 * Touch listener for the GameGraphics canvas.
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2018-08-09
 */
public class GameClickListener implements View.OnTouchListener {

    private DisplayMetrics mDisplayMetrics;
    private ConversionUtils conversion;
    private int mScreenWidth;
    private int dp;

    public GameClickListener(Context pContext){
        CodeIntegrityUtils.checkNotNull(pContext, "Context cannot be null");
        mDisplayMetrics = pContext.getResources().getDisplayMetrics();
        mScreenWidth = mDisplayMetrics.widthPixels;
        conversion = new ConversionUtils(pContext);
        dp = conversion.dpToPx(1);
    }


    @Override
    public boolean onTouch(View view, MotionEvent event){
        int pointerCount = event.getPointerCount();
        float x = event.getX(pointerCount - 1);
        int validEvent = pointerCount > 1 ? MotionEvent.ACTION_POINTER_DOWN : MotionEvent.ACTION_DOWN;

        PlayerManager manager =  GameLoop.getCore().getPlayerManager();
        Plane plane = manager.getLocalPlayer().getPlane();
        MultiplayerAccess access = GameLoop.getCore().getMultiplayerAccess();
        String message = "";

        // If the user is clicking to replay
        if(!GameLoop.mRunning && event.getAction() == MotionEvent.ACTION_DOWN)
        {
            if (GameLoop.getCore().getCallingActivity() instanceof GameActivity)
                ((GameActivity) GameLoop.getCore().getCallingActivity()).onReady(null);
            return true;
        }

        // If click is on right side
        if(x >= mScreenWidth / 2 && event.getActionMasked() == validEvent) {
            manager.getLocalPlayer().getPlane().setTurning(true, true);
            message = MessageUtils.composeMessage(EventType.START_TURN_RIGHT, (int)plane.getRealX(), (int)plane.getRealY(), (int)plane.getHeading());
        // If click is on left side
        } else if(event.getActionMasked() == validEvent) {
            manager.getLocalPlayer().getPlane().setTurning(true, false);
            message = MessageUtils.composeMessage(EventType.START_TURN_LEFT, (int)plane.getRealX(), (int)plane.getRealY(), (int)plane.getHeading());
        // If player has ended touch
        } else if(event.getActionMasked() == MotionEvent.ACTION_UP){
            manager.getLocalPlayer().getPlane().setTurning(false, false);
            message = MessageUtils.composeMessage(EventType.END_TURN, (int)plane.getRealX(), (int)plane.getRealY(), (int)plane.getHeading());
        }

        // Notify participants that click event has been performed
        if(!message.equals("") && access != null)
            access.sendToAll(message);

        view.performClick();
        return true;
    }
}

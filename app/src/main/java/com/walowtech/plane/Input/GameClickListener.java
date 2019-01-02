package com.walowtech.plane.Input;

import android.content.Context;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.walowtech.plane.game.GameLoop;
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

        if(x >= mScreenWidth / 2 && event.getActionMasked() == validEvent) {
           manager.getLocalPlayer().getPlane().setTurning(true, true);
        } else if(event.getActionMasked() == validEvent) {
            manager.getLocalPlayer().getPlane().setTurning(true, false);
        } else if(event.getActionMasked() == MotionEvent.ACTION_UP){
            manager.getLocalPlayer().getPlane().setTurning(false, false);
        }

        return true;
    }
}

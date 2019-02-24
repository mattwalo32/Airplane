package com.walowtech.plane.util;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.TextView;

import javax.annotation.Nullable;

public class GraphicUtils {

    public static Drawable rotateDrawable(Context pContext, final Bitmap pB, final float pAngle){
        final BitmapDrawable drawable = new BitmapDrawable(pContext.getResources(), pB) {
            @Override
            public void draw(Canvas canvas) {
                canvas.save();
                canvas.rotate(pAngle, pB.getWidth() / 2, pB.getHeight() / 2);
                super.draw(canvas);
                canvas.restore();
            }
        };
        return drawable;
    }

    /**
     * Animates the given view to move to the specified position in
     * AccelerateDecelerateInterpolator. This is the default method with fixed animation
     * timne of 2000 ms.
     * @param view The view to move
     * @param finalPos The final absolute position to move to
     */
    public static void animateView(View view, int finalPos)
    {
        animateView(view, finalPos, 1500);
    }

    /**
     * Animates the given view to move to the specified position in
     * AccelerateDecelerateInterpolator.
     * @param view The view to move
     * @param finalPos The final absolute position to move to
     * @param duration Duration of the animation
     */
    public static void animateView(View view, int finalPos, int duration)
    {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "x", finalPos);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(duration);
        anim.start();
    }

    /**
     * Animates the given view to a position and back to the original position
     * making a bouncing motion
     * @param activity Calling activity to update UI on
     * @param view The view to move
     * @param bouncePos The position to bounce to and back
     * @param finalPos The final position for the view to bounce to
     * @param resId The resource ID of the string to set to the view after bounce reaches peak. This is nullable, if left null, text is not changed.
     */
    public static void animateBounce(Activity activity, View view, int bouncePos, int finalPos, @Nullable Integer resId)
    {
        view.clearAnimation();
        animateView(view, bouncePos, 1000);

        new Thread(()->
        {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            activity.runOnUiThread(()->
            {
                if(resId != null && view instanceof TextView)
                {
                    ((TextView) view).setText(resId);
                }
                animateView(view, finalPos, 1200);
            });
        }).start();
    }
}

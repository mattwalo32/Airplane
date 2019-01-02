package com.walowtech.plane.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

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
}

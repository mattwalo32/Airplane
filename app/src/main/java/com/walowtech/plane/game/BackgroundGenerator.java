package com.walowtech.plane.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.graphics.ColorUtils;
import android.util.DisplayMetrics;

import java.util.Random;

/**
 * Generates the background of the game.
 *
 * The background is generated onto a canvas and the canvas is turned into a bitmap that is drawn in the game loop.
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2019-01-01
 */
public class BackgroundGenerator {

    private final static int[] SKY_COLOR = {84, 133, 255};

    private static Bitmap background;
    private static Canvas canvas;
    private static Paint paint;

    /**
     * Creates the background of the game. To be called before the game starts.
     *
     * @param pWidth Max width of the game.
     * @param pHeight Max height of the game.
     * @return the generated background in bitmap format.
     */
    public static Bitmap GenerateBackground(int pWidth, int pHeight){
        paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        background = Bitmap.createBitmap(pWidth, pHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(background);

        paint.setColor(Color.rgb(SKY_COLOR[0], SKY_COLOR[1], SKY_COLOR[2]));
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawRect(0, 0, pWidth, pHeight, paint);

        drawCloudStreaks(pWidth, pHeight);

        canvas.save();

        return background;
    }

    /**
     * Draws a series of random streaks over the background.
     * @param pWidth Max width of streak
     * @param pHeight max height of streak
     */
    private static void drawCloudStreaks(int pWidth, int pHeight) {
        Random r = new Random();

        // Draw streaks
        for(int i = 0; i < 400; i++){
            int streakColor = Color.rgb(255, 255, 255);

            int a = r.nextInt(255);
            int streakLength = r.nextInt(500) + 200;
            int x = r.nextInt(pWidth);
            int y = r.nextInt(pHeight);
            int width = r.nextInt(45) + 30;

            streakColor = ColorUtils.setAlphaComponent(streakColor, a);
            paint.setColor(streakColor);
            paint.setStrokeWidth(width);
            canvas.drawLine(x, y, x + streakLength, y, paint);
        }
    }

}

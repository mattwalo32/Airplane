package com.walowtech.plane.player;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;

import com.walowtech.plane.data.TailCurveType;
import com.walowtech.plane.data.TailDataPoint;
import com.walowtech.plane.game.GameLoop;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Tail {
    private int mTailColor = Color.BLACK;
    private float mTailWidth;
    private CopyOnWriteArrayList<TailDataPoint> mTailData = new CopyOnWriteArrayList<>();

    public void addDataPoint(TailDataPoint pData){
        mTailData.add(pData);
    }

    public void updateCurrentLine() {
        Plane plane = GameLoop.getCore().getPlayerManager().getLocalPlayer().getPlane();

        if(mTailData.size() <= 0)
            return;

        TailDataPoint currentPoint = mTailData.get(mTailData.size() - 1);

        if(currentPoint.getCurveType() == TailCurveType.STRAIGHT) {
            if (plane.isMovingX()) {
                currentPoint.setEndX((float) (currentPoint.getEndX() - plane.getDeltaX()));
            }else {
                currentPoint.setStartX((float) (currentPoint.getStartX() + plane.getDeltaX()));
            }

            if (plane.isMovingY()){
                    currentPoint.setEndY((float) (currentPoint.getEndY() - plane.getDeltaY()));
            }else {
                currentPoint.setStartY((float) (currentPoint.getStartY() + plane.getDeltaY()));
            }

            if(!plane.isMovingX() || !plane.isMovingY()) {
                shiftAllPoints(!plane.isMovingX() ? plane.getDeltaX() : 0, !plane.isMovingY() ? plane.getDeltaY() : 0);
            }

            currentPoint.setRealEndX((float) (currentPoint.getRealEndX() - plane.getDeltaX()));
            currentPoint.setRealEndY((float) (currentPoint.getRealEndY() - plane.getDeltaY()));
        }
    }

    public void updateCurrentCurve(float pX, float pY, float pAngle){
        TailDataPoint data = mTailData.get(mTailData.size() - 1);
        if(data.getCurveType() == TailCurveType.CURVED) {
            
            Plane plane = GameLoop.getCore().getPlayerManager().getLocalPlayer().getPlane();
            RectF bounds = data.getBounds();

            float initialHeading =  data.getStartHeading();


            initialHeading %= 360;
            initialHeading = initialHeading < 0 ? 360 + initialHeading : initialHeading;

            double r =200;
            double x1 = bounds.left; //TODO: assign vars in contructor
            double y1 = bounds.bottom;
            Log.i("TEST", "PX " + pX + " PY" + pY);
            double x2 = pX;
            double y2 = pY;
            double x3 = (x1 + x2) / 2;
            double y3 = (y1 + y2) / 2;
            double q = Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));

            Log.i("TEST", "Q" + q);

            double centerX = x3 - Math.sqrt(Math.pow(r, 2) - Math.pow((q/2), 2)) * (y1-y2)/q;
            double centerY = y3 - Math.sqrt(Math.pow(r, 2) - Math.pow((q/2), 2)) * (x2-x1)/q;

            Log.i("TEST", "CENTER X" +centerX);

            bounds.left = (float) (centerX - r);
            bounds.right = (float) (centerX + r);
            bounds.top = (float) (centerY - r);
            bounds.bottom = (float) (centerY + r);


            data.setSweepAngle(pAngle);

            Log.i("TEST", "BOUNDS: L: " + bounds.right + " R: "
                    + bounds.right + " T: " + bounds.top +
                    " B: " + bounds.bottom);
        }
    }

    private void shiftAllPoints(double dx, double dy){
        Plane plane = GameLoop.getCore().getPlayerManager().getLocalPlayer().getPlane();
        for(int i = 0; i < mTailData.size() - 1; i++) {
            TailDataPoint currentPoint = mTailData.get(i);
            currentPoint.setStartX((float)(currentPoint.getStartX() + dx));
            currentPoint.setEndX((float)(currentPoint.getEndX() + dx));
            currentPoint.setStartY((float)(currentPoint.getStartY() + dy));
            currentPoint.setEndY((float)(currentPoint.getEndY() + dy));
        }
    }

    public void setTailColor(int pColor){
        mTailColor = pColor;
    }

    public int getTailColor(){
        return mTailColor;
    }

    public TailDataPoint getCurrentDataPoint(){
        return mTailData.get(mTailData.size() - 1);
    }

    public CopyOnWriteArrayList<TailDataPoint> getTailData() {
        return mTailData;
    }

    public float getTailWidth() {
        return mTailWidth;
    }

    public void setTailWidth(float pWidth){
        mTailWidth = pWidth;
    }
}

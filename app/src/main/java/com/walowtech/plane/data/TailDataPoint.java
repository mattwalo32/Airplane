package com.walowtech.plane.data;

import android.graphics.RectF;

/**
 * This object contains the data for each point on the curve.
 */
public class TailDataPoint {

    private TailCurveType mCurveType;

    //Curve variables
    private RectF mBounds;
    private float mStartAngle;
    private float mStartHeading;
    private float mSweepAngle;

    //Line variables
    private float mStartX;
    private float mStartY;
    private float mEndX;
    private float mEndY;

    private float mRealStartX;
    private float mRealStartY;
    private float mRealEndX;
    private float mRealEndY;

    public TailDataPoint(RectF pBounds, float pStartHeading, float pStartAngle, float pSweepAngle){
        mBounds = pBounds;
        mStartHeading = pStartHeading;
        mStartAngle = pStartAngle;
        mSweepAngle = pSweepAngle;
        mCurveType = TailCurveType.CURVED;
    }

    public TailDataPoint(float pStartX, float pStartY, float pEndX, float pEndY){
        mStartX = mRealStartX = pStartX;
        mStartY = mRealStartY = pStartY;
        mEndX = mRealEndX = pEndX;
        mEndY = mRealEndY = pEndY;
        mCurveType = TailCurveType.STRAIGHT;
    }

    public TailCurveType getCurveType() {
        return mCurveType;
    }

    public RectF getBounds() {
        return mBounds;
    }

    public float getStartAngle() {
        return mStartAngle;
    }

    public float getStartHeading(){
        return mStartHeading;
    }

    public float getSweepAngle() {
        return mSweepAngle;
    }

    public float getStartX() {
        return mStartX;
    }

    public float getStartY() {
        return mStartY;
    }

    public void setStartX(float pX){
        mStartX = pX;
    }

    public void setStartY(float pY){
        mStartY = pY;
    }

    public float getEndX() {
        return mEndX;
    }

    public float getEndY() {
        return mEndY;
    }

    public float getRealStartX() {
        return mRealStartX;
    }

    public float getRealStartY() {
        return mRealStartY;
    }

    public float getRealEndX() {
        return mRealEndX;
    }

    public float getRealEndY() {
        return mRealEndY;
    }

    public void setEndX(float pX){
        mEndX = pX;
    }

    public void setEndY(float pY){
        mEndY = pY;
    }

    public void setRealEndX(float pX){
        mRealEndX = pX;
    }

    public void setRealEndY(float pY){
        mRealEndY = pY;
    }

    public void setRectX(RectF pRect){
        mBounds = pRect;
    }

    public void setSweepAngle(float pAngle){
        mSweepAngle = pAngle;
    }
}

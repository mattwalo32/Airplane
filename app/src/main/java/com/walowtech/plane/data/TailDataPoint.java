package com.walowtech.plane.data;

import android.graphics.RectF;


/**
 * This object contains the data for each point on a curve or line.
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2018-08-09
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

    /**
     * Constructor for curve
     *
     * @param pBounds Bounding rectangle of curve
     * @param pStartHeading Heading of plane when curve was started
     * @param pStartAngle Abs start angle of curve
     * @param pSweepAngle Abs end angle of curve
     */
    public TailDataPoint(RectF pBounds, float pStartHeading, float pStartAngle, float pSweepAngle){
        mBounds = pBounds;
        mStartHeading = pStartHeading;
        mStartAngle = pStartAngle;
        mSweepAngle = pSweepAngle;
        mCurveType = TailCurveType.CURVED;
    }

    /**
     * Constructor for straight line
     *
     * @param pStartX Starting X coordinate of line
     * @param pStartY Starting Y coordinate of line
     * @param pEndX Ending X coordinate of line
     * @param pEndY Ending Y coordinate of line
     */
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

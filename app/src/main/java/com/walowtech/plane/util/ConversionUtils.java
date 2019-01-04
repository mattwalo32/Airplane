package com.walowtech.plane.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Util class for conversions.
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2018-08-09
 */
public class ConversionUtils {

    private DisplayMetrics mDisplayMetrics;

    public ConversionUtils(Context pContext){
        CodeIntegrityUtils.checkNotNull(pContext, "Context must not be null");
        mDisplayMetrics = pContext.getResources().getDisplayMetrics();
    }

    /**
     * Converts from density independent pixels to pixels.
     *
     * @param pDp The amount of density independent pixels.
     * @return The converted amount of dp.
     */
    public int dpToPx(int pDp){
        CodeIntegrityUtils.checkNotNull(pDp, "Cannot convert NULL dp");
        return 1;
        //return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pDp, mDisplayMetrics);
    }

    /**
     * Converts the degrees of the header, which is standard unit circle orientation, to
     * the degrees used by android for arcs, which is an upside down unit circle. Then subtracts
     * 90 because the tangent of the arc's start should be perpendicular to the tail, not parallel.
     *
     * @param pHeading The heading to convert.
     * @param pTurnRight True if the plane is turning right.
     * @return The converted degrees.
     */
    public static float headingToArc(float pHeading, boolean pTurnRight){
        pHeading %= 360;
        pHeading = pHeading < 0 ? 360 + pHeading : pHeading;
        pHeading = 360 - pHeading;
        pHeading = pTurnRight ? pHeading - 90 : pHeading + 90;
        return pHeading;
    }
}

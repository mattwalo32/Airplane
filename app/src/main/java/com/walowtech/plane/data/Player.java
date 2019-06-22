package com.walowtech.plane.data;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Object to hold information about players in waiting room.
 *
 * @author Matthew Walowski
 * @version 1.0.1
 * @since 2019-06-22
 */
public class Player {

    private Bitmap mIcon;
    private Uri mIconURL;
    private String mName;
    private String mStatus;

    public Player(Uri iconURL, String name, String status) {
        mIconURL = iconURL;
        mName = name;
        mStatus = status;
    }

    public void setStatus(String mStatus) {
        this.mStatus = mStatus;
    }

    public Uri getIconURL() {
        return mIconURL;
    }

    public void setIcon(Bitmap icon) {
        mIcon = icon;
    }

    public Bitmap getIcon() {
        return mIcon;
    }

    public String getName() {
        return mName;
    }

    public String getStatus() {
        return mStatus;
    }
}

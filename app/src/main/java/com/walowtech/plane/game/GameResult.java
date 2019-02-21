package com.walowtech.plane.game;

public enum GameResult {

    WON(0),
    LOST(1),
    TIE(2);

    private int mResultCode;

    GameResult(int resultCode)
    {
        mResultCode = resultCode;
    }

    public int getCode()
    {
        return mResultCode;
    }
}

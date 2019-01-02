package com.walowtech.plane.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.walowtech.plane.Input.GameClickListener;
import com.walowtech.plane.game.GameLoop;

/**
 * Activity that contains the game screen view
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2018-08-09
 */
public class GameActivity extends Activity {

    GameLoop gameLoop;
    GameClickListener clickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gameLoop = new GameLoop(this);
        setContentView(GameLoop.getCore().getGraphics());

        clickListener = new GameClickListener(this);
        GameLoop.getCore().getGraphics().setOnTouchListener(clickListener);

        gameLoop.startGame();
    }
}

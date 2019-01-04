package com.walowtech.plane.multiplayer;

import android.util.Log;

import com.walowtech.plane.player.Plane;

public class MessageUtils {


    public static String composeMessage(EventType pEvent, int pX, int pY, int pHeading){
        String event = String.valueOf(pEvent.getCode());
        String x = String.valueOf(pX);
        String y = String.valueOf(pY);
        String heading = String.valueOf(pHeading);

        String message = String.format("%s,%s,%s,%s", event, x, y, heading);
        Log.i("MULTIPLAYER", "Message: " + message);


        return message;
    }

    public static void parseMessage(String pMessage, Plane pPlane){
        String[] values = pMessage.split(",");
        int event = Integer.valueOf(values[0]);
        int x = Integer.valueOf(values[1]);
        int y = Integer.valueOf(values[2]);
        int heading = Integer.valueOf(values[3]);

        pPlane.setRealX(x);
        pPlane.setRealY(y);
        pPlane.setHeading(heading);

        if(event == EventType.MOVE_STRAIGHT.getCode() || event == EventType.END_TURN.getCode()){
            pPlane.setTurning(false, false);
        }else if(event == EventType.START_TURN_LEFT.getCode()){
            pPlane.setTurning(true, false);
        }else if(event == EventType.START_TURN_RIGHT.getCode()){
            pPlane.setTurning(true, true);
        }else if(event == EventType.UPDATE_NOW.getCode()){

        }
    }

}

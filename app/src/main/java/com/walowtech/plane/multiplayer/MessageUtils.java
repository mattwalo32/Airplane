package com.walowtech.plane.multiplayer;

import android.util.Log;

import com.walowtech.plane.player.Plane;

/** Utility class for componsing and parsing outgoing and incoming multiplayer messages
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2019-01-07
 */
public class MessageUtils {

    /**
     * Creates a message containing information about an event that has occurred, the coordinates
     * at which the event occured, and the heading when the event occurred.
     * @param pEvent The event that occurred
     * @param pX The X coordinate of the event location
     * @param pY The Y coordinate of the event location
     * @param pHeading The heading when the event occurred
     * @return
     */
    public static String composeMessage(EventType pEvent, int pX, int pY, int pHeading){
        String event = String.valueOf(pEvent.getCode());
        String x = String.valueOf(pX);
        String y = String.valueOf(pY);
        String heading = String.valueOf(pHeading);

        String message = String.format("%s,%s,%s,%s", event, x, y, heading);

        return message;
    }

    /**
     * Parses the incoming message and updates the opponent plane as necessary.
     * @param pMessage The incoming message
     * @param pPlane The plane that has sent the message
     */
    public static void parseMessage(String pMessage, Plane pPlane){
        // Parse message
        String[] values = pMessage.split(",");
        int event = Integer.valueOf(values[0]);
        int x = Integer.valueOf(values[1]);
        int y = Integer.valueOf(values[2]);
        int heading = Integer.valueOf(values[3]);

        // Update the sender's location
        pPlane.setRealX(x);
        pPlane.setRealY(y);
        pPlane.setHeading(heading);

        // Update the sender's action
        if(event == EventType.MOVE_STRAIGHT.getCode() || event == EventType.END_TURN.getCode()){
            pPlane.setTurning(false, false);
        }else if(event == EventType.START_TURN_LEFT.getCode()){
            pPlane.setTurning(true, false);
        }else if(event == EventType.START_TURN_RIGHT.getCode()){
            pPlane.setTurning(true, true);
        }
    }
}

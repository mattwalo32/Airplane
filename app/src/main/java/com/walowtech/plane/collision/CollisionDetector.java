package com.walowtech.plane.collision;

import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;

import com.walowtech.plane.data.TailDataPoint;
import com.walowtech.plane.game.GameComponent;
import com.walowtech.plane.game.GameCore;
import com.walowtech.plane.game.GameLoop;
import com.walowtech.plane.player.Plane;
import com.walowtech.plane.player.Player;
import com.walowtech.plane.player.PlayerManager;

import java.util.ArrayList;

/** This class is responsible for all collision detection within the game.
 *  Collisions with player lines, opponent lines, opponents, and bounds are calculated
 *  every cycle.
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2019-01-07
 */
public class CollisionDetector implements GameComponent {

    private Collision mCollision;
    private Plane mPlane;
    private RectF mPlaneHitbox;
    private int hitboxShrink = 15;

    public CollisionDetector(Plane p){
        mPlane = p;
    }

    @Override
    public void init() {
        mCollision = new Collision();
        mCollision.setCollision(CollisionType.NONE);
    }

    @Override
    public void update() {
        checkForCollision();

        if(mCollision.isCollision()) {
            GameLoop.getCore().stop();
        }
    }

    /**
     * Calculates player hitbox, then checks for collisions
     * with bounds, lines, and planes
     */
    public void checkForCollision(){
        initHitbox();
        checkBounds();
        checkLines();
        checkPlanes();
    }

    /**
     * Calculates a rough hitbox by drawing a polygon between the player's wing tips, front, and tail
     */
    private void initHitbox(){
        mPlane.getHitboxPoints().clear();
        mPlane.getHitboxPoints().add(new Point((int)mPlane.getTailX(hitboxShrink), (int)mPlane.getTailY(hitboxShrink)));
        mPlane.getHitboxPoints().add(new Point((int)mPlane.getLeftWingX(hitboxShrink), (int)mPlane.getLeftWingY(hitboxShrink)));
        mPlane.getHitboxPoints().add(new Point((int)mPlane.getHeadX(hitboxShrink), (int)mPlane.getHeadY(hitboxShrink)));
        mPlane.getHitboxPoints().add(new Point((int)mPlane.getRightWingX(hitboxShrink), (int)mPlane.getRightWingY(hitboxShrink)));
        mPlaneHitbox = new RectF(mPlane.getRealX(), mPlane.getRealY(), mPlane.getRealX() + mPlane.getPlaneSprite().getWidth(), mPlane.getRealY() + mPlane.getPlaneSprite().getHeight());
    }

    /**
     * Checks if player is out of bounds
     */
    private void checkBounds(){
        if(!RectF.intersects(PlayerManager.GAME_BOUNDS, mPlaneHitbox)){
            mCollision.setCollision(CollisionType.BOUNDS);
        }
    }

    /**
     * Checks if player has hit any lines
     */
    private void checkLines(){
        for(Player p : GameLoop.getCore().getPlayerManager().getPlayers()) {
            for (TailDataPoint dataPoint : p.getPlane().getTail().getTailData()){
                for(int i = 0; i < mPlane.getHitboxPoints().size(); i++){
                    int sX = p.getPlane().isLocal() ? (int)dataPoint.getStartX() : (int)(dataPoint.getRealStartX() - GameLoop.getCore().getPlayerManager().getLocalPlayer().getPlane().getScreenX());
                    int sY = p.getPlane().isLocal() ? (int)dataPoint.getStartY() : (int)(dataPoint.getRealStartY() - GameLoop.getCore().getPlayerManager().getLocalPlayer().getPlane().getScreenY());
                    int eX = p.getPlane().isLocal() ? (int)dataPoint.getEndX() : (int)(dataPoint.getRealEndX() - GameLoop.getCore().getPlayerManager().getLocalPlayer().getPlane().getScreenX());
                    int eY = p.getPlane().isLocal() ? (int)dataPoint.getEndY() : (int)(dataPoint.getRealEndY() - GameLoop.getCore().getPlayerManager().getLocalPlayer().getPlane().getScreenY());
                    if(isIntersecting(mPlane.getHitboxPoints().get(i), mPlane.getHitboxPoints().get(i + 1 >= mPlane.getHitboxPoints().size() ? 0 : i + 1),
                            new Point(sX, sY), new Point(eX, eY))){
                        mCollision.setCollision(CollisionType.LINE);
                    }
                }
            }
        }
    }

    private void checkPlanes(){
        //TODO: Check plane collision
        for(Player player : GameLoop.getCore().getPlayerManager().getPlayers())
        {
            if(!player.isPlayerLocal())
            {
                for(int i = 0; i < mPlane.getHitboxPoints().size(); i++)
                {
                        if(isIntersecting(mPlane.getHitboxPoints().get(i), mPlane.getHitboxPoints().get(i + 1 >= mPlane.getHitboxPoints().size() ? 0 : i + 1),
                                player.getPlane().getHitboxPoints().get(i), player.getPlane().getHitboxPoints().get(i + 1 >= player.getPlane().getHitboxPoints().size() ? 0 : i + 1)))
                    {
                        mCollision.setCollision(CollisionType.PLANE);
                    }
                }
            }
        }
    }

    /**
     * Checks if two lines drawn between four respective points intersect.
     *
     * @param a Start point in line one
     * @param b End point in line one
     * @param c Start point in line two
     * @param d End point in line two
     * @return
     */
    private boolean isIntersecting(Point a, Point b, Point c, Point d){
        float denominator = ((b.x - a.x) * (d.y - c.y)) - ((b.y - a.y) * (d.x - c.x));
        float numerator1 = ((a.y - c.y) * (d.x - c.x)) - ((a.x - c.x) * (d.y - c.y));
        float numerator2 = ((a.y - c.y) * (b.x - a.x)) - ((a.x - c.x) * (b.y - a.y));

        // Detect coincident lines (has a problem, read below)
        //if (denominator == 0) return numerator1 == 0 && numerator2 == 0;

        float r = numerator1 / denominator;
        float s = numerator2 / denominator;

        return (r >= 0 && r <= 1) && (s >= 0 && s <= 1);
    }

    public Collision getCollisionData(){
        return mCollision;
    }

    @Override
    public void stop() {

    }

    @Override
    public String getName() {
        return "Collision Detector";
    }
}

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

public class CollisionDetector implements GameComponent {

    private Collision mCollision;
    private Plane mPlane;
    private RectF mPlaneHitbox;
    //TODO: Make private again
    public static ArrayList<Point> hitboxPoints = new ArrayList<>();

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

    public void checkForCollision(){
        initHitbox();
        checkBounds();
        checkLines();
        checkPlanes();
    }

    private void initHitbox(){
        hitboxPoints.clear();
        hitboxPoints.add(new Point((int)mPlane.getTailX(hitboxShrink), (int)mPlane.getTailY(hitboxShrink)));
        hitboxPoints.add(new Point((int)mPlane.getLeftWingX(hitboxShrink), (int)mPlane.getLeftWingY(hitboxShrink)));
        hitboxPoints.add(new Point((int)mPlane.getHeadX(hitboxShrink), (int)mPlane.getHeadY(hitboxShrink)));
        hitboxPoints.add(new Point((int)mPlane.getRightWingX(hitboxShrink), (int)mPlane.getRightWingY(hitboxShrink)));
        mPlaneHitbox = new RectF(mPlane.getRealX(), mPlane.getRealY(), mPlane.getRealX() + mPlane.getPlaneSprite().getWidth(), mPlane.getRealY() + mPlane.getPlaneSprite().getHeight());
    }

    private void checkBounds(){
        if(!RectF.intersects(PlayerManager.GAME_BOUNDS, mPlaneHitbox)){
            mCollision.setCollision(CollisionType.BOUNDS);
        }
    }

    private void checkLines(){
        for(Player p : GameLoop.getCore().getPlayerManager().getPlayers()) {
            for (TailDataPoint dataPoint : p.getPlane().getTail().getTailData()){
                for(int i = 0; i < hitboxPoints.size(); i++){
                    if(isIntersecting(hitboxPoints.get(i), hitboxPoints.get(i + 1 >= hitboxPoints.size() ? 0 : i + 1),
                            new Point((int)dataPoint.getStartX(), (int)dataPoint.getStartY()), new Point((int)dataPoint.getEndX(), (int)dataPoint.getEndY()))){
                        mCollision.setCollision(CollisionType.LINE);
                        Log.i("COLLISION", "Collision");
                    }
                }
            }
        }
    }

    private void checkPlanes(){

    }

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

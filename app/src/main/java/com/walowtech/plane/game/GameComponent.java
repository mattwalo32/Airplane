package com.walowtech.plane.game;

/**
 * Interface defines the methods that each component of the game must implement.
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2018-08-09
 */
public interface GameComponent {

    void init(); // Called on startup
    void update(); // Called periodically
    void stop(); // Called when exiting
    String getName(); // Used for retrieving component
}

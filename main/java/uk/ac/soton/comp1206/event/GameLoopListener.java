package uk.ac.soton.comp1206.event;

/**
 * listener for game loop
 */
public interface GameLoopListener {
    /**
     * loop for game
     * @param delay time
     */
    void gameLoop(int delay);
}

package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.Game;

/**
 * listener for game over
 */
public interface GameOverListener {
    /**
     * game over
     * @param game game
     */
    void gameOver(Game game);
}

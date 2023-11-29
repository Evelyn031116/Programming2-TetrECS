package uk.ac.soton.comp1206.event;

import javafx.beans.property.IntegerProperty;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.HashSet;
import java.util.Set;

/**
 * listener for lines clearing
 */
public interface LineClearedListener {
    /**
     * lines been cleaned
     * @param gameBlockCoordinateSet coordinates of game blocks
     */
    void lineCleared(Set<GameBlockCoordinate> gameBlockCoordinateSet);
}

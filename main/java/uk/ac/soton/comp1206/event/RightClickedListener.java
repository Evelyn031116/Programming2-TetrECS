package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlock;

/**
 * listener for right clicked
 */
public interface RightClickedListener {
    /**
     * right clicked void
     * @param gameBlock game block
     */
    void rightClick(GameBlock gameBlock);
}

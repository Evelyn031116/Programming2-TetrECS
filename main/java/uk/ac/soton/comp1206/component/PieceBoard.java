package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

/**
 * PieceBoard represents a piece board in the game
 */
public class PieceBoard extends GameBoard{
    /**
     *
     * @param grid grid
     * @param width the width of the graphic context
     * @param height the height of the graphic context
     */
    public PieceBoard(Grid grid, double width, double height) {
        super(grid, width, height);
    }

    /**
     *
     * @param cols the number of columns
     * @param rows the number of rows
     * @param width the width of the graphic context
     * @param height the height of the graphic context
     */
    public PieceBoard(int cols, int rows, double width, double height) {
        super(cols, rows, width, height);
    }

    /**
     * show the piece
     * @param gamePiece game piece
     */
    public void displayPiece(GamePiece gamePiece){
        this.grid.cleanPiece();
        this.grid.playPiece(gamePiece, 1, 1);

    }
}

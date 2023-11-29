package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     *Judge if the piece can be played
     * @param gamePiece the piece
     * @param x column
     * @param y row
     * @return whether you can put the piece in the given position
     */
    public boolean canPlayPiece(GamePiece gamePiece, int x, int y){
        if(gamePiece == null) {
            return false;
        }
        int[][] blocks = gamePiece.getBlocks();
        for (int i = 0; i < blocks.length; i++){
            for (int j = 0; j < blocks[i].length; j++){
                // If the coordinate of the block is 0, it means it is empty, then continue iterating
                if (blocks[i][j] == 0) continue;
                // If the coordinate of the block is not zero, it means it is not empty, you can't put the piece here
                if (blocks[i][j] != 0){
                    if (get(i + x,j + y) != 0){
                        return false;
                    }
                }
            }
        }
        // If after finishing iterating, all the coordinates of the blocks are zero, then you can put the piece here
        return true;
    }

    /**
     * Judge whether the piece can be played and offset the x and y co-ordinates to ensure a piece is played by its centre
     * @param gamePiece the piece
     * @param x column
     * @param y row
     */
    public void playPiece(GamePiece gamePiece, int x, int y){
        x -= 1;
        y -= 1;
        if(canPlayPiece(gamePiece, x, y)){
            int[][] blocks = gamePiece.getBlocks();

            for(int i = 0; i < blocks.length; i++){
                for(int j = 0; j < blocks[i].length; j++){
                    if(blocks[i][j] == 0) continue;
                    if(blocks[i][j] != 0){
                        // offset the x and y co-ordinates to ensure a piece is played by its centre
                        set(x + i, y + j, blocks[i][j]);
                    }
                }
            }
        }
    }

    /**
     * clean pieces
     */
    public void cleanPiece(){
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++) {
                grid[i][j].set(0);
            }
        }
    }
}
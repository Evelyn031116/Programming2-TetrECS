package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.Multimedia;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.GameOverListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;

import java.util.HashSet;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;
    /**
     * current piece
     */
    protected GamePiece currentPiece;

    /**
     * following piece
     */
    protected GamePiece followingPiece;

    /**
     * current score
     */
    protected IntegerProperty score = new SimpleIntegerProperty(0);
    /**
     * current level
     */
    protected IntegerProperty level = new SimpleIntegerProperty(0);
    /**
     * current left lives
     */
    protected IntegerProperty lives = new SimpleIntegerProperty(3);
    /**
     * current game multiplier
     */
    protected IntegerProperty multiplier = new SimpleIntegerProperty(1);
    /**
     * multimedia
     */
    protected Multimedia multimedia = new Multimedia();
    /**
     * listener for line clearing
     */
    public LineClearedListener lineClearedListener;
    /**
     * listener for game loop
     */
    public GameLoopListener gameLoopListener;
    /**
     * listener for game over
     */
    protected GameOverListener gameOverListener;
    /**
     * game timer
     */
    protected Timer gameTimer = new Timer();
    /**
     * a scheduled executor service
     */
    protected ScheduledExecutorService scheduledExecutorService;


    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        this.scheduledExecutorService= Executors.newSingleThreadScheduledExecutor();

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols, rows);
    }

    /**
     * lives property
     * @return lives
     */
    public IntegerProperty livesProperty() {
        return lives;
    }

    /**
     * score property
     * @return scores
     */
    public IntegerProperty scoreProperty() {
        return score;
    }

    /**
     * level property
     * @return level
     */
    public IntegerProperty levelProperty() {
        return level;
    }

    /**
     * multiplier property
     * @return multiplier
     */
    public IntegerProperty multiplierProperty() {
        return multiplier;
    }

    /**
     * listener for next piece
     */
    protected NextPieceListener nextPieceListener;

    /**
     * Start the game
     */
    public void start() {
        logger.info("Start game");
        initialiseGame();
        if (this.gameLoopListener != null) {
            this.gameLoopListener.gameLoop(this.getTimerDelay());
        }
        TimerTask task =
                new TimerTask() {

                    public void run() {
                        if (Game.this.lives.get() < 0) {
                            Game.this.gameTimer.cancel();
                        }
                        gameLoop();
                    }
                };

        this.gameTimer.schedule(task, this.getTimerDelay(), this.getTimerDelay());
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        // When the game is initialised, spawn a new GamePiece and set it as the currentPiece
        followingPiece = spawnPiece();
        nextPiece();
    }

    /**
     * Handle what should happen when a particular block is clicked
     *
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        // I don't need to change the value of blocks so the following code is useless
/*
        //Get the new value for this block
        int previousValue = grid.get(x,y);
        int newValue = previousValue + 1;
        // Inspect if newValue exceeds the number of available blocks in the game, if yes, set the value to 0 to ensure the number of blocks stays within an effective range
        if (newValue  > GamePiece.PIECES) {
            newValue = 0;
        }

        //Update the grid with the new value
        grid.set(x,y,newValue);
*/
        // judge whether the piece can be played
        if (grid.canPlayPiece(currentPiece, x - 1, y - 1)) {
            Multimedia.playDocumentMusic("place.wav");
            grid.playPiece(currentPiece, x, y);
            if (gameTimer != null) {
                gameTimer.cancel();
            }
            gameTimer = new Timer("new timer");
            logger.info("Created a new timer");
            nextPiece();
            afterPiece();
            TimerTask task =
                    new TimerTask() {
                        public void run() {
                            gameLoop();
                        }
                    };
            if (gameLoopListener != null) {
                gameLoopListener.gameLoop(getTimerDelay());
            }
            gameTimer.schedule(task, getTimerDelay(), getTimerDelay());
        } else {
            Multimedia.playDocumentMusic("fail.wav");
            multiplier.set(1);
        }
    }

    /**
     * set on line cleared listener
     * @param lineClearedListener a listener for line cleared
     */
    public void setLineClearedListener(LineClearedListener lineClearedListener) {
        this.lineClearedListener = lineClearedListener;
    }

    /**
     * set on actions after a piece is placed
     */
    public void afterPiece() {
        int linesToBeCleaned = 0;
        // record the coordinates of blocks needed to be cleaned
        HashSet<GameBlockCoordinate> blocksToBeCleaned = new HashSet<>();

        for (int i = 0; i < cols; i++) {
            // record the number of blocks in the current column
            int countCols = 0;
            for (int j = 0; j < rows; j++) {
                // if the value of the current block is 0, it means the block is empty, finish iterating
                if (grid.get(i, j) == 0) break;
                countCols += 1;
            }
            // means the current column is full
            if (countCols == rows) {
                linesToBeCleaned += 1;
                for (int j = 0; j < rows; j++) {
                    GameBlockCoordinate currentCoor = new GameBlockCoordinate(i, j);
                    // add the coordinate pf the current block to blocksToBeCleaned
                    blocksToBeCleaned.add(currentCoor);
                }
            }
        }
        for (int j = 0; j < rows; j++) {
            // record the number of blocks in the current row
            int countRows = 0;
            for (int i = 0; i < cols; i++) {
                // if the value of the current block is 0, it means the block is empty, finish iterating
                if (grid.get(i, j) == 0) break;
                countRows += 1;
            }
            // means the current row is full
            if (countRows == cols) {
                linesToBeCleaned += 1;
                for (int i = 0; i < cols; i++) {
                    GameBlockCoordinate currentCoor = new GameBlockCoordinate(i, j);
                    // add the coordinate pf the current block to blocksToBeCleaned
                    blocksToBeCleaned.add(currentCoor);
                }
            }
        }
        // if there are lines to be cleared
        if (linesToBeCleaned != 0) {
            // clean the blocks
            for (GameBlockCoordinate block : blocksToBeCleaned) {
                grid.set(block.getX(), block.getY(), 0);
            }
            // add scores
            score(linesToBeCleaned, blocksToBeCleaned.size());
            // update the multiplier of the current block
            this.multiplier.set(this.multiplier.add(1).get());
            if (lineClearedListener != null) {
                lineClearedListener.lineCleared(blocksToBeCleaned);
                logger.info("Cleaned lines");
            }
        } else {
            // reset multiplier
            multiplier.set(1);
        }
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     *
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     *
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     *
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Create a new random GamePiece
     *
     * @return a GamePiece
     */
    public GamePiece spawnPiece() {
        Random random = new Random();
        // set the bound because the max value of the piece is 15
        GamePiece gamePiece = GamePiece.createPiece(random.nextInt(15));
        return gamePiece;
    }

    /**
     * Replace the current piece with a new piece
     */
    public void nextPiece() {
        currentPiece = followingPiece;
        followingPiece = spawnPiece();
        // move the following piece to the current piece, and then replace the following piece
        if (this.nextPieceListener != null) {
            this.nextPieceListener.nextPiece(this.currentPiece, this.followingPiece);
        }
    }

    /**
     * add score after cleaning pieces
     * @param lines lines need to be cleaned
     * @param blocks blocks need to be cleaned
     * @return score
     */
    public int score(int lines, int blocks) {
        int scoreGained = lines * blocks * 10 * this.multiplier.get();
        this.score.set(score.get() + scoreGained);
        int level = this.score.get() / 1000;
        if (this.level.get() != level) {
            this.level.set(level);
            multimedia.playDocumentMusic("level.wav");
        }
        return score.get();
    }

    /**
     * set listener for the next piece
     * @param nextPieceListener listener for next piece
     */
    public void setNextPieceListener(NextPieceListener nextPieceListener) {
        this.nextPieceListener = nextPieceListener;
    }

    /** get following piece
     *
     * @return following piece
     */
    public GamePiece getNewPiece() {
        return this.followingPiece;
    }

    /**
     * get current piece
     * @return current piece
     */
    public GamePiece getCurrentPiece() {
        return this.currentPiece;
    }

    /**
     * rotate current piece
     * @param rotations rotations
     */
    public void rotateCurrentPiece(int rotations) {
        currentPiece.rotate();
    }

    /**
     * swap the current and following pieces
     */
    public void swapCurrentPiece() {
        GamePiece gamePiece = followingPiece;
        followingPiece = currentPiece;
        currentPiece = gamePiece;
    }

    /**
     * set on line cleared listener
     * @param lineClearedListener listener for clearing lines
     */
    public void setOnLineCleared(LineClearedListener lineClearedListener) {
        this.lineClearedListener = lineClearedListener;
    }

    /**
     * make the timeline shorter when the level goes higher
     * @return delay
     */
    public int getTimerDelay() {
        int delay = 12000 - (500 * level.get());
        if (delay < 2500) {
            delay = 2500;
        }
        return delay;
    }

    /**
     * set the actions when losing lives
     */
    public void gameLoop() {
        if (lives.get() == 0) {
            gameOver();
        } else {
            lives.set(lives.get() - 1);
            multimedia.playDocumentMusic("lifelose.wav");
            multiplier.set(1);
            nextPiece();
            if (gameLoopListener != null) {
                gameLoopListener.gameLoop(getTimerDelay());
            }
        }
    }

    /**
     * game over
     */
    public void gameOver() {
        if (gameOverListener != null) {
            Platform.runLater(() -> gameOverListener.gameOver(this));
        }
        this.stop();
        logger.info("Game over");
    }

    /**
     * set on game over
     * @param gameOverListener detect whether the game is over
     */
    public void setOnGameOver(GameOverListener gameOverListener) {
        this.gameOverListener = gameOverListener;
    }

    /**
     * stop game timer
     */
    public void stop() {
        gameTimer.cancel();
        scheduledExecutorService.shutdownNow();
    }

    /**
     * set on game loop
     * @param gameLoopListener listener for game loop
     */
    public void setOnGameLoop(GameLoopListener gameLoopListener) {
        this.gameLoopListener = gameLoopListener;

    }
}

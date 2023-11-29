package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * multiplayer
 */
public class MultiplayerGame extends Game{

    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

    /**
     * Communicator used to receive and send messages to the server
     */
    protected Communicator communicator;

    /**
     * GameWindow of the current game
     */
    protected GameWindow gameWindow;

    /**
     * Queue of pieces to be played
     */
    protected LinkedList<GamePiece> gamePieceLinkedList = new LinkedList<>();

    /**
     * Create a new game with given rows and columns
     *
     * @param cols number of columns
     * @param rows number of rows
     * @param gameWindow the current GameWindow
     */
    public MultiplayerGame(int cols, int rows, GameWindow gameWindow) {
        super(cols, rows);
        this.gameWindow = gameWindow;
    }

    /**
     * set on actions when a new piece is received from the server
     * @param gamePiece game piece
     */
    public void newPiece(GamePiece gamePiece) {
        if(currentPiece == null) {
            currentPiece = gamePiece;
        } else if(followingPiece == null){
            followingPiece = gamePiece;
            nextPieceListener.nextPiece(currentPiece, followingPiece);
        } else {
            gamePieceLinkedList.add(gamePiece);
        }
    }

    /**
     * update current piece and following piece
     */
    @Override
    public void nextPiece() {
        currentPiece = followingPiece;
        followingPiece = gamePieceLinkedList.remove();
        nextPieceListener.nextPiece(currentPiece, followingPiece);
        communicator.send("PIECE");
    }

    /**
     * set on action when a piece has been played
     */
    @Override
    public void afterPiece() {
        super.afterPiece();
        StringBuilder board = new StringBuilder("BOARD ");
        for (int x = 0; x < this.getCols(); x++) {
            for(int y = 0; y < this.getRows(); y++) {
                board.append(grid.get(x,y)).append(" ");
            }
        }
        communicator.send(board.toString());
    }

    /**
     * Initialise a new game
     */
    @Override
    public void initialiseGame() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        communicator = gameWindow.getCommunicator();
//Listens for messages from communicator and handles the command
        communicator.addListener(message -> Platform.runLater(() -> listen(message.trim())));
        Runnable sendPiece = new Runnable() {
            @Override
            public void run() {
                communicator.send("PIECE");
            }
        };
        for(int x = 0; x < 5; x++) {
            scheduledExecutorService.schedule(sendPiece, 1000, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * send messages after adding scores and return lines
     *
     * @param lines  number of lines cleared
     * @param blocks number of blocks cleared
     * @return lines
     */
    @Override
    public int score(int lines, int blocks) {
        super.score(lines, blocks);
        communicator.send("SCORE " + this.scoreProperty().get());
        return lines;
    }

    /**
     * Handles messages from communicator
     * @param message message received from communicator
     */
    protected void listen(String message) {
        if(message.contains("PIECE")) {
            logger.info("Added piece");
            message = message.replace("PIECE ", "");
            GamePiece gamePiece = GamePiece.createPiece(Integer.parseInt(message));
            newPiece(gamePiece);
        }
    }
}

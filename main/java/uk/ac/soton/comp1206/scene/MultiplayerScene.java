package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.Multimedia;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * multiplayer scene
 */

public class MultiplayerScene extends ChallengeScene{
    /**
     * logger
     */
    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);

    /**
     * message vbox
     */
    protected VBox messagesBox;

    /**
     * Communicator
     */
    protected Communicator communicator;

    /**
     * TextField
     */
    protected TextField textField = new TextField();


    /**
     * The scores of all the players
     */
    protected SimpleListProperty<Pair<String, Integer>> multiplayerScores = new SimpleListProperty<>();

    /**
     * show the scores of all the players
     */
    protected ScoresList leaderboard;

    /**
     * Extension - show other players' game boards in multiplayer mode
     */
    protected VBox otherPlayers = new VBox();

    /**
     * A Set of all players in the game
     */
    protected Set<String> players;

    /**
     * Players' own game boards
     */
    protected HashMap<String, GameBoard> playersGameboard;
    /**
     * Create a new challenge scene
     *
     * @param gameWindow the Game Window
     */
    public MultiplayerScene(GameWindow gameWindow, Set<String> playerSet) {
        super(gameWindow);
        this.multiplayerScores.set(FXCollections.observableArrayList(new ArrayList<Pair<String, Integer>>()));
        this.players = playerSet;
    }

    /**
     * Initialise the scene and starts the multiplayer game
     */
    @Override
    public void initialise() {
        super.initialise();
        communicator = gameWindow.getCommunicator();
        this.game.setOnGameLoop(this::gameLoop);
        //Listens for messages from communicator and handles the command
        communicator.addListener(message -> Platform.runLater(() -> listen(message.trim())));
        communicator.send("SCORES");
        initialisePlayerBoards();
    }

    /**
     * Handles Keyboard input
     * @param keyEvent keyboard input
     */
    @Override
    protected void keyboardSupport(KeyEvent keyEvent) {
        super.keyboardSupport(keyEvent);

        KeyCode keyCode = keyEvent.getCode();
        // open the chat window
        if (keyCode == KeyCode.T) {
            if (textField.isVisible()) {
                textField.setVisible(false);
                textField.clear();
            } else {
                textField.setVisible(true);
                textField.requestFocus();
            }
            // escape
        } else if (keyCode == KeyCode.ESCAPE) {
            if (textField.isVisible()) {
                textField.setVisible(false);
                textField.clear();
            } else {
                Multimedia.stopBgmMusic();
                game.gameOver();
                Multimedia.playDocumentMusic("transition.wav");
                endGame();
                gameWindow.startMenu();
                communicator.send("DIE");
                logger.info("Escape Pressed");
            }
            // send messages
        } else if (keyCode == KeyCode.ENTER && textField.isVisible()) {
            String message = textField.getText();
            if (message != null && !message.isBlank()) {
                communicator.send("MSG " + message);
                textField.clear();
            }
            textField.setVisible(false);
        }
    }


    /**
     * Set up the game
     */
    @Override
    public void setupGame() {
        logger.info("Starting a new multiplayer game");

        game = new MultiplayerGame(5,  5, this.gameWindow);
    }

    /**
     * Build the Multiplayer window
     */
    @Override
    public void build() {
        super.build();
        logger.info("Building " + this.getClass().getName());

        // Chat Window
        messagesBox = new VBox();
        messagesBox.getStyleClass().add("messages");

        ScrollPane messagesScrollPane = new ScrollPane(messagesBox);
        messagesScrollPane.setBackground(null);
        messagesScrollPane.setPrefSize(gameWindow.getWidth() / 8, gameWindow.getHeight() / 8);
        messagesScrollPane.needsLayoutProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                messagesScrollPane.setVvalue(1.0);
            }
        });

        // Scores
        leaderboard = new ScoresList();
        leaderboard.setAlignment(Pos.CENTER);
        leaderboard.setTranslateY(-50);
        leaderboard.setTranslateX(25);
        this.multiplayerScores.bind(leaderboard.getScores());

        // Game Boards
        HBox boardsBox = new HBox(pieceBoard1, pieceBoard2);
        boardsBox.setAlignment(Pos.CENTER_RIGHT);
        boardsBox.setSpacing(10);

        VBox mainBox = new VBox(messagesScrollPane, leaderboard, boardsBox);
        mainBox.setAlignment(Pos.CENTER_RIGHT);
        mainBox.setSpacing(20);
        mainBox.setPadding(new Insets(20));

        mainPane.setRight(mainBox);

        // Game Over
        game.setOnGameOver(game -> {
            endGame();
            gameWindow.loadScene(new ScoresScene(gameWindow, game, this.multiplayerScores));
        });
    }



    /**
     * Remove a user's name when a game over or the user leaves
     * @param userName usernames
     */
    protected void removeUser(String userName) {
        leaderboard.removeUselessElements(userName);
    }

    /**
     * Handles messages from communicator
     * @param message message received from communicator
     */
    protected void listen(String message) {
        if (message.startsWith("MSG ")) {
            String[] parts = message.substring(4).split(":");
            if (parts.length > 1) {
                String sender = parts[0];
                String text = parts[1];
                Text messageText = new Text(sender + " : " + text);
                messageText.getStyleClass().add("messages Text");
                messagesBox.getChildren().add(messageText);
            }
        } else if (message.startsWith("SCORES ")) {
            String[] scores = message.substring(7).split("\n");
            multiplayerScores.clear();
            for (String score : scores) {
                String[] parts = score.split(":");
                if (parts.length > 1) {
                    String name = parts[0];
                    int value = Integer.parseInt(parts[1]);
                    multiplayerScores.add(new Pair<>(name, value));
                }
            }
        } else if (message.startsWith("DIE ")) {
            String name = message.substring(4);
            removeUser(name);
        } else if (message.startsWith("BOARD ")) {
            String boardStr = message.substring(6);
            updatePlayerBoard(boardStr);
        }
    }


    /**
     * Initialises other players' game boards
     */
    public void initialisePlayerBoards() {
        playersGameboard = new HashMap<>();
        for (String player: players) {
            GameBoard gameBoard = new GameBoard(5,5, 75,75);
            Text name = new Text(player);
            name.getStyleClass().add("heading");
            name.setTextAlignment(TextAlignment.CENTER);
            VBox playerBox = new VBox(name, gameBoard);
            playerBox.setAlignment(Pos.CENTER);
            playerBox.setSpacing(10);
            playersGameboard.put(player, gameBoard);
            otherPlayers.getChildren().add(playerBox);
        }
        mainPane.setLeft(otherPlayers);
        otherPlayers.setAlignment(Pos.CENTER_LEFT);
        otherPlayers.setMaxHeight(this.gameWindow.getHeight());
    }


    /**
     * Updates GameBoards when a message is received
     * @param board game board
     */
    public void updatePlayerBoard(String board) {
        String[] boardParts = board.split(":");
        String playerName = boardParts[0];
        String[] boardValues = boardParts[1].split(" ");

        if (players.contains(playerName)) {
            GameBoard playerBoard = playersGameboard.get(playerName);

            int boardIndex = 0;
            for (int row = 0; row < game.getRows(); row++) {
                for (int col = 0; col < game.getCols(); col++) {
                    int cellValue = Integer.parseInt(boardValues[boardIndex]);
                    playerBoard.getGrid().set(col, row, cellValue);
                    boardIndex++;
                }
            }
        }
    }

}

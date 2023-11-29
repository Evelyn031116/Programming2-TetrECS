package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.*;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.*;
import java.security.spec.RSAOtherPrimeInfo;
import java.util.*;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {
    /**
     * score property
     */
    protected IntegerProperty scoreProperty = new SimpleIntegerProperty();
    /**
     * level property
     */
    protected IntegerProperty levelProperty = new SimpleIntegerProperty();
    /**
     * lives property
     */
    protected IntegerProperty livesProperty = new SimpleIntegerProperty();
    /**
     * multiplier property
     */
    protected IntegerProperty multiplierProperty = new SimpleIntegerProperty();
    /**
     * logger
     */
    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    /**
     * game
     */
    protected Game game;
    /**
     * current piece board
     */
    protected PieceBoard pieceBoard1;
    /**
     * following piece board
     */
    protected PieceBoard pieceBoard2;
    /**
     * game board
     */
    protected GameBoard board;
    /**
     * multimedia
     */
    protected Multimedia multimedia;
    /**
     * border pane
     */
    protected BorderPane mainPane;
    /**
     * the horizontal ordinate
     */
    protected int X;
    /**
     * the vertical ordinate
     */
    protected int Y;
    /**
     * game timer
     */
    protected Rectangle gameTimer;
    /**
     * stack pane
     */
    protected StackPane timerBox;
    /**
     * high scores
     */
    public SimpleIntegerProperty highScores = new SimpleIntegerProperty(0);

    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        // record the name of the class which is being built
        logger.info("Building " + this.getClass().getName());
        // initialization
        setupGame();

        this.scene = gameWindow.getScene();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

        // current piece board
        mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        // add board to the central position of the page
        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        mainPane.setCenter(board);
        // current piece board
        this.pieceBoard1 = new PieceBoard(3,3,150,150);
        // following piece board
        this.pieceBoard2 = new PieceBoard(3,3,100,100);

        gameTimer = new Rectangle();
        gameTimer.setHeight(20);
        gameTimer.setFill(Color.BLUE);

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(5, 5, 5, 5));
        mainPane.setRight(vBox);
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        mainPane.setTop(gridPane);

        //Score UI element
        var scoreLabel = new Text("Score: ");
        var scoreNum = new Text("0");
        // bind the UI element to the game properties
        scoreProperty.bind(game.scoreProperty());
        scoreNum.textProperty().bind(game.scoreProperty().asString());
        var scoreBox = new HBox(scoreLabel, scoreNum);
        scoreLabel.getStyleClass().add("heading");
        scoreNum.getStyleClass().add("score");

        //Level UI element
        var levelLabel = new Text("Level: ");
        var levelNum = new Text("1");
        // bind the UI element to the game properties
        levelProperty.bind(game.levelProperty());
        levelNum.textProperty().bind(game.levelProperty().asString());
        var levelBox = new HBox(levelLabel, levelNum);
        levelLabel.getStyleClass().add("heading");
        levelNum.getStyleClass().add("level");

        //Lives UI element
        var livesLabel = new Text("Lives: ");
        var livesNum = new Text("3");
        // bind the UI element to the game properties
        livesProperty.bind(game.livesProperty());
        livesNum.textProperty().bind(game.livesProperty().asString());
        var livesBox = new HBox(livesLabel, livesNum);
        livesLabel.getStyleClass().add("heading");
        livesNum.getStyleClass().add("lives");

        var multiplierLabel = new Text("Multiplier: ");
        var multiplierNum = new Text("1");
        multiplierProperty.bind(game.multiplierProperty());
        multiplierNum.textProperty().bind(game.multiplierProperty().asString());
        var multiplierBox = new HBox(multiplierLabel, multiplierNum);
        multiplierLabel.getStyleClass().add("heading");
        multiplierNum.getStyleClass().add("lives");

        //HighScore UI element
        var highScore = new Text("Highscore: ");
        var highScoreText = new Text();
        highScoreText.textProperty().bind(highScores.asString());
        var highScoreBox = new VBox(highScore, highScoreText);
        highScore.getStyleClass().add("heading");
        highScoreText.getStyleClass().add("heading");

        //Adjusts Alignment for the HighScore element
        highScoreBox.setAlignment(Pos.TOP_CENTER);
        highScoreBox.setTranslateY(-30);
        highScoreBox.setTranslateX(22.5);

        Text title = new Text("Challenge Mode");
        HBox.setHgrow(title, Priority.ALWAYS);
        title.getStyleClass().add("title");
        title.setTextAlignment(TextAlignment.CENTER);

        gridPane.add(scoreBox, 0, 0);
        gridPane.add(livesBox, 2, 0);
        gridPane.add(multiplierBox,2,1);
        gridPane.add(levelBox,0,1);
        gridPane.add(title, 1, 0);

        GridPane.setFillWidth(title, true);
        GridPane.setHgrow(title, Priority.ALWAYS);
        GridPane.setHalignment(title, HPos.CENTER);


        vBox.getChildren().add(pieceBoard1);
        vBox.getChildren().add(pieceBoard2);
        pieceBoard2.setPadding(new Insets(20, 0, 0, 0));

        // Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);
        scene.setOnKeyPressed(this::keyboardSupport);
        board.setOnRightClicked(this::rotate);
        pieceBoard1.setOnBlockClick(this::rotate);
        pieceBoard2.setOnBlockClick(this::swapCurrentPiece);
        pieceBoard1.setCentralCircle();
        game.setOnGameLoop(this::gameLoop);
        game.setOnGameOver(game -> {
            endGame();
            gameWindow.startScores(game);
        });
        game.scoreProperty().addListener(this::getHighScore);


        // Timer bar
        this.timerBox = new StackPane();
        mainPane.setBottom(timerBox);
        this.timerBox.getChildren().add(gameTimer);
        StackPane.setAlignment(gridPane, Pos.CENTER_LEFT);
        BorderPane.setMargin(this.timerBox, new Insets(10, 10, 10, 10));

        game.setNextPieceListener((gamePiece1, followingPiece) -> nextPiece(gamePiece1));
    }

    /**
     * check which key was pressed and perform different actions
     * @param keyEvent keyboard input
     */
    protected void keyboardSupport(KeyEvent keyEvent) {
        // If the ESCAPE key was pressed, end the game and log a message using a logger object with the level INFO
        if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
            endGame();
            if(!(game instanceof MultiplayerGame)) {
                gameWindow.startMenu();
            }
            logger.info("Escape");
            // If the ENTER or X key was pressed, drop a piece
        } else if (keyEvent.getCode().equals(KeyCode.ENTER)
                || keyEvent.getCode().equals(KeyCode.X)) {
            this.blockClicked(this.board.getBlock(this.X, this.Y));
            // If the SPACE or R key was pressed, swap the current piece
        } else if (keyEvent.getCode().equals(KeyCode.SPACE)
                || keyEvent.getCode().equals(KeyCode.R)) {
            this.swapCurrentPiece();
            // If the Q, Z, or [ key was pressed, rotate the piece by 3 times
        } else if (keyEvent.getCode().equals(KeyCode.Q)
                || keyEvent.getCode().equals(KeyCode.Z)
                || keyEvent.getCode().equals(KeyCode.OPEN_BRACKET)) {
            this.rotate(3);
            // If the E, C, or ] key was pressed, rotate the piece by 1 time
        } else if (keyEvent.getCode().equals(KeyCode.E)
                || keyEvent.getCode().equals(KeyCode.C)
                || keyEvent.getCode().equals(KeyCode.CLOSE_BRACKET)) {
            this.rotate(1);
            // move the piece to left by 1 block
        } else if (keyEvent.getCode().equals(KeyCode.A)
                || keyEvent.getCode().equals(KeyCode.LEFT)) {
            if (this.X > 0) {
                X -= 1;
            }else{
                Multimedia.playDocumentMusic("fail.wav");
            }
            // move the piece to right by 1 block
        } else if (keyEvent.getCode().equals(KeyCode.D)
                || keyEvent.getCode().equals(KeyCode.RIGHT)) {
            if (this.X < game.getCols() - 1) {
                X++;
            }else{
                Multimedia.playDocumentMusic("fail.wav");
            }
            // move the piece up by 1 block
        } else if (keyEvent.getCode().equals(KeyCode.W)
                || keyEvent.getCode().equals(KeyCode.UP)) {
            if (this.Y > 0) {
                Y -= 1;
            }else{
                Multimedia.playDocumentMusic("fail.wav");
            }
            // move the piece down by 1 block
        } else if (keyEvent.getCode().equals(KeyCode.S)
                || keyEvent.getCode().equals(KeyCode.DOWN)) {
            if (this.Y < game.getRows() - 1) {
                Y++;
            }else{
                Multimedia.playDocumentMusic("fail.wav");
            }
        }
        board.hover(board.getBlock(X, Y));
    }

    /**
     * rotate the game block by 3 times
     * @param gameBlock game block
     */
    protected void rotate(GameBlock gameBlock){
        rotate(3);
    }

    /**
     * rotate game piece
     * @param rotations rotation times
     */
    protected void rotate(int rotations) {
        Multimedia.playDocumentMusic("rotate.wav");
        game.rotateCurrentPiece(rotations);
        pieceBoard1.displayPiece(game.getCurrentPiece());
    }

    /**
     * swap current piece
     * @param gameBlock game block
     */
    protected void swapCurrentPiece(GameBlock gameBlock) {
        swapCurrentPiece();
    }

    /**
     * swap current piece
     */
    protected void swapCurrentPiece() {
        Multimedia.playDocumentMusic("rotate.wav");
        game.swapCurrentPiece();
        pieceBoard1.displayPiece(game.getCurrentPiece());
        pieceBoard2.displayPiece(game.getNewPiece());
    }

    /**
     * block clicked
     * @param block game block
     */
    protected void blockClicked(GameBlock block) {
        game.blockClicked(block);
    }

    /**
     * clear lines
     * @param gameBlockCoordinates coordinates of game blocks
     */
    protected void lineCleared(Set<GameBlockCoordinate> gameBlockCoordinates) {
        Multimedia.playDocumentMusic("clear.wav");
        board.fadeOut(gameBlockCoordinates);
    }

    /**
     * Sets the timer for the next turn
     * @param delay represents how long the timer will last.
     */
    protected void gameLoop(int delay) {
        Timeline timeline = new Timeline();
        KeyValue startFill = new KeyValue(this.gameTimer.fillProperty(), Color.PURPLE);
        KeyValue endFill = new KeyValue(this.gameTimer.fillProperty(), Color.GREEN);
        KeyValue halfTimeFill = new KeyValue(this.gameTimer.fillProperty(), Color.YELLOW);
        KeyValue threeQuarterTimeFill = new KeyValue(this.gameTimer.fillProperty(), Color.ORANGE);
        KeyValue finalFill = new KeyValue(this.gameTimer.fillProperty(), Color.RED);
        KeyValue endWidth = new KeyValue(this.gameTimer.widthProperty(), 0);
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO, startFill, new KeyValue(this.gameTimer.widthProperty(), this.timerBox.getWidth())),
                new KeyFrame(new Duration((double) delay * 0.125), endFill),
                new KeyFrame(new Duration((double) delay * 0.25), halfTimeFill),
                new KeyFrame(new Duration((double) delay * 0.5), threeQuarterTimeFill),
                new KeyFrame(new Duration((double) delay * 0.75), finalFill),
                new KeyFrame(new Duration((double) delay), endWidth)
        );
        timeline.play();
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.start();
        this.multimedia.playBgmMusic("game.wav");
        X = 0;
        Y = 0;
        this.scene.setOnKeyPressed(this::keyboardSupport);
        this.game.setOnLineCleared(this::lineCleared);
        game.start();
        setHighScores();
    }

    /**
     * next piece
     * @param gamePiece1 current piece
     */
    public void nextPiece(GamePiece gamePiece1) {
        pieceBoard1.displayPiece(gamePiece1);
        pieceBoard2.displayPiece(game.getNewPiece());
    }

    /**
     *end game
     */
    public void endGame(){
        if(!(game instanceof MultiplayerGame)) {
            logger.info("Game Over");
            gameTimer.setVisible(false);
//        game.gameOver();
            Multimedia.stopBgmMusic();
            Multimedia.playDocumentMusic("transition.wav");
            gameWindow.startMenu();
        }
    }

    /**
     * get high score
     * @param observable observable value
     * @param oldValue old value
     * @param newValue new value
     */
    protected void getHighScore(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        setHighScores();
    }

    /**
     * set high scores
     */
    protected void setHighScores() {
        File file = new File("scores.txt");
        int highScore = 0;
        try (Scanner scanner = new Scanner(file)) {
            if (file.exists()){
            List<Pair<String, Integer>> scores = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String[] nameScore = scanner.nextLine().split(":");
                Pair<String, Integer> pair = new Pair<>(nameScore[0], Integer.parseInt(nameScore[1]));
                scores.add(pair);
            }
            scores.sort((a, b) -> b.getValue() - a.getValue());
            highScore = scores.get(0).getValue();
        } else {
                highScore = game.scoreProperty().get();
            }
        }
        catch (IOException e) {
            logger.error("Unable to find high scores", e);
        }
        if (game.scoreProperty().get() > highScore) {
            highScores.set(game.scoreProperty().get());
        } else {
            highScores.set(highScore);
        }
    }



}

package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.Multimedia;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

/**
 * scores scene
 */
public class ScoresScene extends BaseScene{
    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    /**
     *logger
     */
    private static final Logger logger = LogManager.getLogger(ScoresScene.class);
    /**
     * final game state
     */
    protected Game finalGameState;
    /**
     * scores of players
     */
    protected int score;
    /**
     * scores of multiplayer
     */
    protected SimpleListProperty<Pair<String, Integer>> multiplayerScores;
    /**
     * hold the current list of scores in the Scene
     */
    protected SimpleListProperty<Pair<String, Integer>> localScoresList = new SimpleListProperty<>();
    /**
     * hold the current list of scores of online users in the Scene
     */
    protected SimpleListProperty<Pair<String, Integer>> remoteScoresList = new SimpleListProperty<>();
    /**
     * communicator
     */
    protected Communicator communicator;
    /**
     * usernames
     */
    protected String userName;
    /**
     * list of local scores
     */
    private ScoresList localList;
    /**
     * list of online scores
     */
    private ScoresList remoteList;
    /**
     * judge whether is multiplayer mode
     */
    protected boolean isMultiplayer = false;

    /**
     * scores scene in single mode
     * @param gameWindow game window
     * @param game game
     */
    public ScoresScene(GameWindow gameWindow, Game game) {
        super(gameWindow);
        finalGameState = game;
        score = game.scoreProperty().get();
        this.localScoresList.set(FXCollections.observableArrayList(new ArrayList<Pair<String, Integer>>()));
        this.remoteScoresList.set(FXCollections.observableArrayList(new ArrayList<Pair<String, Integer>>()));
        logger.info("Creating Scores Scene");
        communicator = gameWindow.getCommunicator();
    }

    /**
     * scores scene in multiplayer mode
     * @param gameWindow game window
     * @param game game
     * @param scores scores
     */
    public ScoresScene(GameWindow gameWindow, Game game, SimpleListProperty<Pair<String, Integer>> scores) {
        super(gameWindow);
        finalGameState = game;
        score = game.scoreProperty().get();
        this.localScoresList.set(FXCollections.observableArrayList(new ArrayList<Pair<String, Integer>>()));
        this.remoteScoresList.set(FXCollections.observableArrayList(new ArrayList<Pair<String, Integer>>()));
        multiplayerScores = scores;
        logger.info("Creating Scores Scene");
        communicator = gameWindow.getCommunicator();
        isMultiplayer = true;
    }

    @Override
    public void initialise() {
        Multimedia.playDocumentMusic("explode.wav");
        Multimedia.playBgmMusic("end.wav");

        if(!isMultiplayer) {
            loadScores();
            addNamesAndScores(this.userName,this.score);
        } else {
            this.localScoresList.addAll(multiplayerScores);
            this.localScoresList.sort((a, b) -> b.getValue() - a.getValue());
        }

        this.scene.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                Multimedia.playDocumentMusic("transition.wav");
                gameWindow.startMenu();
                logger.info("Exit");
                if (isMultiplayer) {
                    communicator.send("PART");
                }
            }
        });

        loadOnlineScores();
        this.communicator.addListener(message -> Platform.runLater(() -> this.getMessage(message.trim())));
    }


    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var scorePane = new StackPane();
        scorePane.getStyleClass().add("menu-background");
        root.getChildren().add(scorePane);

        var mainPane = new BorderPane();
        scorePane.getChildren().add(mainPane);

// add image title
        var imageBox = new VBox();
        imageBox.setAlignment(Pos.TOP_CENTER);
        mainPane.setCenter(imageBox);

        Image title = new Image(ScoresScene.class.getResource("/images/TetrECS.png").toExternalForm());
        ImageView image = new ImageView(title);
        image.setPreserveRatio(true);
        image.setFitWidth((double) this.gameWindow.getWidth() * 0.5);

        imageBox.getChildren().add(image);

// show local scores list and online scores list
        var scoreBox = new HBox();
        scoreBox.setAlignment(Pos.CENTER);
        scorePane.getChildren().add(scoreBox);

// local scores list
        var localScoresBox = new VBox();
        localScoresBox.setAlignment(Pos.CENTER);
        scoreBox.getChildren().add(localScoresBox);

// online scores list
        var onlineScoresBox = new VBox();
        onlineScoresBox.setAlignment(Pos.CENTER);
        scoreBox.getChildren().add(onlineScoresBox);

        var gameoverTtl = new Text("Game Over");
        gameoverTtl.getStyleClass().add("bigtitle");
        mainPane.setTop(gameoverTtl);

        var highScoresTtl = new Text("High Scores");
        highScoresTtl.getStyleClass().add("title");
        mainPane.setBottom(highScoresTtl);

        var localScoresTitle = new Text("Local Scores");
        localScoresTitle.getStyleClass().add("heading");
        localScoresBox.getChildren().add(localScoresTitle);

        localList = new ScoresList();
        localList.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        localScoresBox.getChildren().add(localList);
        localList.setAlignment(Pos.CENTER);
        this.localScoresList.bind(localList.getScores());
        localList.getStyleClass().add("heading");

        var onlineScoresTitle = new Text("Online Scores");
        onlineScoresTitle.getStyleClass().add("heading");
        onlineScoresBox.getChildren().add(onlineScoresTitle);

        remoteList = new ScoresList();
        remoteList.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        onlineScoresBox.getChildren().add(remoteList);
        remoteList.setAlignment(Pos.CENTER);
        this.remoteScoresList.bind(remoteList.getScores());
        remoteList.getStyleClass().add("heading");

        if (!isMultiplayer) {
            var nameDialog = new TextInputDialog();
            nameDialog.setTitle("Name input");
            nameDialog.setContentText("Please enter your name");
            Optional<String> result = nameDialog.showAndWait();
            this.userName = result.orElse("User");
        }

        var exit = new Button("Exit and back to start menu");
        exit.getStyleClass().addAll("menuItem", "transparent-button");
        scorePane.getChildren().add(exit);
        exit.setOnAction(this::backToStartMenu);
        exit.setAlignment(Pos.CENTER);
        exit.setTranslateY(200);

        exit.hoverProperty().addListener((ov, oldValue, newValue) -> {
            if (newValue) {
                exit.setStyle("-fx-text-fill: yellow");
            } else {
                exit.setStyle("-fx-text-fill: white");
            }
        });
        exit.setStyle("-fx-text-fill: white");
    }

    /**
     * load a set of high scores from a file and populate an ordered list
     */
    protected void loadScores() {
        logger.info("Loading scores");
        File file = new File("scores.txt");
        try {
// Check if the file exists
            if (file.exists()) {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
// Split the input to usernames and scores
                    String[] usernamesAndScores = line.split(":");
// Save usernames and scores into data
                    var data = new Pair<String, Integer>(usernamesAndScores[0], Integer.parseInt(usernamesAndScores[1]));
                    this.localScoresList.add(data);
                }
                bufferedReader.close();
            } else {
// If the file doesn't exist, create a new file and write default scores
                file.createNewFile();
                writeScores();
            }
        } catch (IOException e) {
            logger.error("Unable to load scores");
            e.printStackTrace();
        }
    }

    private void writeScores() {
        logger.info("Writing scores");
        ArrayList<Pair<String, Integer>> scores = new ArrayList<>();
        scores.add(new Pair<>("Jingyi", 10000));
        scores.add(new Pair<>("Jingyi", 9000));
        scores.add(new Pair<>("Jingyi", 8000));
        scores.add(new Pair<>("Jingyi", 7000));
        scores.add(new Pair<>("Jingyi", 6000));
        scores.add(new Pair<>("Jingyi", 5000));
        scores.add(new Pair<>("Jingyi", 4000));
        scores.add(new Pair<>("Jingyi", 3000));
        File file = new File("scores.txt");
        try (FileWriter fileWriter = new FileWriter(file);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            for (Pair<String, Integer> pair : scores) {
                String usernamesAndScores = pair.getKey() + ":" + pair.getValue();
                localScoresList.add(pair);
                bufferedWriter.write(usernamesAndScores);
                bufferedWriter.newLine();
            }
        } catch (IOException e) {
            logger.error("Unable to write scores", e);
        }
    }

    /**
     * add usernames and scores to the file and add them to local score list
     * @param names usernames
     * @param scores scores
     */
    public void addNamesAndScores(String names, int scores) {
        File file = new File("scores.txt");
        try (FileWriter writer = new FileWriter(file, true);
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            bufferedWriter.write(names + ":" + scores);
            bufferedWriter.newLine();
            this.localScoresList.add(new Pair<>(this.userName, this.score));
            this.localScoresList.sort((a, b) -> b.getValue() - a.getValue());
        } catch (IOException e) {
            logger.error("Unable to add score to text file", e);
        }
    }

    /**
     * set on actions when the user exits and back to start menu
     * @param actionEvent action event
     */
    protected void backToStartMenu(ActionEvent actionEvent) {
        gameWindow.startMenu();
        Multimedia.stopBgmMusic();
        Multimedia.playDocumentMusic("transition.wav");
        if(isMultiplayer) {
            communicator.send("PART");
        }
    }

    /**
     * load online scores
     */
    protected void loadOnlineScores() {
        communicator.send("HISCORES");
    }

    /**
     * write online scores
     */
    protected void writeOnlineScore() {
        communicator.send("HISCORE " + this.userName + ":" + this.score);
    }

    /**
     * get message
     * @param message message
     */
    protected void getMessage(String message) {
        if (message.contains("NEWSCORE")) {
            logger.info("Server received highscore");
            return;
        }

        if (!message.contains("HISCORES")) {
            return;
        }

        message = message.replace("HISCORES", "");
        String[] scorePairs = message.split("\n");
        remoteScoresList.clear(); // clear the remoteScoresList before adding new scores
        for (String scorePair : scorePairs) {
            String[] scoreAndName = scorePair.split(":");
            remoteScoresList.add(new Pair<>(scoreAndName[0], Integer.parseInt(scoreAndName[1])));
        }

        remoteScoresList.sort((a, b) -> b.getValue() - a.getValue()); // sort the remoteScoresList in descending order of scores

        int lowestScoreOnList = remoteScoresList.get(Math.min(8, remoteScoresList.size() - 1)).getValue();
        if (score > lowestScoreOnList) {
            writeOnlineScore();
        }
    }




}

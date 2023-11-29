package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    private Multimedia multimedia = new Multimedia();

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        // title image
        Image title = new Image(MenuScene.class.getResource("/images/TetrECS.png").toExternalForm());
        ImageView image = new ImageView(title);
        // maintain in the aspect ratio
        image.setPreserveRatio(true);
        // set the height to 135
        image.setFitHeight(135);
        // set the Y-axis offset to -120
        image.setTranslateY(-120);
        // add the image to menuPane
        menuPane.getChildren().add(image);

        // title animations
        // set the duration of the animation to 5000 millisecond
        RotateTransition rotate = new RotateTransition(Duration.millis(5000), image);
        // set the starting angle of the image to 15
        rotate.setFromAngle(-12);
        // set the ending angle of the image to -15
        rotate.setToAngle(12);
        rotate.setByAngle(0);
        // make the image keep rotating
        rotate.setCycleCount(-1);
        // set easing effects at the beginning and the end to make the animation more smooth
        rotate.setInterpolator(Interpolator.EASE_BOTH);
        // make the animation automatically reverse playback
        rotate.setAutoReverse(true);
        rotate.play();

        // play background music
        Multimedia.playBgmMusic("menu.mp3");

        /*
        //Awful title
        var title = new Text("TetrECS");
        title.getStyleClass().add("title");
        mainPane.setTop(title);
         */

        // set buttons
        var singlePlayer = new Button("Single Player");
        var multiPlayer = new Button("Multi Player");
        var instructions = new Button("How to Play");
        var exit = new Button("Exit");

        // set the distance of each buttons to 15
        var v = new VBox(15, singlePlayer, multiPlayer, instructions, exit);
        menuPane.getChildren().add(v);
        // set the style of the buttons to menuItem
        v.getStyleClass().add("menuItem");
        // place the buttons in the center of the page
        v.setAlignment(Pos.BOTTOM_CENTER);
        // set the background of the buttons to null which means they don't have backgrounds or colours
        singlePlayer.setBackground(null);
        multiPlayer.setBackground(null);
        instructions.setBackground(null);
        exit.setBackground(null);

        // start single player game when clicking the button
        singlePlayer.setOnAction(this::startGame);
        // start multiplayer game when clicking the button
        multiPlayer.setOnAction(this::startMultiplayer);
        // view instructions when clicking the button
        instructions.setOnAction(this::startInstructions);
        // end the game and exit when clicking "Exit"
        exit.setOnAction((ActionEvent event) -> {
            System.exit(0);
        });
        for (Node node: v.getChildren()) {
            // set up a listener to detect events that occur when the mouse hovers over a button
            node.hoverProperty().addListener((ov, oldValue, newValue) -> {
                // When newValue is true (ie the mouse is over a button), change the node's text color to yellow
                if (newValue) {
                    node.setStyle("-fx-text-fill: yellow");
                } else {
                    // otherwise, change the text color to white
                    node.setStyle("-fx-text-fill: white");
                }
            });
            // set the text colour to white at first
            node.setStyle("-fx-text-fill: white");
        }

        /*
        //For now, let us just add a button that starts the game. I'm sure you'll do something way better.
        var button = new Button("Play");
        mainPane.setCenter(button)

        //Bind the button action to the startGame method in the menu
        button.setOnAction(this::startGame);
         */
    }

    /**
     * Initialise the menu
     * Allow the user to press escape to exit the the game itself
     */
    @Override
    public void initialise() {
        scene.setOnKeyPressed((e) -> {
            Multimedia.playDocumentMusic("transition.wav");
            this.gameWindow.startMenu();
            logger.info("Exit");
        });
    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
        Multimedia.playDocumentMusic("transition.wav");
        Multimedia.stopBgmMusic();
    }

    /**
     * Handle when the Multiplayer button is pressed
     * @param event event
     */
    private void startMultiplayer(ActionEvent event) {
        gameWindow.startLobby();
        Multimedia.playDocumentMusic("transition.wav");
        Multimedia.stopBgmMusic();
    }

    /**
     * Handle when the Instructions button is pressed
     * @param event event
     */
    private void startInstructions(ActionEvent event) {
        gameWindow.startInstructions();
        Multimedia.playDocumentMusic("transition.wav");
        Multimedia.stopBgmMusic();
    }


}

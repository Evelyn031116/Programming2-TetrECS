package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.Multimedia;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * instructions scene
 */
public class InstructionsScene extends BaseScene{
    /**
     * multimedia
     */
    protected Multimedia multimedia = new Multimedia();
    /**
     * logger
     */
    private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public InstructionsScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Create instructions scene");
    }

    /**
     * Allow the user to press escape to exit the instructions
     */
    @Override
    public void initialise() {
        scene.setOnKeyPressed((e) -> {
            Multimedia.playDocumentMusic("transition.wav");
            this.gameWindow.startMenu();
            logger.info("Exit");
        });
    }

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

        // instructions title
        var instructionsTtl = new Text("Instructions");
        mainPane.getChildren().add(instructionsTtl);
        instructionsTtl.getStyleClass().add("heading");
        StackPane.setAlignment(instructionsTtl, Pos.CENTER);
        // Set the vertical position of the "instructions" text object to 30 pixels
        instructionsTtl.setLayoutY(30);
        // Set the horizontal position of the "instructions" text object to 350 pixels
        instructionsTtl.setLayoutX(350);

        // instructions image
        Image instructionsIma = new Image(MenuScene.class.getResource("/images/Instructions.png").toExternalForm());
        ImageView image = new ImageView(instructionsIma);
        image.setPreserveRatio(true);
        image.setFitWidth(620);
        image.setX(110);
        image.setY(30);
        mainPane.getChildren().add(image);

        // pieces title
        var piecesTtl = new Text("Pieces");
        mainPane.getChildren().add(piecesTtl);
        piecesTtl.getStyleClass().add("heading");
        StackPane.setAlignment(instructionsTtl, Pos.CENTER);
        // Set the vertical position of the "Pieces" text object to 500 pixels
        piecesTtl.setLayoutY(500);
        // Set the horizontal position of the "Pieces" text object to 130 pixels
        piecesTtl.setLayoutX(130);

        // pieces image
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        // Set the preferred size of the "gridPane" grid layout object to be 100 pixels wide and the same height as the "gameWindow" object
        gridPane.setPrefSize(100, gameWindow.getWidth());

        mainPane.getChildren().add(gridPane);
        // Set the row spacing of the "gridPane" grid layout object to 10 pixels
        gridPane.setVgap(10);
        // Set the column spacing of the "gridPane" grid layout object to 10 pixels
        gridPane.setHgap(10);
        // Set the vertical position of the "Pieces" text object to 500 pixels
        gridPane.setLayoutY(500);
        // Set the horizontal position of the "Pieces" text object to 400 pixels
        gridPane.setLayoutX(400);

        int x = 0;
        int y = 0;
        for (int i = 0; i < 15; i++) {
            GamePiece piece = GamePiece.createPiece(i);
            // Create a game board object called "gameBoard" and initialize it to a board with 3 rows and 3 columns, a width of 50 pixels, and a height of 50 pixels
            PieceBoard gameBoard = new PieceBoard(3, 3, 50, 50);
            gameBoard.displayPiece(piece);
            // Add the game board object "gameBoard" to the specified position of the "gridPane" grid layout object
            gridPane.add(gameBoard, x, y);
            // Makes the next checkerboard object appear in the next column
            x++;
            // If the value of the "x" variable is equal to 5, reset the "x" variable to 0 and increment the "y" variable by 1 so that the next checkerboard object is displayed on the next row. Because each row displays 5 checkerboard objects, when the value of the "x" variable is equal to 5, it needs to be reset to 0, starting from the first column of the next row
            if (x == 5) {
                x = 0;
                y++;
            }
        }
    }
}

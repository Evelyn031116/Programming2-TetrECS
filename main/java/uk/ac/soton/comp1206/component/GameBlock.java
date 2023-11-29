package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };
    /**
     * game board
     */
    private final GameBoard gameBoard;
    /**
     * width of the graphic context
     */
    private final double width;
    /**
     * height of the graphic context
     */
    private final double height;
    /**
     * judge whether the block is the center of the piece
     */
    public boolean center;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);
    /**
     * judge whether need to hover the block
     */
    public boolean hoverOrNot = false;
    /**
     * a timer for animation
     */
    public AnimationTimer animationTimer;


    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }
        if(center){
            paintCentralDot();
        }
        if(hoverOrNot){
            paintHover();
        }
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        gc.setFill(Color.web("BLACK", 0.5));
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.WHITE);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Colour fill
        gc.setFill(colour);
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);

        //Triangle Fill
        gc.setFill(Color.color(1,1, 1, 0.5));
        gc.fillPolygon(new double[]{0.0, 0.0, width}, new double[]{0, height, height}, 3);
    }

    /**
     * paint the central dot
     */
    private void paintCentralDot(){
        GraphicsContext graphicsContext = this.getGraphicsContext2D();
        graphicsContext.setFill(Color.color(1, 1, 1, 1));
        graphicsContext.fillOval(width / 4, height / 4, width / 2, height / 2);
    }

    /**
     * set the central dot
     */
    public void setCentralCircle() {
        center = true;
        paint();
    }

    /**
     *
     * @param hover judge whether the block needs to be hovered
     */
    public void setHovering(boolean hover) {
        this.hoverOrNot = hover;
        paint();
    }

    /**
     * paint the hover animation
     */
    private void paintHover() {
        var gc = getGraphicsContext2D();
        gc.setStroke(Color.WHITE);
        gc.strokeRect(0,0,width,height);
        if(value.get() == 0) {
            gc.setFill(Color.WHITE.deriveColor(0,0,1,0.5));
            gc.fillRect(0,0, width, height);
        }
    }

    /**
     * use this to flash and then fades out to indicate a cleared block
     */
    public void fadeOut() {
        this.animationTimer = new AnimationTimer() {

            // tore the current fade level, which will gradually decrease from 1 to 0
            double fade = 1;
            @Override
            public void handle(long l) {
                {
                    // draw an empty object on the screen
                    paintEmpty();
                    var graphicsContext = getGraphicsContext2D();
                    // gradually decrease the opacity of the object on the screen
                    fade = fade - 0.05;
                    if (fade <= 0) {
                        animationTimer.stop();
                        // remove the reference to the object and allow it to be garbage collected
                        animationTimer = null;
                    }
                    // set the fill color of the rectangle to a semi-transparent white color
                    graphicsContext.setFill(Color.WHITE.deriveColor(0,0,1,fade));
                    // draw a filled rectangle on the screen that covers the entire object
                    graphicsContext.fillRect(0,0,width,height);
                }
            }
        };
        animationTimer.start();
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

}

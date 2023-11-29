package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.*;

/**
 * lobby scene
 */
public class LobbyScene extends BaseScene{
    /**
     * logger
     */
    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    /**
     * timer
     */
    protected Timer timer;
    /**
     * communicator
     */
    protected Communicator communicator;
    /**
     * vbox of channel
     */
    protected VBox channelNames = new VBox();
    /**
     * nick name button
     */
    protected Button nickName;
    /**
     * start channel button
     */
    protected Button startChannel;
    /**
     * start game button
     */
    protected Button startGame;
    /**
     * leave button
     */
    protected Button leave;
    /**
     * channel vbox
     */
    protected VBox channelBox = new VBox();
    /**
     * channel text vbox
     */
    protected Text channelText = new Text();
    /**
     * message box
     */
    protected VBox messageBox = new VBox();
    /**
     * gridpane of players
     */
    protected GridPane playersPane;
    /**
     * set of players
     */
    protected Set<String> playerSet = new HashSet<>();
    /**
     * usernames
     */
    protected String username;
    /**
     * border pane
     */
    protected BorderPane borderPane;
    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        this.scene = gameWindow.getScene();
    }

    @Override
    public void initialise() {
        this.timer = new Timer();
        this.scene.setOnKeyPressed(keyEvent -> {
            // Leave the game and channel when escape is pressed
            if(keyEvent.getCode() == KeyCode.ESCAPE) {
                handleEscapeKeyPressed();
            }
        });
        scheduleChannelListRefresh();
        setCommunicator();
        Multimedia.playBgmMusic("end.wav");
    }

    private void handleEscapeKeyPressed() {
        Multimedia.playDocumentMusic("transition.wav");
        Multimedia.stopBgmMusic();
        gameWindow.startMenu();
        logger.info("Exit");
        communicator.send("PART");
    }

    private void scheduleChannelListRefresh() {
        timer.scheduleAtFixedRate(new TimerTask() {
            // search the channel for new messages every 3 seconds
            @Override
            public void run() {
                searchForNewMessages();
            }
        }, 1000, 5000);
    }

    private void searchForNewMessages() {
        communicator.send("LIST");
    }

    private void setCommunicator() {
        communicator = gameWindow.getCommunicator();
        communicator.addListener(this::handleIncomingMessage);
    }

    private void handleIncomingMessage(String message) {
        Platform.runLater(() -> listen(message.trim()));
    }


    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var lobbyPane = new StackPane();
        lobbyPane.setMaxWidth(gameWindow.getWidth());
        lobbyPane.setMaxHeight(gameWindow.getHeight());
        lobbyPane.getStyleClass().add("menu-background");
        root.getChildren().add(lobbyPane);

        borderPane = new BorderPane();
        lobbyPane.getChildren().add(borderPane);
        borderPane.setMaxSize(lobbyPane.getMaxWidth(), lobbyPane.getMaxHeight());

        var vbox = new VBox();
        borderPane.setLeft(vbox);

        // set start channel
        startChannel = new Button("Start A New Channel");
        startChannel.setStyle("-fx-border-color: transparent;");
        startChannel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                var textInputDialog = new TextInputDialog();
                textInputDialog.setTitle("New Channel");
                textInputDialog.setContentText("Please enter your name for new channel");
                Optional<String> userInput = textInputDialog.showAndWait();
                if(userInput.isPresent()) {
                    communicator.send("CREATE " + userInput.get());
                } else {
                    communicator.send("CREATE channel");
                }
            }
        });

        // edit nickname
        nickName = new Button("Edit Nick Name");
        nickName.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                var textInputDialog = new TextInputDialog();
                textInputDialog.setTitle("Edit Nickname");
                textInputDialog.setContentText("Please enter your new nickname: ");
                Optional<String> userInput = textInputDialog.showAndWait();
                if(userInput.isPresent()) {
                    communicator.send("NICK " + userInput.get());
                }
            }
        });

        // leave the channel
        leave = new Button("Leave");
        leave.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                leave.setVisible(false);
                nickName.setVisible(false);
                channelBox.setVisible(false);
                channelText.setText(" ");
                messageBox.getChildren().clear();
                communicator.send("PART");
            }
        });

        // UI when users join the channel
        channelBox = new VBox();
        channelBox.setSpacing(3);
        channelBox.setPadding(new Insets(0, 35, 0, 0));
        channelBox.setAlignment(Pos.CENTER_RIGHT);
        channelBox.setMaxWidth(gameWindow.getWidth());
        channelBox.setMaxHeight(gameWindow.getHeight());
        channelBox.getStyleClass().add("gameBox");

        var messagePane = new BorderPane();
        messagePane.setPrefSize(gameWindow.getWidth()/2, gameWindow.getHeight()/2);

        // make the chat window can scroll down
        var messageSPane = new ScrollPane();
        messageSPane.getStyleClass().add("scroller");
        messageSPane.setPrefSize(messagePane.getWidth(), messagePane.getHeight() - 100);
        messageSPane.needsLayoutProperty().addListener((observable, oldValue, newValue) -> updateVvalue(newValue, messageSPane));

        messageBox = new VBox();
        messageBox.getStyleClass().add("messages");
        messageBox.setPrefSize(messageSPane.getPrefWidth(), messageSPane.getPrefHeight());
        // make message box take up the window as much as possible
        VBox.setVgrow(messageSPane, Priority.ALWAYS);

        messageSPane.setContent(messageBox);
        messagePane.setCenter(messageSPane);

        // field for users to edit message
        var textField = new TextField();
        textField.getStyleClass().add("TextField");
        var sendMessage = new Button("Send message");
        textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if(keyEvent.getCode() == KeyCode.ENTER) {
                    String message = textField.getText();
                    if(message != null) {
                        communicator.send("MSG " + message);
                        textField.clear();
                    }
                }
            }
        });
        // send the message when users press the button
        sendMessage.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String message = textField.getText();
                if(message != null) {
                    communicator.send("MSG " + message);
                    textField.clear();
                }
            }
        });

        // start the game
        startGame = new Button("Start Game");
        startGame.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                communicator.send("START");
            }
        });

        playersPane = new GridPane();
        playersPane.setPrefWidth(messageSPane.getPrefWidth());

        var chatBox = new HBox(textField, sendMessage);
        var buttonBox = new HBox(nickName, leave);

        channelText = new Text();
        channelText.getStyleClass().add("heading");

        //adds all UI to channelBox
        channelBox.getChildren().addAll(channelText, buttonBox, messagePane, chatBox, startGame, playersPane);

        this.borderPane.setRight(channelBox);
        channelBox.setVisible(false);

        // set buttons' styles
        Button[] buttons = new Button[]{startChannel, nickName, leave, startGame};
        for (Button node : buttons) {
            node.hoverProperty().addListener((ov, oldValue, newValue) -> updateButtonStyle(node, newValue));
            setButtonStyle(node);
            setButtonClass(node);
            setButtonBackground(node);
        }
        vbox.getChildren().addAll(startChannel, channelNames);
    }

    /**
     * other ppl join existed channel
     * @param channelName name of channel
     */
    protected void joinChannel(String channelName) {
        nickName.setVisible(true);
        leave.setVisible(true);
        channelBox.setVisible(true);
        startGame.setVisible(false);
        Multimedia.playDocumentMusic("pling.wav");
        channelText.setText("You are in channel: " + channelName);
    }

    /**
     * add players
     * @param players players
     */
    protected void addPlayers(String players){
        playersPane.getChildren().clear();
        playerSet.clear();
        String[] playersArr = players.split("\n");
        int row = 0, col = 0;
        for (String player : playersArr) {
            playerSet.add(player);
            Text text = new Text(player);
            text.getStyleClass().add("heading");
            if (row < 3) {
                this.playersPane.add(text, col, row);
            } else if (row < 6) {
                this.playersPane.add(text, col, row - 3 + 1);
            } else {
                this.playersPane.add(text, col, row - 6 + 2);
            }
            col++;
            if (col == 3) {
                col = 0;
                row++;
            }
        }
        playersPane.setVgap(10);
        playersPane.setHgap(10);
    }

    /**
     * commands
     * @param s string
     */
    protected void listen(String s) {
        String[] commands= s.split("\\s+", 2); // 将字符串按第一个空格分割成两个元素的数组
        String command = commands[0]; // 获取命令字符串

        switch (command) {
            case "CHANNELS":
                // show all available channels
                channelNames.getChildren().clear();
                String[] channelArray = commands[1].split("\n");
                for (String channel : channelArray) {
                    Text text = new Text(channel);
                    text.setOnMouseClicked(event -> communicator.send("JOIN " + channel));
                    text.hoverProperty().addListener((ov, oldValue, newValue) -> {
                        if (newValue) {
                            text.setStyle("-fx-text-fill: yellow");
                        } else {
                            text.setStyle("-fx-text-fill: white");
                        }
                    });
                    text.getStyleClass().add("channelItem");
                    channelNames.getChildren().add(text);
                }
                break;

            case "JOIN":
                // join the channel
                String channelName = commands[1];
                joinChannel(channelName);
                break;

            case "MSG":
                // show new messages
                String[] messageArr = commands[1].split(":");
                if (messageArr.length > 1) {
                    Text message = new Text(messageArr[0] + " : " + messageArr[1]);
                    message.getStyleClass().add("messages Text");
                    messageBox.getChildren().add(message);
                }
                break;

            case "HOST":
                // when the player is the host
                startGame.setVisible(true);
                break;

            case "USERS":
                // show the list of current users
                addPlayers(commands[1]);
                break;

            case "START":
                // start the game
                startMultiplayer();
                break;

            case "NICK":
                // check the changes of users' nicknames
                username = commands[1];
                break;
        }
    }

    /**
     *
     * @param needsLayout judge whether need to layout
     * @param scrollPane scroll pane
     */
    private void updateVvalue(boolean needsLayout, ScrollPane scrollPane) {
        if (!needsLayout) {
            scrollPane.setVvalue(1.0);
        }
    }

    /**
     * update buttons styles
     * @param button button
     * @param isHovering judge whether the mouse is hovering on the block
     */
    private void updateButtonStyle(Button button, boolean isHovering) {
        if (isHovering) {
            button.setStyle("-fx-text-fill: yellow");
        } else {
            button.setStyle("-fx-text-fill: white");
        }
    }

    /**
     * set buttons' styles
     * @param button button
     */
    private void setButtonStyle(Button button) {
        button.setStyle("-fx-text-fill: white");
        if (button == startGame) {
            button.getStyleClass().clear();
            button.getStyleClass().add("smallMenuItem");
        }
    }

    /**
     * set buttons' styles
     * @param button button
     */
    private void setButtonClass(Button button) {
        button.getStyleClass().add("menuItem");
    }

    /**
     * set the background of the buttons as null
     * @param button
     */
    private void setButtonBackground(Button button) {
        button.setBackground(null);
    }

    /**
     * start multiplayer mode
     */
    protected void startMultiplayer() {
        playerSet.remove(username);
        Multimedia.playDocumentMusic("transition.wav");
        gameWindow.loadScene(new MultiplayerScene(gameWindow, playerSet));
        Multimedia.stopBgmMusic();
    }


}

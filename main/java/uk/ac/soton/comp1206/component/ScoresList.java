package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.ArrayList;

/**
 * ScoresList is a class contains all the scores
 */
public class ScoresList extends VBox {
    /**
     * list of scores
     */
    protected SimpleListProperty<Pair<String, Integer>> scores = new SimpleListProperty<>();
    /**
     * list of multiplayer
     */
    protected ArrayList<String> multiplayer = new ArrayList<>();

    /**
     * score list
     */
    public ScoresList() {
        this.scores.addListener(this::update);
        scores.set(FXCollections.observableArrayList(new ArrayList<Pair<String, Integer>>()));
    }

    /**
     * show players' scores
     * @param text scores list
     */
    protected void reveal(Text text) {
        FadeTransition showScores = new FadeTransition(new Duration(500), text);
        // set the start opacity to 0
        showScores.setFromValue(0);
        // set the end opacity to 1
        showScores.setToValue(1);
        // play the animation for 1 time
        showScores.setCycleCount(1);
        // no need to play the reverse animation after playing
        showScores.setAutoReverse(false);
        showScores.play();
    }

    /**
     * some preparations for updating
     * @param newValues new data of players' scores
     */
    private void updatePrepare(ObservableList<Pair<String, Integer>> newValues){
        this.getChildren().clear();
        int count = 1;
        for(Pair pair: newValues) {
            // only can hold 10 users' scores
            if (count < 11) {
                Text scoreTtl = new Text(pair.getKey() + " : " + pair.getValue());
                if(multiplayer.contains(pair.getKey())) {
                    scoreTtl.getStyleClass().add("strike");
                } else {
                    scoreTtl.getStyleClass().add("scorelist");
                }
                this.getChildren().add(scoreTtl);
                this.reveal(scoreTtl);
                count ++;
            } else {
                break;
            }
        }
    }

    /**
     * update new data
     * @param values data
     * @param oldValues old data
     * @param newValues new data
     */
    private void update(ObservableValue<?  extends ObservableList<Pair<String, Integer>>> values, ObservableList<Pair<String, Integer>> oldValues, ObservableList<Pair<String, Integer>> newValues){
        this.updatePrepare(newValues);
    }

    /**
     * get scores
     * @return scores
     */
    public ListProperty<Pair<String, Integer>> getScores() {
        return this.scores;
    }

    /**
     * remove dead users
     * @param item string
     */
    public void removeUselessElements(String item) {
        int indexToRemove = -1;
        this.multiplayer.add(item);
        for (int i = 0; i < scores.size(); i++) {
            Pair<String, Integer> pair = scores.get(i);
            if (pair.getKey().equals(item)) {
                indexToRemove = i;
                this.getChildren().get(i).getStyleClass().add("trike");
                break;
            }
        }
        if (indexToRemove != -1) {
            scores.remove(indexToRemove);
        }
    }

}

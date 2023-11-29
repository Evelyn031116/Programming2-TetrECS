package uk.ac.soton.comp1206.component;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Multimedia is a class used to play sounds
 */

public class Multimedia {
    private static final Logger logger = LogManager.getLogger(Multimedia.class);
    /**
     * documentPlayer is used to play music from documents
     */
    public static MediaPlayer documentPlayer;
    /**
     * bgmPlayer is used to play the background music
     */
    public static MediaPlayer bgmPlayer;

    /**
     * Play the music from given documents
     * @param documentMusic music from documents
     */
    public static void playDocumentMusic(String documentMusic){
        String document = Multimedia.class.getResource("/sounds/" + documentMusic).toExternalForm();
        try{
            Media media = new Media(document);
            documentPlayer = new MediaPlayer(media);
            documentPlayer.play();
            logger.info("Played document music " + documentMusic);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Play background music
     * @param bgmMusic background music
     */
    public static void playBgmMusic(String bgmMusic){
        String bgm = Multimedia.class.getResource("/music/" + bgmMusic).toExternalForm();
        try{
            Media media = new Media(bgm);
            bgmPlayer = new MediaPlayer(media);
            // set the mode of playing to make the music be played automatically and indefinitely
            bgmPlayer.setAutoPlay(true);
            bgmPlayer.setCycleCount(-1);
            bgmPlayer.play();
            logger.info("Played bgm " + bgmMusic);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * stop the bgm
     */
    public static void stopBgmMusic() {
        bgmPlayer.stop();
        logger.info("Stopped bgm");
    }
}

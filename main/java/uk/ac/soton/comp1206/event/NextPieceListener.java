package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The ChallengeScene (UI) will register a NextPieceListener on the Game (model), so that when a new piece is provided by the game, it can update the PieceBoard with that piece (so the ChallengeScene acts as the bridge between the model - game - and the pieceboard component
 */
public interface NextPieceListener {
    /**
     * listener for next piece
     * @param piece current piece
     * @param followingPiece next piece
     */
    void nextPiece(GamePiece piece, GamePiece followingPiece);
}

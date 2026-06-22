package hangman.logic;

// Purpose: Defines a contract for classes that provide words for the Hangman game.
public interface WordProvider {

    // Returns a word based on the selected difficulty level.
    String getWord(String difficulty);
}

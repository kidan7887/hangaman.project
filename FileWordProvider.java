package hangman.logic;

import java.io.*;
import java.sql.*;
import java.util.*;

// Purpose: This class combines file-based words and database-stored custom words.
// It allows each player to access shared team words and their own saved words.
public class HybridWordProvider implements WordProvider {

    // Stores the current player's username.
    private final String username;

    // Represents the shared words.txt file.
    private final File wordsFile;

    // Used to randomly select a word from the available list.
    private final Random random = new Random();

    // SQLite database connection URL.
    private final String url = "jdbc:sqlite:hangman_words.db";

    // Constructor: receives the active player's username and prepares resources.
    public HybridWordProvider(String username) {
        this.username = username;
        this.wordsFile = new File("words.txt");

        loadDriver();
        createWordsFileIfMissing();
        createCustomWordsTable();
    }

    // Loads the SQLite JDBC driver so Java can communicate with the database.
    private void loadDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC Driver not found: " + e.getMessage());
        }
    }

    // Creates and returns a database connection.
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    // Creates words.txt with starter words if the file does not already exist.
    private void createWordsFileIfMissing() {
        if (!wordsFile.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(wordsFile))) {

                writer.println("VARIABLE:EASY");
                writer.println("CLASS:EASY");
                writer.println("METHOD:MEDIUM");
                writer.println("ENCAPSULATION:HARD");
                writer.println("POLYMORPHISM:HARD");

            } catch (IOException e) {
                System.out.println("Error creating words.txt: " + e.getMessage());
            }
        }
    }

    // Creates the custom_words database table if it does not already exist.
    private void createCustomWordsTable() {

        String sql = """
                CREATE TABLE IF NOT EXISTS custom_words (
                    word TEXT,
                    difficulty TEXT,
                    username TEXT
                )
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

        } catch (SQLException e) {
            System.out.println("Error creating custom_words table: " + e.getMessage());
        }
    }

    // Retrieves a random word that matches the requested difficulty level.
    // The word may come from the shared file, the user's database entries, or both.
    @Override
    public String getWord(String difficulty) {

        List<String> words = new ArrayList<>();

        loadWordsFromFile(words, difficulty);
        loadWordsFromDatabase(words, difficulty);

        if (words.isEmpty()) {
            return "DEFAULT";
        }

        return words.get(random.nextInt(words.size()));
    }

    // Reads words from words.txt and adds matching difficulty words to the list.
    private void loadWordsFromFile(List<String> words, String difficulty) {

        try (BufferedReader reader = new BufferedReader(new FileReader(wordsFile))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split(":");

                if (parts.length == 2) {

                    String word = parts[0].trim();
                    String wordDifficulty = parts[1].trim();

                    if (wordDifficulty.equalsIgnoreCase(difficulty)) {
                        words.add(word);
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading words.txt: " + e.getMessage());
        }
    }

    // Retrieves custom words belonging to the current player from the database.
    private void loadWordsFromDatabase(List<String> words, String difficulty) {

        String sql = """
                SELECT word FROM custom_words
                WHERE difficulty = ? AND username = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, difficulty);
            pstmt.setString(2, username);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                words.add(rs.getString("word"));
            }

        } catch (SQLException e) {
            System.out.println("Error loading custom words: " + e.getMessage());
        }
    }

    // Saves a new custom word to the database for the current player.
    // The word is stored permanently and does not modify words.txt.
    @Override
    public void addWord(String word, String difficulty) {

        String sql = """
                INSERT INTO custom_words (word, difficulty, username)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, word.toUpperCase());
            pstmt.setString(2, difficulty.toUpperCase());
            pstmt.setString(3, username);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error saving custom word: " + e.getMessage());
        }
    }
}

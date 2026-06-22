package hangman.logic;

import java.sql.*;
import java.util.*;

// Purpose: This class manages the SQLite database for Hangman game statistics.
// It saves completed games and creates a leaderboard from stored results.
public class DatabaseStatsManager {

    // Stores the SQLite database connection path.
    private final String url = "jdbc:sqlite:hangman_stats.db";

    // Constructor: loads the SQLite driver and creates the stats table when the object is made.
    public DatabaseStatsManager() {
        loadDriver();
        createTable();
    }

    // Loads the SQLite JDBC driver so Java can connect to the database.
    private void loadDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC Driver not found: " + e.getMessage());
        }
    }

    // Creates and returns a connection to the Hangman statistics database.
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    // Creates the game_stats table if it does not already exist.
    private void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS game_stats (
                    player_name TEXT,
                    played_word TEXT,
                    difficulty_level TEXT,
                    game_won BOOLEAN
                )
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

        } catch (SQLException e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }

    // Saves one completed Hangman game into the database.
    public void logGame(String playerName, String playedWord, String difficultyLevel, boolean gameWon) {
        String sql = """
                INSERT INTO game_stats 
                (player_name, played_word, difficulty_level, game_won)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, playerName);
            pstmt.setString(2, playedWord);
            pstmt.setString(3, difficultyLevel);
            pstmt.setBoolean(4, gameWon);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error saving game stats: " + e.getMessage());
        }
    }

    // Returns the leaderboard by grouping games by player and ordering by most wins.
    public ArrayList<String> getLeaderboard() {
        ArrayList<String> leaderboard = new ArrayList<>();

        String sql = """
                SELECT player_name,
                       COUNT(*) AS games_played,
                       SUM(CASE WHEN game_won = 1 THEN 1 ELSE 0 END) AS wins
                FROM game_stats
                GROUP BY player_name
                ORDER BY wins DESC
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Reads each row from the ResultSet and formats it into a readable leaderboard line.
            while (rs.next()) {
                String row = "Player: " + rs.getString("player_name")
                        + " | Games Played: " + rs.getInt("games_played")
                        + " | Wins: " + rs.getInt("wins");

                leaderboard.add(row);
            }

        } catch (SQLException e) {
            System.out.println("Error loading leaderboard: " + e.getMessage());
        }

        return leaderboard;
    }
}

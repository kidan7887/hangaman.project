package hangman.logic;

import java.sql.*;
import java.util.*;

// Purpose: Manages database operations for Hangman game statistics.
// Handles database setup, game logging, and leaderboard generation.
public class DatabaseStatsManager {

    // Database connection URL for the SQLite file.
    private final String url = "jdbc:sqlite:hangman_stats.db";

    // Initializes the database manager and prepares the database.
    public DatabaseStatsManager() {
        loadDriver();
        createTable();
    }

    // Loads the SQLite JDBC driver into memory.
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

    // Creates the game statistics table if it does not already exist.
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

    // Records a completed game in the database.
    public void logGame(String playerName, String playedWord,
                        String difficultyLevel, boolean gameWon) {

        String sql = """
                INSERT INTO game_stats
                (player_name, played_word, difficulty_level, game_won)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Assigns values to the SQL placeholders.
            pstmt.setString(1, playerName);
            pstmt.setString(2, playedWord);
            pstmt.setString(3, difficultyLevel);
            pstmt.setBoolean(4, gameWon);

            // Executes the insert operation.
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error saving game stats: " + e.getMessage());
        }
    }

    // Generates a leaderboard sorted by total wins.
    public ArrayList<String> getLeaderboard() {

        // Stores formatted leaderboard entries.
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

            // Processes each database record.
            while (rs.next()) {

                // Formats player statistics for display.
                String row = "Player: " + rs.getString("player_name")
                        + " | Games Played: " + rs.getInt("games_played")
                        + " | Wins: " + rs.getInt("wins");

                leaderboard.add(row);
            }

        } catch (SQLException e) {
            System.out.println("Error loading leaderboard: " + e.getMessage());
        }

        // Returns the completed leaderboard.
        return leaderboard;
    }
}

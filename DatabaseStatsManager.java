package hangman.logic;

import java.sql.*;
import java.util.*;

public class DatabaseStatsManager {

    private final String url = "jdbc:sqlite:hangman_stats.db";

    public DatabaseStatsManager() {
        loadDriver();
        createTable();
    }

    private void loadDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC Driver not found: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

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

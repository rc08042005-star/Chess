
package dataaccess.sql;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.GameDAO;
import model.GameData;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class SQLGameDAO implements GameDAO {

    private final Gson gson = new Gson();

    public SQLGameDAO() throws DataAccessException {
        SQLDatabase.initialize();
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        String sql = """
                INSERT INTO game
                (whiteUsername, blackUsername, gameName, game)
                VALUES (?, ?, ?, ?)
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, game.whiteUsername());
            statement.setString(2, game.blackUsername());
            statement.setString(3, game.gameName());
            statement.setString(4, gson.toJson(game.game()));

            statement.executeUpdate();

            try (var keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

            throw new DataAccessException("Failed to generate game ID");

        } catch (SQLException e) {
            throw new DataAccessException("Failed to create game", e);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT * FROM game WHERE gameID = ?";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setInt(1, gameID);

            try (var result = statement.executeQuery()) {
                if (result.next()) {
                    return new GameData(
                            result.getInt("gameID"),
                            result.getString("whiteUsername"),
                            result.getString("blackUsername"),
                            result.getString("gameName"),
                            gson.fromJson(
                                    result.getString("game"),
                                    chess.ChessGame.class)
                    );
                }
            }

            return null;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to get game", e);
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        String sql = "SELECT * FROM game";
        Collection<GameData> games = new ArrayList<>();

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql);
             var result = statement.executeQuery()) {

            while (result.next()) {
                games.add(new GameData(
                        result.getInt("gameID"),
                        result.getString("whiteUsername"),
                        result.getString("blackUsername"),
                        result.getString("gameName"),
                        gson.fromJson(
                                result.getString("game"),
                                chess.ChessGame.class)
                ));
            }

            return games;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to list games", e);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = """
                UPDATE game
                SET whiteUsername = ?,
                    blackUsername = ?,
                    gameName = ?,
                    game = ?
                WHERE gameID = ?
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setString(1, game.whiteUsername());
            statement.setString(2, game.blackUsername());
            statement.setString(3, game.gameName());
            statement.setString(4, gson.toJson(game.game()));
            statement.setInt(5, game.gameID());

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated == 0) {
                throw new DataAccessException("Game does not exist");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to update game", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM game";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear games", e);
        }
    }
}

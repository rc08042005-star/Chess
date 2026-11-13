
package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Manages a chess game, including moves, turns, check,
 * checkmate, and stalemate.
 */
public class ChessGame {

    private TeamColor teamTurn;
    private ChessBoard board;

    public ChessGame() {
        teamTurn = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
    }

    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Returns the team whose turn it is.
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Sets the team whose turn it is.
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Returns the legal moves for the piece at the given position.
     */
    public Collection<ChessMove> validMoves(
            ChessPosition startPosition) {

        ChessPiece piece = board.getPiece(startPosition);

        if (piece == null) {
            return null;
        }

        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : piece.pieceMoves(board, startPosition)) {
            if (isMoveSafe(move, piece)) {
                validMoves.add(move);
            }
        }

        return validMoves;
    }

    /**
     * Temporarily makes a move to determine whether
     * the moving team's king remains safe.
     */
    private boolean isMoveSafe(ChessMove move, ChessPiece piece) {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();

        ChessPiece capturedPiece = board.getPiece(end);

        board.addPiece(start, null);
        board.addPiece(end, piece);

        boolean safe;

        try {
            safe = !isInCheck(piece.getTeamColor());
        } finally {
            board.addPiece(start, piece);
            board.addPiece(end, capturedPiece);
        }

        return safe;
    }

    /**
     * Makes a move if it is legal and belongs to the current team.
     *
     * @throws InvalidMoveException if the move is invalid
     */
    public void makeMove(ChessMove move)
            throws InvalidMoveException {

        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();

        ChessPiece piece = board.getPiece(start);

        validateMove(move, piece);

        board.addPiece(start, null);
        board.addPiece(end, resultingPiece(move, piece));

        switchTurn();
    }

    /**
     * Validates the requested move.
     */
    private void validateMove(ChessMove move, ChessPiece piece)
            throws InvalidMoveException {

        if (piece == null || piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException();
        }

        Collection<ChessMove> moves =
                validMoves(move.getStartPosition());

        if (moves == null || !moves.contains(move)) {
            throw new InvalidMoveException();
        }
    }

    /**
     * Returns either the original piece or its promotion.
     */
    private ChessPiece resultingPiece(
            ChessMove move, ChessPiece piece) {

        ChessPiece.PieceType promotion =
                move.getPromotionPiece();

        if (promotion == null) {
            return piece;
        }

        return new ChessPiece(piece.getTeamColor(), promotion);
    }

    /**
     * Changes the active team after a legal move.
     */
    private void switchTurn() {
        teamTurn = teamTurn == TeamColor.WHITE
                ? TeamColor.BLACK
                : TeamColor.WHITE;
    }

    /**
     * Determines whether a team's king is under attack.
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKing(teamColor);

        if (kingPosition == null) {
            return false;
        }

        return isSquareAttacked(kingPosition, teamColor);
    }

    /**
     * Finds the king belonging to the specified team.
     */
    private ChessPosition findKing(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);

                if (isKingAt(position, teamColor)) {
                    return position;
                }
            }
        }

        return null;
    }

    /**
     * Checks whether the given position contains the team's king.
     */
    private boolean isKingAt(
            ChessPosition position, TeamColor teamColor) {

        ChessPiece piece = board.getPiece(position);

        return piece != null
                && piece.getTeamColor() == teamColor
                && piece.getPieceType() == ChessPiece.PieceType.KING;
    }

    /**
     * Determines whether an opposing piece attacks a square.
     */
    private boolean isSquareAttacked(
            ChessPosition target, TeamColor teamColor) {

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);

                if (enemyAttacksSquare(position, target, teamColor)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks whether an enemy piece attacks the target square.
     */
    private boolean enemyAttacksSquare(
            ChessPosition position,
            ChessPosition target,
            TeamColor teamColor) {

        ChessPiece piece = board.getPiece(position);

        if (piece == null || piece.getTeamColor() == teamColor) {
            return false;
        }

        for (ChessMove move : piece.pieceMoves(board, position)) {
            if (move.getEndPosition().equals(target)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines whether a team is in checkmate.
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && !hasLegalMoves(teamColor);
    }

    /**
     * Determines whether a team is in stalemate.
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && !hasLegalMoves(teamColor);
    }

    /**
     * Determines whether any piece belonging to the team
     * has at least one legal move.
     */
    private boolean hasLegalMoves(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);

                if (pieceHasLegalMoves(position, teamColor)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks whether a piece belongs to the team
     * and has at least one legal move.
     */
    private boolean pieceHasLegalMoves(
            ChessPosition position, TeamColor teamColor) {

        ChessPiece piece = board.getPiece(position);

        if (piece == null || piece.getTeamColor() != teamColor) {
            return false;
        }

        Collection<ChessMove> moves = validMoves(position);

        return moves != null && !moves.isEmpty();
    }

    /**
     * Sets the game's chessboard.
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Returns the game's chessboard.
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ChessGame other)) {
            return false;
        }

        return teamTurn == other.teamTurn
                && Objects.equals(board, other.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }
}

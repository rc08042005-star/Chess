package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {

        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }


    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    @Override
    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }
        if (!(obj instanceof ChessPiece other)){
            return false;
        }
        return pieceColor==other.pieceColor && type==other.type;
    }
    @Override
    public int hashCode(){
        return Objects.hash(pieceColor, type);
    }




    public Collection<ChessMove> pieceMoves(
            ChessBoard board,
            ChessPosition myPosition
    ) {
        Collection<ChessMove> moves = new ArrayList<>();

        switch (type) {
            case ROOK -> {
                int[][] directions = {
                        {1, 0}, {-1, 0}, {0, 1}, {0, -1}
                };
                addSlidingMoves(board, myPosition, moves, directions);
            }

            case BISHOP -> {
                int[][] directions = {
                        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
                };
                addSlidingMoves(board, myPosition, moves, directions);
            }

            case QUEEN -> {
                int[][] directions = {
                        {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
                };
                addSlidingMoves(board, myPosition, moves, directions);
            }

            case KNIGHT -> {
                int[][] jumps = {
                        {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                        {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
                };
                addSingleStepMoves(board, myPosition, moves, jumps);
            }

            case KING -> {
                int[][] directions = {
                        {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
                };
                addSingleStepMoves(board, myPosition, moves, directions);
            }

            case PAWN -> addPawnMoves(board, myPosition, moves);
        }

        return moves;
    }
    private void addSlidingMoves(
            ChessBoard board,
            ChessPosition start,
            Collection<ChessMove> moves,
            int[][] directions
    ) {
        for (int[] direction : directions) {
            int row = start.getRow() + direction[0];
            int col = start.getColumn() + direction[1];

            while (isOnBoard(row, col)) {
                ChessPosition destination = new ChessPosition(row, col);
                ChessPiece occupyingPiece = board.getPiece(destination);

                if (occupyingPiece == null) {
                    moves.add(new ChessMove(start, destination, null));
                } else {
                    if (occupyingPiece.getTeamColor() != pieceColor) {
                        moves.add(new ChessMove(start, destination, null));
                    }

                    // Cannot move through another piece.
                    break;
                }

                row += direction[0];
                col += direction[1];
            }
        }
    }
    }

}

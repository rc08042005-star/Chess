
package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

public class ChessBoardRenderer {

    private static final String RESET = "\u001B[0m";
    private static final String LIGHT_SQUARE = "\u001B[47m";
    private static final String DARK_SQUARE = "\u001B[100m";
    private static final String WHITE_PIECE = "\u001B[97m";
    private static final String BLACK_PIECE = "\u001B[30m";

    public void drawBoard(ChessBoard board,
                          ChessGame.TeamColor perspective) {

        boolean whitePerspective =
                perspective == ChessGame.TeamColor.WHITE;

        int rowStart = whitePerspective ? 8 : 1;
        int rowEnd = whitePerspective ? 1 : 8;
        int rowStep = whitePerspective ? -1 : 1;

        int colStart = whitePerspective ? 1 : 8;
        int colEnd = whitePerspective ? 8 : 1;
        int colStep = whitePerspective ? 1 : -1;

        System.out.println();
        printColumnLabels(colStart, colEnd, colStep);

        for (int row = rowStart;
             whitePerspective ? row >= rowEnd : row <= rowEnd;
             row += rowStep) {

            System.out.print(" " + row + " ");

            for (int col = colStart;
                 whitePerspective ? col <= colEnd : col >= colEnd;
                 col += colStep) {

                ChessPosition position =
                        new ChessPosition(row, col);

                ChessPiece piece = board.getPiece(position);

                boolean lightSquare = (row + col) % 2 != 0;

                String background = lightSquare
                        ? LIGHT_SQUARE
                        : DARK_SQUARE;

                String foreground = piece != null
                        && piece.getTeamColor()
                        == ChessGame.TeamColor.WHITE
                        ? WHITE_PIECE
                        : BLACK_PIECE;

                System.out.print(
                        background + foreground
                                + " " + pieceSymbol(piece) + " "
                                + RESET);
            }

            System.out.println(" " + row);
        }

        printColumnLabels(colStart, colEnd, colStep);
        System.out.println();
    }

    private void printColumnLabels(int start,
                                   int end,
                                   int step) {

        System.out.print("   ");

        for (int col = start;
             step > 0 ? col <= end : col >= end;
             col += step) {

            char label = (char) ('a' + col - 1);
            System.out.print(" " + label + " ");
        }

        System.out.println();
    }

    private String pieceSymbol(ChessPiece piece) {

        if (piece == null) {
            return " ";
        }

        boolean white =
                piece.getTeamColor()
                        == ChessGame.TeamColor.WHITE;

        return switch (piece.getPieceType()) {
            case KING -> white ? "♔" : "♚";
            case QUEEN -> white ? "♕" : "♛";
            case ROOK -> white ? "♖" : "♜";
            case BISHOP -> white ? "♗" : "♝";
            case KNIGHT -> white ? "♘" : "♞";
            case PAWN -> white ? "♙" : "♟";
        };
    }
}


package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

public class ChessBoardRenderer {

    public void drawBoard(ChessBoard board, ChessGame.TeamColor perspective) {
        boolean whitePerspective =
                perspective == ChessGame.TeamColor.WHITE;

        int firstRow = whitePerspective ? 8 : 1;
        int lastRow = whitePerspective ? 1 : 8;
        int rowStep = whitePerspective ? -1 : 1;

        int firstColumn = whitePerspective ? 1 : 8;
        int lastColumn = whitePerspective ? 8 : 1;
        int columnStep = whitePerspective ? 1 : -1;

        System.out.println();

        printColumnLabels(firstColumn, lastColumn, columnStep);

        for (int row = firstRow;
             whitePerspective ? row >= lastRow : row <= lastRow;
             row += rowStep) {

            System.out.print(" " + row + " ");

            for (int column = firstColumn;
                 whitePerspective
                         ? column <= lastColumn
                         : column >= lastColumn;
                 column += columnStep) {

                ChessPosition position =
                        new ChessPosition(row, column);

                ChessPiece piece = board.getPiece(position);

                System.out.print(" " + pieceSymbol(piece) + " ");
            }

            System.out.println(" " + row);
        }

        printColumnLabels(firstColumn, lastColumn, columnStep);
        System.out.println();
    }

    private void printColumnLabels(
            int firstColumn,
            int lastColumn,
            int columnStep) {

        System.out.print("   ");

        for (int column = firstColumn;
             columnStep > 0
                     ? column <= lastColumn
                     : column >= lastColumn;
             column += columnStep) {

            char label = (char) ('a' + column - 1);
            System.out.print(" " + label + " ");
        }

        System.out.println();
    }

    private String pieceSymbol(ChessPiece piece) {
        if (piece == null) {
            return ".";
        }

        boolean white =
                piece.getTeamColor() == ChessGame.TeamColor.WHITE;

        return switch (piece.getPieceType()) {
            case KING -> white ? "K" : "k";
            case QUEEN -> white ? "Q" : "q";
            case ROOK -> white ? "R" : "r";
            case BISHOP -> white ? "B" : "b";
            case KNIGHT -> white ? "N" : "n";
            case PAWN -> white ? "P" : "p";
        };
    }
}

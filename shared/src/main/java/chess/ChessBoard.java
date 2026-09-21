package chess;
import java.util.Arrays;
/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private final ChessPiece[][] squares= new ChessPiece[8][8];

    public ChessBoard() {
        
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {

        squares[position.getRow()-1][position.getColumn()-1]=piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {

        return squares[position.getRow()-1][position.getColumn()-1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
// clear pieces
        for(int row=0;row<8;row++){
            for(int col=0;col<8;col++){
                squares[row][col]=null;
            }
        }
        // pawn placement
        for (int col=1; col<=8;col++){
            addPiece(new ChessPosition(2,col),
            new ChessPiece(ChessGame.TeamColor.WHITE,ChessPiece.PieceType.PAWN));

            addPiece(new ChessPosition(7,col),
                    new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }
        //placing the other piece in their starting position.
        ChessPiece.PieceType[] backRow={
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.ROOK
        };
        for (int col=1; col<=8;col++){
            ChessPiece.PieceType type =backRow[col-1];

            addPiece(new ChessPosition(1,col),new ChessPiece(ChessGame.TeamColor.WHITE, type));
            addPiece(new ChessPosition(8,col), new ChessPiece(ChessGame.TeamColor.BLACK, type));

        }
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ChessBoard other)) {
            return false;
        }

        return Arrays.deepEquals(squares, other.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }
}

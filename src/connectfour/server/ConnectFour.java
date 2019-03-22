package connectfour.server;

import java.util.Set;
import connectfour.ConnectFourException;

/**
 * A basic implementation of the Connect Four game.
 */
public class ConnectFour {
    /** the number of rows */
    public final static int ROWS = 6;
    /** the number of columns */
    public final static int COLS = 7;
    /** how big a line one needs to win */
    public final static int WIN_LEN = 4;

    /**
     * Used to indicate a move that has been made on the board.
     */
    public enum Move {
        PLAYER_ONE('X'),
        PLAYER_TWO('O'),
        NONE('.');

        private char symbol;

        private Move(char symbol) {
            this.symbol = symbol;
        }

        public char getSymbol() {
            return symbol;
        }
    }

    /**
     * The number of rows in the board.
     */
    private int rows;

    /**
     * The number of columns in the board.
     */
    private int cols;

    /**
     * The board.
     */
    private Move[][] board;

    /**
     * Used to keep track of which player's turn it is; 0 for player 1, and 1
     * for player 2.
     */
    private int turn;

    /**
     *  The last column a piece was placed.  Used for win checking.
     */
    private int lastCol;

    /**
     * The row the last piece was placed.  Used for win checking.
     */
    private int lastRow;

    /**
     * Creates a Connect Four game using a board with the standard number of
     * rows (6) and columns (7).
     */
    public ConnectFour() {
        this(ROWS, COLS);
    }

    /**
     * Creates a Connect Four game using a board with the specified number of
     * rows and columns. Assumes that player 1 is the first to move.
     *
     * @param rows The number of rows in the board.
     * @param cols The number of columns in the board.
     */
    public ConnectFour(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;

        board = new Move[cols][rows];
        for(int col=0; col<cols; col++) {
            for(int row=0; row < rows; row++) {
                board[col][row] = Move.NONE;
            }
        }

        turn = 0;
    }

    /**
     * Makes a move for the player whose turn it is. If the move is successful,
     * play automatically switches to the other player's turn.
     *
     * @param column The column in which the player is moving.
     *
     * @throws ConnectFourException If the move is invalid for any reason.
     */
    public void makeMove(int column) throws ConnectFourException {
        Move move = turn == 0 ? Move.PLAYER_ONE : Move.PLAYER_TWO;

        if(column < 0 || column >= cols) {
            throw new ConnectFourException("Invalid column");
        }
        else if(board[column][0] != Move.NONE) {
            throw new ConnectFourException("Column full!");
        }
        else {
            int dropTo = 0;
            for(int r=1; r<rows && board[column][r] == Move.NONE; r++) {
                dropTo = r;
            }
            board[column][dropTo] = move;

            turn = turn ^ 1;
            lastCol = column;
            lastRow = dropTo;
        }
    }

    private class C_R {
        public final int c, r;
        public C_R( int c, int r ) { this.c = c; this.r = r; }
        public C_R advance( int scale, C_R dir ) {
            return new C_R( this.c + scale * dir.c, this.r + scale * dir.r );
        }
        public boolean inBounds() {
            return
                this.c >= 0 && this.c < COLS && this.r >= 0 && this.r < ROWS;
        }
        public Move contents() {
            return ConnectFour.this.board[ this.c ][ this.r ];
        }
    }

    private Set< C_R > DIRS = Set.of(
            new C_R( -1, -1), new C_R( -1, 0), new C_R( -1, 1), new C_R( 0, -1),
            new C_R( 0, 1), new C_R( 1, -1), new C_R( 1, 0), new C_R( 1, 1)
    );

    public boolean hasWonGame() {
        for ( int c = 0; c < COLS; ++c ) {
            for ( int r = 0; r < ROWS; ++r ) {
                C_R start = new C_R( c, r );
                Move here = start.contents();
                if ( here == Move.NONE ) continue; // NONE can't win :-)
                for ( C_R dir: DIRS ) {
                    C_R end = start.advance( WIN_LEN-1, dir );
                    if ( end.inBounds() ) {
                        boolean goodOne = true;
                        for ( int delta = 1; delta < WIN_LEN; ++delta ) {
                            if ( start.advance( delta, dir ).contents() != here ) {
                                goodOne = false;
                                break;
                            }
                        }
                        if ( goodOne )return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the game is currently in a winning state. Can be used to
     * determine if the most recent move won the game (and therefore the player
     * that made the move has won).
     *
     * @return True if the game is in a winning state. False otherwise.
     */
    public boolean hasWonGame_old() {
        Move player = board[lastCol][lastRow];

          // check left horizontal
        if (this.lastCol >= 3) {
            int count = 1;
            for (int col=this.lastCol-1; col>this.lastCol-4; --col) {
                if (board[col][lastRow] == player) {
                    ++count;
                }
            }
            if (count == 4) {
                return true;
            }
        }

        // check right horizontal
        if (this.lastCol <= 3) {
            int count = 1;
            for (int col=this.lastCol+1; col>this.lastCol+4; ++col) {
                if (board[col][lastRow] == player) {
                    ++count;
                }
            }
            if (count == 4) {
                return true;
            }
        }

        // check vertically down
        if (this.lastRow <=2) {
            int count = 1;
            for (int row=this.lastRow+1; row<this.lastRow+4; ++row) {
                if (board[this.lastCol][row] == player) {
                    ++count;
                }
            }
            if (count == 4) {
                return true;
            }
        }

        // check diagonally to left
        if (this.lastRow <= 2 && this.lastCol >= 3) {
            int count = 1;
            int col = this.lastCol - 1;
            for (int row = this.lastRow + 1; row < this.lastRow + 4; ++row) {
                if (board[col][row] == player) {
                    --col;
                    ++count;
                }
            }
            if (count == 4) {
                return true;
            }
        }

        // check diagonally to right
        if (this.lastRow <= 2 && this.lastCol <= 3) {
            int count = 1;
            int col = this.lastCol + 1;
            for (int row = this.lastRow + 1; row < this.lastRow + 4; ++row) {
                if (board[col][row] == player) {
                    ++col;
                    ++count;
                }
            }
            if (count == 4) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to see if the game is tied - no NONE moves left in board.  This
     * is called after hasGameWon.
     *
     * @return whether game is tied or not
     */
    public boolean hasTiedGame() {
        for (int row=0; row<rows; ++row) {
            for (int col=0; col<cols; ++col) {
                if (board[col][row] == Move.NONE) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns a {@link String} representation of the board, suitable for
     * printing.
     *
     * @return A {@link String} representation of the board.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for(int r=0; r<rows; r++) {
            for(int c=0; c<cols; c++) {
                builder.append('[');
                builder.append(board[c][r].getSymbol());
                builder.append(']');
            }
            builder.append('\n');
        }
        return builder.toString();
    }
}

package connectfour.client;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

/**
 * The model for the connect four game.
 *
 * @author James Heloitis @ RIT CS
 * @author Sean Strout @ RIT CS
 */
public class ConnectFourBoard {
    /** the number of rows */
    public final static int ROWS = 6;
    /** the number of columns */
    public final static int COLS = 7;

    /**
     * Used to indicate a move that has been made on the board,
     * and to keep track of whose turn it is
     */
    public enum Move {
        PLAYER_ONE, PLAYER_TWO, NONE;

        public Move opponent() {
            return this == PLAYER_ONE ?
                    PLAYER_TWO :
                    this == PLAYER_TWO ?
                            PLAYER_ONE :
                            this;
        }
    }

    /** Possible statuses of game */
    public enum Status {
        NOT_OVER, I_WON, I_LOST, TIE, ERROR;

        private String message = null;

        public void setMessage( String msg ) {
            this.message = msg;
        }

        @Override
        public String toString() {
            return super.toString() +
                    this.message == null ? "" : ( '(' + this.message + ')' );
        }
    }

    /** How many moves are left to make before end of game */
    private int movesLeft;

    /**
     * If this "copy" of the server's board is the one that can be updated,
     * i.e., if it is "this" player's turn. This is determined by the server.
     * It has a {@link connectfour.ConnectFourProtocol#MAKE_MOVE} message that sets
     * this variable true. The act of the player choosing a move sets the
     * variable false again.
     */
    private boolean myTurn;

    /** this value flips back and forth as discs are added to the board */
    private Move currentPiece;

    /** current game status */
    private Status status;

    /** the board */
    private Move[][] board;

    /** the observers of this model */
    private List<Observer<ConnectFourBoard>> observers;

    /** 
     * The view calls this method to add themselves as an observer of the model.
     * 
     * @param observer the observer
     */
    public void addObserver(Observer<ConnectFourBoard> observer) {
        this.observers.add(observer);
    }

    /** when the model changes, the observers are notified via their update() method */
    private void alertObservers() {
        for (Observer<ConnectFourBoard> obs: this.observers ) {
            obs.update(this);
        }
    }

    public ConnectFourBoard() {
        this.observers = new LinkedList<>();

        this.board = new Move[COLS][ROWS];
        for(int col=0; col<COLS; col++) {
            for(int row=0; row < ROWS; row++) {
                board[col][row] = Move.NONE;
            }
        }

        this.movesLeft = COLS*ROWS;
        this.status = Status.NOT_OVER;
        // it's never my turn unless the server tells me to make a move.
        this.myTurn = false;
        // whether it's me or the other player, Player #1 always goes first.
        this.currentPiece = Move.PLAYER_ONE;    }

    public void error(String arguments) {
        this.status = Status.ERROR;
        this.status.setMessage(arguments);
        alertObservers();
    }

    /**
     * Information for the UI
     * @return the number of additional moves until the board is full.
     */
    public int getMovesLeft() {
        return this.movesLeft;
    }

    /**
     * Can the local user make changes to the board?
     * @return true if the server has told this player it is its time to move
     */
    public boolean isMyTurn() {
        return this.myTurn;
    }

    /**
     * The user has chosen a move.
     */
    public void didMyTurn() {
        this.myTurn = false;
    }

    /**
     * Get game status.
     * @return the Status object for the game
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * What is at this square?
     * @param row row number of square
     * @param col column number of square
     * @return the player (or {@link Move#NONE}) at the given location
     */
    public Move getContents(int row, int col) {
        return this.board[row][col];
    }

    /**
     * Will this move be accepted as valid by the server?
     * This method is added so that a bad move is caught before it is sent
     * to the server, and the server quits.
     *
     * @param col the column
     * @return true iff the column is not full
     */
    public boolean isValidMove(int col) {
        return (col >= 0 && col < COLS) &&
                (this.board[col][0] == Move.NONE);
    }

    /**
     * Called when the server notifies us to make a move.
     */
    public void makeMove() {
        this.myTurn = true;
        alertObservers();
    }

    /**
     * The UI calls this to announce the player's choice of a move.
     * @param col the column
     */
    public void moveMade(int col) {
        // gets called as a result of the message from the server.
        // place piece on board
        this.movesLeft -= 1;

        // find first open row from bottom up
        for (int row=ROWS-1; row >= 0; --row) {
            if (board[col][row] == Move.NONE) {
                this.board[col][row] = this.currentPiece;
                break;
            }
        }

        this.currentPiece = this.currentPiece.opponent();
        this.myTurn = false;
        alertObservers();
    }

    /**
     * Called when the game has been won by this player.
     */
    public void gameWon() {
        this.status = Status.I_WON;
        alertObservers();
    }

    /**
     * Called when the game has been won by the other player.
     */
    public void gameLost() {
        this.status = Status.I_LOST;
        alertObservers();
    }

    /**
     * Called when the game has been tied.
     */
    public void gameTied() {
        this.status = Status.TIE;
        alertObservers();
    }

    /**
     * The user they may close at any time
     */
    public void close() {
        alertObservers();
    }

    private static EnumMap<Move, Character> cmap = new EnumMap<>(Move.class);

    static {
        cmap = new EnumMap<>( Move.class );
        cmap.put( Move.PLAYER_ONE, 'O' );
        cmap.put( Move.PLAYER_TWO, 'X' );
        cmap.put( Move.NONE,       '.' );
    }

    /**
     * Returns a string representation of the board, suitable for printing out.
     * The starting board would be:<br>
     * <br><tt>
     * 0  1  2  3 4 5 6<br>
     * 0[.][.][.][.][.][.][.]<br>
     * 1[.][.][.][.][.][.][.]<br>
     * 2[.][.][.][.][.][.][.]<br>
     * 3[.][.][.][.][.][.][.]<br>
     * 4[.][.][.][.][.][.][.]<br>
     * 5[.][.][.][.][.][.][.]<br>
     * </tt>
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        // build the top row with column numbers
        builder.append(' ');
        for (int c = 0; c < COLS; ++c) {
            builder.append(" " + c + ' ');
        }
        builder.append('\n');

        // build remaining rows with row numbers and column values
        for ( int r = 0; r < ROWS; ++r) {
            builder.append( r );
            for ( int c = 0; c < COLS; ++c) {
                builder.append('[');
                builder.append( cmap.get(this.board[c][r]));
                builder.append(']');
            }
            builder.append('\n');
        }

        return builder.toString();
    }
}

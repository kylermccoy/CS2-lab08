package connectfour.server;

import connectfour.ConnectFourException;

/**
 * Connect four game.
 */
public class ConnectFourGame implements Runnable {
    /** first player */
    private ConnectFourPlayer playerOne;
    /** second player */
    private ConnectFourPlayer playerTwo;

    /** the game model */
    private ConnectFour game;

    /**
     * Initialize the game.
     *
     * @param playerOne first player
     * @param playerTwo second player
     */
    public ConnectFourGame(ConnectFourPlayer playerOne,
                           ConnectFourPlayer playerTwo) {

        this.playerOne = playerOne;
        this.playerTwo = playerTwo;

        game = new ConnectFour();
    }

    @Override
    public void run() {
        boolean go = true;
        while(go) {
            try {
                if(makeMove(playerOne, playerTwo)) {
                    go = false;
                }
                else if(makeMove(playerTwo, playerOne)) {
                    go = false;
                }
            }
            catch(ConnectFourException e) {
                playerOne.error(e.getMessage());
                playerTwo.error(e.getMessage());
                go = false;
            }
        }

        playerOne.close();
        playerTwo.close();
    }

    /**
     * Make a move in the game.
     *
     * @param turn this player's turn
     * @param other the other player
     * @return whether the game was won or not
     * @throws ConnectFourException
     */
    private boolean makeMove(ConnectFourPlayer turn, ConnectFourPlayer other)
        throws ConnectFourException {

        int column = turn.makeMove();
        game.makeMove(column);

        turn.moveMade(column);
        other.moveMade(column);

        if (game.hasWonGame()) {
            turn.gameWon();
            other.gameLost();
            return true;
        } else if (game.hasTiedGame()) {
            turn.gameTied();
            other.gameTied();
            return true;
        }
        else {
            return false;
        }
    }
}

package connectfour.client;

import connectfour.ConnectFourException;
import connectfour.ConnectFourProtocol;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static connectfour.ConnectFourProtocol.*;

/**
 * The client side network interface to a ConnectFour game server.
 * Each of the two players in a game gets its own connection to the server.
 * This class represents the controller part of a model-view-controller
 * triumvirate, in that part of its purpose is to forward user actions
 * to the remote server.
 *
 * @author James Heloitis @ RIT CS
 * @author Sean Strout @ RIT CS
 */
public class ConnectFourNetworkClient {
    /** Turn on if standard output debug messages are desired. */
     private static final boolean DEBUG = false;

    /**
     * Print method that does something only if DEBUG is true
     *
     * @param logMsg the message to log
     */
    private static void dPrint( Object logMsg ) {
        if ( ConnectFourNetworkClient.DEBUG ) {
            System.out.println( logMsg );
        }
    }

    /** client socket to communicate with server */
    private Socket clientSocket;
    /** used to read requests from the server */
    private Scanner networkIn;
    /** Used to write responses to the server. */
    private PrintStream networkOut;
    /** the model which keeps track of the game */
    private ConnectFourBoard board;
    /** sentinel loop used to control the main loop */
    private boolean go;

    /**
     * Accessor that takes multithreaded access into account
     *
     * @return whether it ok to continue or not
     */
    private synchronized boolean goodToGo() {
        return this.go;
    }

    /**
     * Multithread-safe mutator
     */
    private synchronized void stop() {
        this.go = false;
    }

    /**
     * Called when the server sends a message saying that
     * gameplay is damaged. Ends the game.
     *
     * @param arguments The error message sent from the reversi.server.
     */
    public void error( String arguments ) {
        ConnectFourNetworkClient.dPrint( '!' + ERROR + ',' + arguments );
        dPrint( "Fatal error: " + arguments );
        this.board.error( arguments );
        this.stop();
    }

    /**
     * Hook up with a ConnectFour game server already running and waiting for
     * two players to connect. Because of the nature of the server
     * protocol, this constructor actually blocks waiting for the first
     * message (connect) from the server.  Afterwards a thread that listens for
     * server messages and forwards them to the game object is started.
     *
     * @param host  the name of the host running the server program
     * @param port  the port of the server socket on which the server is listening
     * @param board the local object holding the state of the game that
     *              must be updated upon receiving server messages
     * @throws ConnectFourException If there is a problem opening the connection
     */
    public ConnectFourNetworkClient(String host, int port, ConnectFourBoard board)
            throws ConnectFourException {
        try {
            this.clientSocket = new Socket(host, port);
            this.networkIn = new Scanner(clientSocket.getInputStream());
            this.networkOut = new PrintStream(clientSocket.getOutputStream());
            this.board = board;
            this.go = true;

            // Block waiting for the CONNECT message from the server.
            String request = this.networkIn.next();
            String arguments = this.networkIn.nextLine();
            if (!request.equals(ConnectFourProtocol.CONNECT )) {
                throw new ConnectFourException("Expected CONNECT from server");
            }
            ConnectFourNetworkClient.dPrint("Connected to server " + this.clientSocket);
        }
        catch(IOException e) {
            throw new ConnectFourException(e);
        }
    }

    /**
     * Called from the GUI when it is ready to start receiving messages
     * from the server.
     */
    public void startListener() {
        new Thread(() -> this.run()).start();
    }

    /**
     * Tell the local user to choose a move. How this is communicated to
     * the user is up to the View (UI).
     */
    private void makeMove() {
        this.board.makeMove();
    }

    /**
     * A move has been made by one of the players
     *
     * @param arguments string from the server's message that
     *                  contains the row, then column where the
     *                  player made the move
     */
    public void moveMade( String arguments ) {
        ConnectFourNetworkClient.dPrint( '!' + MOVE_MADE + ',' + arguments );

        String[] fields = arguments.trim().split( " " );
        int column = Integer.parseInt(fields[0]);

        // Update the board model.
        this.board.moveMade(column);
    }

    /**
     * Called when the server sends a message saying that the
     * board has been won by this player. Ends the game.
     */
    public void gameWon() {
        ConnectFourNetworkClient.dPrint( '!' + GAME_WON );

        dPrint( "You won! Yay!" );
        this.board.gameWon();
        this.stop();
    }

    /**
     * Called when the server sends a message saying that the
     * game has been won by the other player. Ends the game.
     */
    public void gameLost() {
        ConnectFourNetworkClient.dPrint( '!' + GAME_LOST );
        dPrint( "You lost! Boo!" );
        this.board.gameLost();
        this.stop();
    }

    /**
     * Called when the server sends a message saying that the
     * game is a tie. Ends the game.
     */
    public void gameTied() {
        ConnectFourNetworkClient.dPrint( '!' + GAME_TIED );
        dPrint( "You tied! Meh!" );
        this.board.gameTied();
        this.stop();
    }

    /**
     * This method should be called at the end of the game to
     * close the client connection.
     */
    public void close() {
        try {
            this.clientSocket.close();
        }
        catch( IOException ioe ) {
            // squash
        }
        this.board.close();
    }

    /**
     * UI wants to send a new move to the server.
     *
     * @param col the column
     */
    public void sendMove(int col) {
        this.networkOut.println( MOVE + " " + col );
    }


    /**
     * Run the main client loop. Intended to be started as a separate
     * thread internally. This method is made private so that no one
     * outside will call it or try to start a thread on it.
     */
    private void run() {
        while (this.goodToGo()) {
            try {
                String request = this.networkIn.next();
                String arguments = this.networkIn.nextLine().trim();
                ConnectFourNetworkClient.dPrint( "Net message in = \"" + request + '"' );

                switch ( request ) {
                    case MAKE_MOVE:
                        makeMove();
                        break;
                    case MOVE_MADE:
                        moveMade( arguments );
                        break;
                    case GAME_WON:
                        gameWon();
                        break;
                    case GAME_LOST:
                        gameLost();
                        break;
                    case GAME_TIED:
                        gameTied();
                        break;
                    case ERROR:
                        error( arguments );
                        break;
                    default:
                        System.err.println("Unrecognized request: " + request);
                        this.stop();
                        break;
                }
            }
            catch( NoSuchElementException nse ) {
                // Looks like the connection shut down.
                this.error( "Lost connection to server." );
                this.stop();
            }
            catch( Exception e ) {
                this.error( e.getMessage() + '?' );
                this.stop();
            }
        }
        this.close();
    }

}

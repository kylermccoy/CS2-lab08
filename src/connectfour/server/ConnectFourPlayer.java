package connectfour.server;

import connectfour.ConnectFourException;
import connectfour.ConnectFourProtocol;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * A class that manages the requests and responses to a single client.
 */
public class ConnectFourPlayer implements ConnectFourProtocol, Closeable {
    /**
     * The {@link Socket} used to communicate with the client.
     */
    private Socket sock;

    /**
     * The {@link Scanner} used to read responses from the client.
     */
    private Scanner scanner;

    /**
     * The {@link PrintStream} used to send requests to the client.
     */
    private PrintStream printer;

    /**
     * Creates a new {@link ConnectFourPlayer} that will use the specified
     * {@link Socket} to communicate with the client.
     *
     * @param sock The {@link Socket} used to communicate with the client.
     *
     * @throws ConnectFourException If there is a problem establishing
     * communication with the client.
     */
    public ConnectFourPlayer(Socket sock) throws ConnectFourException {
        this.sock = sock;
        try {
            scanner = new Scanner(sock.getInputStream());
            printer = new PrintStream(sock.getOutputStream());
        }
        catch (IOException e) {
            throw new ConnectFourException(e);
        }
    }

    /**
     * Sends the initial {@link #CONNECT} request to the client.
     */
    public void connect() {
        printer.println(CONNECT);
    }

    /**
     * Sends a {@link #MAKE_MOVE} request to the client and returns the column
     * in which the client would like to move.
     *
     * @return The column in which the client would like to move.
     *
     * @throws ConnectFourException If the client's response is invalid, i.e.
     * not {@link #MOVE} and a column number.
     */
    public int makeMove() throws ConnectFourException {
        printer.println(MAKE_MOVE);
        String response = scanner.nextLine();

        if(response.startsWith(MOVE)) {
            String[] tokens = response.split(" ");
            if(tokens.length == 2) {
                return Integer.parseInt(tokens[1]);
            }
            else {
                throw new ConnectFourException("Invalid player response: " +
                        response);
            }
        }
        else {
            throw new ConnectFourException("Invalid player response: " +
                    response);
        }
    }

    /**
     * Sends a {@link #MOVE_MADE} request to the client to inform the client
     * that a move has been made on the board.
     *
     * @param column The column in which the move has been made.
     *
     */
    public void moveMade(int column) {
        printer.println(MOVE_MADE + " " + column);
    }

    /**
     * Called to send a {@link #GAME_WON} request to the client because the
     * player's most recent move won the game.
     *
     */
    public void gameWon() {
        printer.println(GAME_WON);

    }

    /**
     * Called to send a {@link #GAME_LOST} request to the client because the
     * other player's most recent move won the game.
     *
     */
    public void gameLost()  {
        printer.println(GAME_LOST);
    }

    /**
     * Called to send a {@link #GAME_TIED} request to the client because the
     * game tied.
     */
    public void gameTied()  {
        printer.println(GAME_TIED);
    }

    /**
     * Called to send an {@link #ERROR} to the client. This is called if either
     * client has invalidated themselves with a bad response.
     *
     * @param message The error message.
     */
    public void error(String message) {
        printer.println(ERROR + " " + message);
    }

    /**
     * Called to close the client connection after the game is over.
     */
    @Override
    public void close() {
        try {
            sock.close();
        }
        catch(IOException ioe) {
            // squash
        }
    }
}

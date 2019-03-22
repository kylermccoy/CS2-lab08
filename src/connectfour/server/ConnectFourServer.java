package connectfour.server;

import connectfour.ConnectFourException;
import connectfour.ConnectFourProtocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The {@link ConnectFourServer} waits for incoming client connections and
 * pairs them off to play {@link ConnectFourGame games}.
 */
public class ConnectFourServer implements ConnectFourProtocol, Runnable {
    /**
     * The {@link ServerSocket} used to wait for incoming client connections.
     */
    private ServerSocket server;

    /**
     * Creates a new {@link ConnectFourServer} that listens for incoming
     * connections on the specified port.
     *
     * @param port The port on which the server should listen for incoming
     *             connections.
     * @throws ConnectFourException If there is an error creating the
     *                              {@link ServerSocket}
     */
    public ConnectFourServer(int port) throws ConnectFourException {
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            throw new ConnectFourException(e);
        }
    }

    /**
     * Starts a new {@link ConnectFourServer}. Simply creates the server and
     * calls {@link #run()} in the main thread.
     *
     * @param args Used to specify the port on which the server should listen
     *             for incoming client connections.
     * @throws ConnectFourException If there is an error starting the server.
     */
    public static void main(String[] args) throws ConnectFourException {

        if (args.length != 1) {
            System.out.println("Usage: java ConnectFourServer <port>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        ConnectFourServer server = new ConnectFourServer(port);
        server.run();
    }

    /**
     * Waits for two clients to connect. Creates a {@link ConnectFourPlayer}
     * for each and then pairs them off in a {@link ConnectFourGame}.<P>
     */
    @Override
    public void run() {
        try {
            System.out.println("Waiting for player one...");
            Socket playerOneSocket = server.accept();
            ConnectFourPlayer playerOne =
                    new ConnectFourPlayer(playerOneSocket);
            playerOne.connect();
            System.out.println("Player one connected!");

            System.out.println("Waiting for player two...");
            Socket playerTwoSocket = server.accept();
            ConnectFourPlayer playerTwo =
                    new ConnectFourPlayer(playerTwoSocket);
            playerTwo.connect();
            System.out.println("Player two connected!");

            System.out.println("Starting game!");
            ConnectFourGame game =
                    new ConnectFourGame(playerOne, playerTwo);
            // server is not multithreaded
            new Thread(game).run();
        } catch (IOException e) {
            System.err.println("Something has gone horribly wrong!");
            e.printStackTrace();
        } catch (ConnectFourException e) {
            System.err.println("Failed to create players!");
            e.printStackTrace();
        }
    }
}

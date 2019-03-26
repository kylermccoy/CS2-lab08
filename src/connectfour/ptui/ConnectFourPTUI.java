package connectfour.ptui;

import connectfour.ConnectFourException;
import connectfour.client.ConnectFourBoard;
import connectfour.client.ConnectFourNetworkClient;
import connectfour.client.Observer;

import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

/**
 * The plain text UI for the Connect Four game.  This class represents both the
 * View and Controller in the MVC pattern.
 *
 * @author James Heloitis @ RIT CS
 * @author Sean Strout @ RIT CS
 */
public class ConnectFourPTUI extends ConsoleApplication implements Observer<ConnectFourBoard> {
    /** the model */
    private ConnectFourBoard board;
    /** connection to network interface to server */
    private ConnectFourNetworkClient serverConn;
    /** What to read to see what user types */
    private Scanner userIn;
    /** Where to send text that the user can see */
    private PrintWriter userOut;

    /**
     * Create the board model, create the network connection based on
     * command line parameters, and use the first message received to
     * allocate the board size the server is also using.
     */
    @Override
    public void init() {
        try {
            List<String> args = super.getArguments();

            // get host info from command line
            String host = args.get(0);
            int port = Integer.parseInt(args.get(1));

            // create uninitialized board
            this.board = new ConnectFourBoard();

            // add ourselves as an observer
            this.board.addObserver(this);

            // create the network connection
            this.serverConn = new ConnectFourNetworkClient(host, port, this.board);
        }
        catch( ConnectFourException |
                ArrayIndexOutOfBoundsException |
                NumberFormatException e ) {
            System.err.println(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Update all GUI Nodes to match the state of the model.
     */
    private void refresh(ConnectFourBoard board) {
        if (!board.isMyTurn()) {
            this.userOut.println(board);
            this.userOut.println(board.getMovesLeft() + " moves left." );
            ConnectFourBoard.Status status = board.getStatus();
            switch (status) {
                case ERROR:
                    this.userOut.println( status );
                    this.endGame();
                    break;
                case I_WON:
                    this.userOut.println( "You won. Yay!" );
                    this.endGame();
                    break;
                case I_LOST:
                    this.userOut.println( "You lost. Boo!" );
                    this.endGame();
                    break;
                case TIE:
                    this.userOut.println( "Tie game. Meh." );
                    this.endGame();
                    break;
                default:
                    this.userOut.println();
            }
        }
        else {
            boolean done = false;
            do {
                this.userOut.print("Enter column: ");
                this.userOut.flush();
                int col = this.userIn.nextInt();
                if (board.isValidMove(col)) {
                    this.userOut.println(this.userIn.nextLine());
                    this.serverConn.sendMove(col);
                    done = true;
                }
            } while (!done);
        }
    }

    @Override
    public void update(ConnectFourBoard board) {
        refresh(board);
    }

    /**
     * This method continues running until the game is over.
     * It is not like {@link javafx.application.Application#start(Stage)}.
     * That method returns as soon as the setup is done.
     * This method waits for a notification from {@link #endGame()},
     * called indirectly from a model update from {@link ConnectFourNetworkClient}.
     *
     * @param userIn what to read to see what user types
     * @param userOut where to send messages so user can see them
     */
    @Override
    public synchronized void go( Scanner userIn, PrintWriter userOut ) {
        this.userIn = userIn;
        this.userOut = userOut;

        // Start the network client listener thread
        this.serverConn.startListener();

        // Manually force a display of all board state, since it's too late
        // to trigger update().
        this.refresh(this.board);

        while (this.board.getStatus() == ConnectFourBoard.Status.NOT_OVER) {
            try {
                this.wait();
            }
            catch( InterruptedException ie ) {}
        }

    }

    private synchronized void endGame() {
        this.notify();
    }

    /**
     * GUI is closing, so close the network connection. Server will get the message.
     */
    @Override
    public void stop() {
        this.userIn.close();
        this.userOut.close();
        this.serverConn.close();
    }

    /**
     * Launch the JavaFX GUI.
     *
     * @param args not used, here, but named arguments are passed to the GUI.
     *             <code>--host=<i>hostname</i> --port=<i>portnum</i></code>
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java ConnectFourGUI host port");
            System.exit(-1);
        } else {
            ConsoleApplication.launch(ConnectFourPTUI.class, args);
        }
    }
}

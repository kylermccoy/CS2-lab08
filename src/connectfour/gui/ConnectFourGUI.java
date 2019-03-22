package connectfour.gui;

import connectfour.client.ConnectFourBoard;
import connectfour.client.Observer;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

/**
 * A JavaFX GUI for the networked Connect Four game.
 *
 * @author James Heloitis @ RIT CS
 * @author Sean Strout @ RIT CS
 * @author YOUR NAME HERE
 */
public class ConnectFourGUI extends Application implements Observer<ConnectFourBoard> {

    @Override
    public void init() {
        try {
            // get the command line args
            List<String> args = getParameters().getRaw();

            // get host info and port from command line
            String host = args.get(0);
            int port = Integer.parseInt(args.get(1));

            // TODO
        } catch(NumberFormatException e) {
            System.err.println(e);
            throw new RuntimeException(e);
        }
    }


    /**
     * Construct the layout for the game.
     *
     * @param stage container (window) in which to render the GUI
     * @throws Exception if there is a problem
     */
    public void start( Stage stage ) throws Exception {
        // TODO

        stage.show();
    }

    /**
     * GUI is closing, so close the network connection. Server will get the message.
     */
    @Override
    public void stop() {
        // TODO
    }


    /**
     * Called by the model, client.ConnectFourBoard, whenever there is a state change
     * that needs to be updated by the GUI.
     *
     * @param connectFourBoard
     */
    @Override
    public void update(ConnectFourBoard connectFourBoard) {
        // TODO
    }

    /**
     * The main method expects the host and port.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java ConnectFourGUI host port");
            System.exit(-1);
        } else {
            Application.launch(args);
        }
    }
}

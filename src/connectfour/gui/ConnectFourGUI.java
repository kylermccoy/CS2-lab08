package connectfour.gui;

import connectfour.ConnectFourException;
import connectfour.client.ConnectFourBoard;
import connectfour.client.ConnectFourNetworkClient;
import connectfour.client.Observer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage ;

import java.util.List;

/**
 * A JavaFX GUI for the networked Connect Four game.
 *
 * @author James Heloitis @ RIT CS
 * @author Sean Strout @ RIT CS
 * @author Kyle McCoy
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
            ConnectFourBoard game = new ConnectFourBoard() ;
            ConnectFourNetworkClient client = new ConnectFourNetworkClient(host, port, game) ;
        } catch(NumberFormatException e) {
            System.err.println(e);
            throw new RuntimeException(e);
        }   catch(ConnectFourException e) {
            System.err.println(e) ;
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
        GridPane gridPane = new GridPane() ;
        for(int col = 0; col < 7; col++){
            for(int row = 0; row < 6; row++){
                Button button = new Button() ;
                button.setPrefSize(64, 64) ;
                button.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("empty.png"))));
                gridPane.add(button, col, row) ;
            }
        }
        gridPane.setGridLinesVisible(true) ;
        Label left = new Label("Left") ;
        left.setStyle("-fx-font: " + 18 + " arial;") ;
        Label middle = new Label("Middle") ;
        middle.setStyle("-fx-font: " + 18 + " arial;") ;
        Label right = new Label("Right") ;
        right.setStyle("-fx-font: " + 18 + " arial;") ;
        BorderPane borderPane = new BorderPane() ;
        borderPane.setCenter(middle) ;
        borderPane.setAlignment(middle, Pos.BOTTOM_CENTER) ;
        borderPane.setPrefHeight(64);
        borderPane.setLeft(left) ;
        borderPane.setAlignment(left, Pos.BOTTOM_LEFT) ;
        borderPane.setRight(right) ;
        borderPane.setAlignment(right, Pos.BOTTOM_RIGHT) ;
        VBox vbox = new VBox(gridPane, borderPane) ;
        Scene scene = new Scene(vbox) ;
        stage.setScene(scene) ;
        stage.setTitle("Lab 8: Test") ;

        stage.show();

        // TODO: call startListener() in ConnectFourNetworkClient here
    }

    /**
     * GUI is closing, so close the network connection. Server will get the message.
     */
    @Override
    public void stop() {
        // TODO
    }

    /**
     * Do your GUI updates here.
     */
    private void refresh() {
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
        if ( Platform.isFxApplicationThread() ) {
            this.refresh();
        }
        else {
            Platform.runLater( () -> this.refresh() );
        }
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

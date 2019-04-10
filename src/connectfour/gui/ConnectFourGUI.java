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
import javafx.scene.layout.*;
import javafx.stage.Stage ;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A JavaFX GUI for the networked Connect Four game.
 *
 * @author James Heloitis @ RIT CS
 * @author Sean Strout @ RIT CS
 * @author Kyle McCoy
 */
public class ConnectFourGUI extends Application implements Observer<ConnectFourBoard> {
    // the model
    private ConnectFourBoard game ;
    // the client
    private ConnectFourNetworkClient client ;
    // left label
    private Label left ;
    // middle label
    private Label middle ;
    // right label
    private Label right ;
    // list of buttons
    private List<Button> buttons ;

    @Override
    public void init() {
        try {
            // get the command line args
            List<String> args = getParameters().getRaw();

            // get host info and port from command line
            String host = args.get(0);
            int port = Integer.parseInt(args.get(1));

            // TODO
            this.game = new ConnectFourBoard() ;
            this.client = new ConnectFourNetworkClient(host, port, this.game) ;
            this.game.addObserver(this);
        } catch(NumberFormatException e) {
            System.err.println(e);
            throw new RuntimeException(e);
        } catch(ConnectFourException e) {
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
        buttons = new ArrayList<>() ;
        GridPane gridPane = new GridPane() ;
        // loop to insert buttons into gridpane
        for(int row = 0; row < 6; row++){
            for(int col = 0; col < 7; col++){
                int move = col ;
                Button button = new Button() ;
                button.setPrefSize(64, 64) ;
                button.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("empty.png"))));
                button.setOnAction( event -> {
                    if(game.isValidMove(move) && game.isMyTurn()) {
                        client.sendMove(move);
                    }
                });
                buttons.add(button) ;
                gridPane.add(button, col, row) ;
            }
        }
        gridPane.setGridLinesVisible(true) ;
        left = new Label(game.getMovesLeft() + " Moves Left") ;
        left.setStyle("-fx-font: " + 18 + " arial;") ;
        middle = new Label("*") ;
        middle.setStyle("-fx-font: " + 18 + " arial;") ;
        right = new Label("STARTING GAME!") ;
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
        stage.setTitle("Connect Four") ;

        stage.show();

        // TODO: call startListener() in ConnectFourNetworkClient here
        client.startListener();
    }

    /**
     * GUI is closing, so close the network connection. Server will get the message.
     */
    @Override
    public void stop() {
        // TODO
        this.game.close();
        this.client.close();
    }

    /**
     * Do your GUI updates here.
     */
    private void refresh() {
        // TODO
        // loop through buttons and change image displayed, disable button if not the user's turn
        int i = 0 ;
        for(int row = 0; row < 6; row++){
            for(int col = 0; col < 7; col++){
                if(this.game.getContents(row,col) == ConnectFourBoard.Move.PLAYER_ONE){
                    buttons.get(i).setGraphic(new ImageView(new Image(getClass().getResourceAsStream("p1black.png"))));
                }else if(this.game.getContents(row,col) == ConnectFourBoard.Move.PLAYER_TWO){
                    buttons.get(i).setGraphic(new ImageView(new Image(getClass().getResourceAsStream("p2red.png"))));
                }
                if(this.game.isMyTurn()){
                    buttons.get(i).setDisable(false);
                }else{
                    buttons.get(i).setDisable(true);
                }
                i++ ;
            }
        }
        // display who's turn it is
        left.setText(this.game.getMovesLeft() + " Moves Left");
        if(this.game.isMyTurn()){
            middle.setText("YOUR TURN!");
        }else{
            middle.setText("OPPONENT'S TURN!");
        }
        // display status of the game
        ConnectFourBoard.Status status = this.game.getStatus() ;
        switch (status){
            case TIE:
                right.setText("TIE!");
                middle.setText("*");
                break;
            case ERROR:
                right.setText("ERROR!");
                middle.setText("*");
                break;
            case I_WON:
                right.setText("YOU WON!");
                middle.setText("*");
                break;
            case I_LOST:
                right.setText("YOU LOST!");
                middle.setText("*");
                break;
            case NOT_OVER:
                right.setText("NOT OVER!");
                break;
        }
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

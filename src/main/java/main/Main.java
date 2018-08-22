package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.network.data.DataConnectedComputer;

import java.io.IOException;
import java.net.ServerSocket;

public class Main extends Application {

    public enum SceneId {
        STARTUP,
        TRANSFER
    }

    public static volatile boolean exit = false;
    public static boolean isHost = false;
    public static ServerSocket server;
    public static DataConnectedComputer hostComputer;
    public static String connectedHostIp;
    public static int port;
    public static Stage currentStage;
    public static Main main;

    @Override
    public void start(Stage primaryStage) {
        main = this;
        Main.currentStage = primaryStage;

        primaryStage.setOnCloseRequest(event -> {
            exit = true;
        });
        showScene(SceneId.STARTUP);
    }

    public void showScene(SceneId sceneId) {
        try {

            Parent root;
            Main.currentStage.close();

            switch (sceneId) {
                case STARTUP:
                    root = FXMLLoader.load(Main.main.getClass().getResource("/startup.fxml"));
                    currentStage.setTitle("P2P File Transfer");
                    currentStage.setScene(new Scene(root, 300, 325));
                    break;

                case TRANSFER:
                    root = FXMLLoader.load(Main.main.getClass().getResource("/transfer.fxml"));
                    Main.currentStage.setScene(new Scene(root, 1000, 700));
                    break;
            }

            currentStage.show();

        } catch (IOException e) {
            System.err.println("Could not load scene");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

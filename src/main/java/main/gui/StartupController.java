package main.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import main.Main;
import main.network.data.DataConnectedComputer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class StartupController {


    public TextField tfPort;
    public TextField tfIpv4;
    public TextField tfIpv6;
    public Button btnHost;
    public Button btnConnectIpv4;
    public Button btnConnectIpv6;

    public void host(ActionEvent actionEvent) {
        Main.isHost = true;

        try {
            Main.server = new ServerSocket(Integer.valueOf(tfPort.getText()));
            startTransferGui();
        } catch(Exception e) {
            e.printStackTrace();

            // Show Error
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Could Not Start Server");
            alert.setHeaderText("Could not start local server on port " + tfPort.getText());
            alert.setContentText(e.toString());
            alert.showAndWait();
        }
    }

    public void connectIpv4(ActionEvent actionEvent) {
        connect(tfIpv4.getText());
    }

    public void connectIpv6(ActionEvent actionEvent) {
        connect(tfIpv6.getText());
    }

    private void connect(String ip) {
        Main.connectedHostIp = ip;

        try {
            Socket connectedHostSocket = new Socket(ip, Integer.valueOf(tfPort.getText()));
            Main.hostComputer = new DataConnectedComputer(connectedHostSocket, connectedHostSocket.getInputStream(), connectedHostSocket.getOutputStream(), null, true, null);
            startTransferGui();
        } catch (IOException e) {
            e.printStackTrace();

            // Show Error
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Could Not Connect To Server");
            alert.setHeaderText("Could not connect to server " + ip + " on port " + tfPort.getText());
            alert.setContentText(e.toString());
            alert.showAndWait();
        }
    }

    private void startTransferGui() {
        Main.port = Integer.valueOf(tfPort.getText());

        Main.main.showScene(Main.SceneId.TRANSFER);
    }
}

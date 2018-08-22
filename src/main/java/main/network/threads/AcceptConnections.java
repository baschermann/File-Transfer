package main.network.threads;

import javafx.application.Platform;
import main.Main;
import main.gui.TransferController;
import main.gui.tabledata.TabledataSessionComputer;
import main.network.data.DataConnectedComputer;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class AcceptConnections extends Thread {

    private TransferController transferController;
    private ServerSocket server;

    public AcceptConnections(ServerSocket server, TransferController transferController) {
        this.transferController = transferController;
        this.server = server;
        setName("Accept Connections");
        start();
    }

    @Override
    public void run() {
        startListening();
    }

    private void startListening() {

        while (!Main.exit) {
            try {
                server.setSoTimeout(1000);
                Socket client = server.accept();

                TabledataSessionComputer tabledataSessionComputer = new TabledataSessionComputer("Client", "Connected", client.getInetAddress().toString());
                DataConnectedComputer dataConnectedComputer = new DataConnectedComputer(client, client.getInputStream(), client.getOutputStream(), tabledataSessionComputer, false, transferController);

                Platform.runLater(() -> {
                    transferController.tabledataConnectedComputerList.add(tabledataSessionComputer);
                    transferController.sendAllOfferedFiles(dataConnectedComputer);
                });
                transferController.connections.add(dataConnectedComputer);

            } catch (SocketTimeoutException e) {
                // Retry again!
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

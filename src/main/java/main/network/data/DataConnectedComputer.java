package main.network.data;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import main.Main;
import main.gui.TransferController;
import main.gui.tabledata.TabledataQueueFile;
import main.gui.tabledata.TabledataSessionComputer;
import main.network.BandwithLimitInformer;
import main.network.threads.ReceiveData;
import main.network.threads.SendData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class DataConnectedComputer {

    public final boolean isHost;
    public volatile boolean isAlive = true;
    public Socket socket;
    public InputStream in;
    public OutputStream out;
    public TabledataSessionComputer tabledataSessionComputer;
    private List<DataDownloadFileTask> downloads = new ArrayList<>();
    private List<DataUploadFileTask> uploads = new ArrayList<>();
    private List<DataCommandTask> commands = new ArrayList<>();

    public ReceiveData receiveData;
    public SendData sendData;
    private TransferController transferController;
    public BandwithLimitInformer downloadBandwidthLimitInformer;

    public DataConnectedComputer(Socket socket, InputStream in, OutputStream out, TabledataSessionComputer tabledataSessionComputer, boolean isHost, TransferController transferController) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.tabledataSessionComputer = tabledataSessionComputer;
        this.isHost = isHost;
        this.transferController = transferController;
        this.receiveData = new ReceiveData(this, transferController);
        this.sendData = new SendData(this, transferController);

        if(transferController != null)
            transferController.uploadBandwidthLimitInformer.register(this);

        this.downloadBandwidthLimitInformer = new BandwithLimitInformer();
        this.downloadBandwidthLimitInformer.register(this);
    }

    public void disconnect() {
        try {
            if(!isHost) {
                System.out.println("Removing ourselves " + socket.getInetAddress().toString());

                Platform.runLater(() -> {
                    // Remove all Queued up files are being downloaded or uploaded
                    System.out.println("Remove all files");

                    List<TabledataQueueFile> deleteFiles = new ArrayList<>();

                    for (TabledataQueueFile queueFile : transferController.tabledataQueueFileList) {
                        if (queueFile.getConnectedComputer() == this) {
                            System.out.println("Remove file: " + queueFile.getFileName());
                            deleteFiles.add(queueFile);
                        }
                    }

                    transferController.tabledataQueueFileList.removeAll(deleteFiles);

                    // TODO Remove all upload and download tasks
                });
            } else {
                System.out.println("Removing Host " + socket.getInetAddress().toString());

                Platform.runLater(() -> {
                    // Show Alert
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("P2P File Transfer");
                    alert.setHeaderText("Connection to the Host was lost");
                    alert.setContentText("Either the host has closed the the program or the connection was lost.");

                    alert.showAndWait().ifPresent(response -> {
                        // Bring the user back to connection Menu
                        if (response == ButtonType.OK) {
                            Main.main.showScene(Main.SceneId.STARTUP);
                        }
                    });
                });

            }
            socket.close();
        } catch (IOException e) {
            System.err.println("Error closing stream while removing Connection: " + e.toString());
        }


        Platform.runLater(() -> {
            transferController.connections.remove(this);
            transferController.tabledataConnectedComputerList.remove(tabledataSessionComputer);
            transferController.removeAllOfferedFiles(this);
        });

        transferController.uploadBandwidthLimitInformer.unregister(this);
        isAlive = false;
    }


    public synchronized void addDownload(DataDownloadFileTask downloadFile) {
        downloads.add(downloadFile);
    }

    public synchronized DataDownloadFileTask getDownloadFileTask(String hash) {

        for (DataDownloadFileTask downloadFileTask : downloads) {
            if(downloadFileTask.tabledataQueueFile.getHash().equals(hash)) {
                return downloadFileTask;
            }
        }
        return null;
    }

    public synchronized void addUpload(DataUploadFileTask uploadFileTask) {
        uploads.add(uploadFileTask);
    }

    public synchronized DataUploadFileTask popUploadFileTask() {
        if(uploads.size() > 0) {
            DataUploadFileTask uploadFile = uploads.get(0);
            uploads.remove(0);
            return uploadFile;
        } else {
            return null;
        }
    }

    // TODO Remove Upload and Download and probably stop running ones


    public synchronized void sendCommand(DataCommandTask commandTask) {
        commands.add(commandTask);
    }

    public synchronized DataCommandTask popCommand() {
        if(commands.size() > 0) {
            DataCommandTask commandTask = commands.get(0);
            commands.remove(0);
            return commandTask;
        } else {
            return null;
        }
    }

    public TransferController getTransferController() {
        return transferController;
    }

    public void setTransferController(TransferController transferController) {
        this.transferController = transferController;
        receiveData.transferController = transferController;
        sendData.transferController = transferController;
        transferController.uploadBandwidthLimitInformer.register(this);
    }
}

package main.network.threads;

import javafx.application.Platform;
import main.Constants;
import main.Main;
import main.gui.TransferController;
import main.gui.tabledata.TabledataOfferedFile;
import main.gui.tabledata.TabledataOwnOfferedFile;
import main.gui.tabledata.TabledataQueueFile;
import main.network.data.DataConnectedComputer;
import main.network.data.DataDownloadFileTask;
import main.network.data.DataUploadFileTask;

import java.io.*;
import java.math.BigInteger;

public class ReceiveData extends Thread {

    public TransferController transferController;
    private final DataConnectedComputer connectedComputer;
    private DataDownloadFileTask currentDownloadTask = null;


    public ReceiveData(DataConnectedComputer connectedComputer, TransferController transferController) {
        this.transferController = transferController;
        this.connectedComputer = connectedComputer;
        setName("Receive Data");
        start();
    }

    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(connectedComputer.in);
            int identifier;

            // TODO Use Interruptions instead of using the OS to shut down the stream and abandon thread
            // TODO Throttling the download limit blocks receiving commands
            while((identifier = in.read()) != -1 && connectedComputer.isAlive && !Main.exit) {
                switch ((byte) identifier) {
                    case Constants.IDENTIFIER_COMMAND:
                        readCommand(in);
                        break;

                    case Constants.IDENTIFIER_DATA:
                        if(!currentDownloadTask.receive(in)) {
                            System.out.println("File Download complete");
                            currentDownloadTask = null;
                        }
                        break;
                }
            }

            System.out.println("Could not read from stream or program ended");
            connectedComputer.disconnect();
        } catch (IOException e) {
            System.out.println("Input stream disconnect from: " + connectedComputer.socket.getInetAddress().toString());
            System.err.println(e.toString());
            connectedComputer.disconnect();
        }
    }


    private void readCommand(DataInputStream in) throws IOException {
        int commandType = in.read();
        if(commandType == -1) {
            System.err.println("read -1 in command type");
            throw new IOException();
        }

        switch ((byte)commandType) {
            case Constants.COMMAND_PING:
                    // Do nothing, keep the connection alive
                break;

            case Constants.COMMAND_NAME_CHANGE:
                readCommandNameChange(in);
                break;

            case Constants.COMMAND_FILE_AVAILABLE:
                readCommandFileAvailable(in);
                break;

            case Constants.COMMAND_FILE_REMOVE:
                readCommandFileRemoved(in);
                break;

            case Constants.COMMAND_FILE_DOWNLOAD_ANNOUNCE:
                readCommandFileDownloadAnnounce(in);
                break;

            case Constants.COMMAND_FILE_DOWNLOAD_CANCEL:
                readCommandFileDownloadCancel(in);
                break;

            case Constants.COMMAND_FILE_UPLOAD_START:
                readCommandFileUploadStart(in);
                break;

            case Constants.COMMAND_FILE_DOWNLOAD_ACCEPT:
                readCommandFileDownloadAccept(in);
                break;

            case Constants.COMMAND_TRANSFER_LIMIT:
                readCommandTransferLimit(in);
                break;
        }
    }

    private void readCommandFileRemoved(DataInputStream in) throws IOException {
        final String hash = in.readUTF();
        Platform.runLater(() -> transferController.removeOfferedFile(connectedComputer, hash));
    }

    private void readCommandFileAvailable(DataInputStream in) throws IOException {
        String fileName = in.readUTF();
        String hash = in.readUTF();
        long fileSize = in.readLong();
        String from = connectedComputer.tabledataSessionComputer.getName();

        final TabledataOfferedFile offeredFile = new TabledataOfferedFile(fileName, hash, fileSize, from, connectedComputer);
        Platform.runLater(() -> transferController.tabledataOfferedFilesList.add(offeredFile));
    }

    private void readCommandNameChange(DataInputStream in) throws IOException {
        String newName = in.readUTF();
        Platform.runLater(() -> connectedComputer.tabledataSessionComputer.setName(newName));
    }

    private void readCommandFileDownloadAnnounce(DataInputStream in) throws IOException {
        String hash = in.readUTF();

        // Someone wants to download a file. Add it to our uploads
        Platform.runLater(() -> {

            TabledataOwnOfferedFile ownOfferedFile = null;

            for (TabledataOwnOfferedFile file : transferController.tabledataOwnOfferedFilesList) {
                if(file.getHash().equals(hash))
                    ownOfferedFile = file;
            }

            if(ownOfferedFile != null) {
                //System.out.println("Found file to upload: " + ownOfferedFile.getFileName());
                TabledataQueueFile uploadFile = new TabledataQueueFile(ownOfferedFile.getFileName(), "Waiting", "-", connectedComputer, TabledataQueueFile.Type.UPLOAD, hash);
                transferController.tabledataQueueFileList.add(uploadFile);

                DataUploadFileTask dataUploadFileTask = new DataUploadFileTask(ownOfferedFile, uploadFile);
                connectedComputer.addUpload(dataUploadFileTask);
                uploadFile.setUploadFileTask(dataUploadFileTask);
            } else {
                System.err.println("File that wants to be downloaded can't be found in our own Files. Hash: " + hash);
            }
        });
    }

    private void readCommandFileDownloadCancel(DataInputStream in) throws IOException {
        String hash = in.readUTF();

        // Someone wants to download a file. Add it to our uploads
        Platform.runLater(() -> {
            transferController.removeQueuedUploadFile(hash);
        });

        // TODO Cancel the current download if it matches
    }

    private void readCommandFileUploadStart(DataInputStream in)  throws IOException {
        final String hash = in.readUTF();

        // Uploader wants to start sending a file, set it to current file and prepare
        currentDownloadTask = connectedComputer.getDownloadFileTask(hash);
        currentDownloadTask.prepare();
    }

    private void readCommandFileDownloadAccept(DataInputStream in)  throws IOException {
        final String hash = in.readUTF();

        // Downloader has prepared the file and is ready to being sent data
        DataUploadFileTask currentUploadTask = connectedComputer.sendData.currentUploadTask;
        if(currentUploadTask.tabledataOwnOfferedFile.getHash().equals(hash)) {
            currentUploadTask.setUploadStatus(DataUploadFileTask.UploadStatus.UPLOADING);
            connectedComputer.sendData.isSendingFile = true;
        }
    }

    private void readCommandTransferLimit(DataInputStream in)  throws IOException {
        final long byteLimit = in.readLong();

        // Downloader set a download limit
        connectedComputer.downloadBandwidthLimitInformer.setLimitPerSecond(byteLimit);
    }
}

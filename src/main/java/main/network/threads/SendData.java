package main.network.threads;

import main.Constants;
import main.Main;
import main.gui.TransferController;
import main.network.data.DataConnectedComputer;
import main.network.data.DataCommandTask;
import main.network.data.DataUploadFileTask;

import java.io.*;

public class SendData extends Thread {

    private final DataConnectedComputer connectedComputer;
    public boolean isSendingFile = false;
    private long lastPing = System.currentTimeMillis();
    public DataUploadFileTask currentUploadTask = null;
    public TransferController transferController;

    public SendData(DataConnectedComputer connectedComputer, TransferController transferController) {
        this.connectedComputer = connectedComputer;
        this.transferController = transferController;
        setName("Send Data");
        start();
    }

    @Override
    public void run() {

        while(connectedComputer.isAlive && !Main.exit) {

            // Send commands
            DataCommandTask taskToSendOver;
            while((taskToSendOver = connectedComputer.popCommand()) != null) {
                sendCommandOver(taskToSendOver);
            }

            // Sending File
            if(currentUploadTask == null) {
                DataUploadFileTask dataUploadFileTask = connectedComputer.popUploadFileTask();

                if (dataUploadFileTask != null) {
                    currentUploadTask = dataUploadFileTask;

                    DataCommandTask commandTask = DataCommandTask.getFileUploadStartTask(currentUploadTask.tabledataOwnOfferedFile.getHash());
                    connectedComputer.sendCommand(commandTask);

                    currentUploadTask.setUploadStatus(DataUploadFileTask.UploadStatus.PREPARING);
                    currentUploadTask.prepare();
                }
            }

            if(isSendingFile) {
                // TODO Get the real time the thread was sleeping instead of 25 ms
                try {
                    if(!currentUploadTask.send(transferController.uploadBandwidthLimitInformer, connectedComputer.downloadBandwidthLimitInformer)) {
                        System.out.println("File Transfer complete");
                        isSendingFile = false;
                        currentUploadTask = null;
                    }

                    // Only sleep if upload limit is set
                    if(!transferController.uploadBandwidthLimitInformer.unlimited || !connectedComputer.downloadBandwidthLimitInformer.unlimited)
                        Thread.sleep(transferController.uploadBandwidthLimitInformer.timer);

                } catch (IOException e) {
                    e.printStackTrace();
                    currentUploadTask.setUploadStatus(DataUploadFileTask.UploadStatus.ERROR);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }

            } else {

                // Ping every 2000ms to keep the connection alive
                if((System.currentTimeMillis() - lastPing) > 2000) {
                    lastPing = System.currentTimeMillis();
                    connectedComputer.sendCommand(DataCommandTask.getPingTask());
                }

                // Let the thread sleep when not sending a file
                try {
                    Thread.sleep(200);
                    // TODO Use ScheduledExecutor to use the Scheduling Service from the OS to be more precise
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendCommandOver(DataCommandTask dataCommandTask) {
        DataOutputStream out = new DataOutputStream(connectedComputer.out);

        try {
            out.write(Constants.IDENTIFIER_COMMAND);
            out.write(dataCommandTask.command);

            switch (dataCommandTask.command) {
                case Constants.COMMAND_PING:
                    // Nothing, just send a Ping
                    break;

                case Constants.COMMAND_NAME_CHANGE:
                    out.writeUTF(dataCommandTask.newName);
                    break;

                case Constants.COMMAND_FILE_AVAILABLE:
                    out.writeUTF(dataCommandTask.fileName);
                    out.writeUTF(dataCommandTask.hash);
                    out.writeLong(dataCommandTask.fileSize);
                    break;

                case Constants.COMMAND_FILE_REMOVE:
                    out.writeUTF(dataCommandTask.hash);
                    break;

                case Constants.COMMAND_FILE_DOWNLOAD_ANNOUNCE:
                    out.writeUTF(dataCommandTask.hash);
                    break;

                case Constants.COMMAND_FILE_DOWNLOAD_CANCEL:
                    out.writeUTF(dataCommandTask.hash);
                    break;

                case Constants.COMMAND_FILE_UPLOAD_START:
                    out.writeUTF(dataCommandTask.hash);
                    break;

                case Constants.COMMAND_FILE_DOWNLOAD_ACCEPT:
                    out.writeUTF(dataCommandTask.hash);
                    break;

                case Constants.COMMAND_TRANSFER_LIMIT:
                    out.writeLong(dataCommandTask.byteLimit);
                    break;
            }

            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            connectedComputer.disconnect();
        }
    }
}

package main.network.data;

import main.Constants;

import java.math.BigInteger;

public class DataCommandTask {

    public byte command;
    public String newName;
    public String fileName;
    public String hash;
    public long fileSize;
    public long byteLimit;

    private DataCommandTask(byte command) {
        this.command = command;
    }

    public static DataCommandTask getPingTask() {
        return new DataCommandTask(Constants.COMMAND_PING);
    }

    public static DataCommandTask getNameChangedTask(String newName) {
        DataCommandTask commandTask = new DataCommandTask(Constants.COMMAND_NAME_CHANGE);
        commandTask.newName = newName;
        return commandTask;
    }

    public static DataCommandTask getFileAvailableTask(String fileName, String hash, long fileSize) {
        DataCommandTask commandTask = new DataCommandTask(Constants.COMMAND_FILE_AVAILABLE);
        commandTask.fileName = fileName;
        commandTask.hash = hash;
        commandTask.fileSize = fileSize;
        return commandTask;
    }

    public static DataCommandTask getFileRemoveTask(String hash) {
        DataCommandTask commandTask = new DataCommandTask(Constants.COMMAND_FILE_REMOVE);
        commandTask.hash = hash;
        return commandTask;
    }

    public static DataCommandTask getFileDownloadAnnounceTask(String hash) {
        DataCommandTask commandTask = new DataCommandTask(Constants.COMMAND_FILE_DOWNLOAD_ANNOUNCE);
        commandTask.hash = hash;
        return commandTask;
    }

    public static DataCommandTask getFileDownloadCancelTask(String hash) {
        DataCommandTask commandTask = new DataCommandTask(Constants.COMMAND_FILE_DOWNLOAD_CANCEL);
        commandTask.hash = hash;
        return commandTask;
    }

    public static DataCommandTask getFileUploadStartTask(String hash) {
        DataCommandTask commandTask = new DataCommandTask(Constants.COMMAND_FILE_UPLOAD_START);
        commandTask.hash = hash;
        return commandTask;
    }

    public static DataCommandTask getFileDownloadAcceptTask(String hash) {
        DataCommandTask commandTask = new DataCommandTask(Constants.COMMAND_FILE_DOWNLOAD_ACCEPT);
        commandTask.hash = hash;
        return commandTask;
    }

    public static DataCommandTask getTransferLimitTask(long byteLimit) {
        DataCommandTask commandTask = new DataCommandTask(Constants.COMMAND_TRANSFER_LIMIT);
        commandTask.byteLimit = byteLimit;
        return commandTask;
    }
}

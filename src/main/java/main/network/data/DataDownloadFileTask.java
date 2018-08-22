package main.network.data;


import main.Constants;
import main.file.FileUtility;
import main.gui.tabledata.TabledataOfferedFile;
import main.gui.tabledata.TabledataQueueFile;

import java.io.*;

public class DataDownloadFileTask {

    public enum DownloadStatus {
        WAITING,
        PREPARING,
        DOWNLOADING,
        PAUSED,
        DELETING,
        ERROR,
        CHECKING,
        FINISHED
    }

    public DownloadStatus downloadStatus = DownloadStatus.WAITING;
    public TabledataOfferedFile tabledataOfferedFile;
    public TabledataQueueFile tabledataQueueFile;
    public File file = null;
    public RandomAccessFile randomAccessFile = null;
    public File downloadDirectory;
    private long lastUpdateTime;

    private long totalFileSize;
    private long currentSize = 0;
    private byte[] buffer = new byte[1];


    public DataDownloadFileTask(TabledataOfferedFile tabledataOfferedFile, TabledataQueueFile tabledataQueueFile) {
        this.tabledataOfferedFile = tabledataOfferedFile;
        this.tabledataQueueFile = tabledataQueueFile;
        this.downloadDirectory = tabledataOfferedFile.getConnectedComputer().getTransferController().downloadDirectory;
        this.lastUpdateTime = System.currentTimeMillis();
        this.totalFileSize = tabledataOfferedFile.getFileSize();
    }

    public void prepare() {
        setDownloadStatus(DownloadStatus.PREPARING);

        // Prepare File
        file = findAvailableFileName(downloadDirectory.getAbsolutePath(), tabledataOfferedFile.getFileName(), "tmp");

        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.setLength(tabledataOfferedFile.getFileSize());
            DataCommandTask commandTask = DataCommandTask.getFileDownloadAcceptTask(tabledataOfferedFile.getHash());
            tabledataOfferedFile.getConnectedComputer().sendCommand(commandTask);
            setDownloadStatus(DownloadStatus.DOWNLOADING);
            tabledataQueueFile.setPercent("0%");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            setDownloadStatus(DownloadStatus.ERROR);
            // TODO Send the error to the uploader
        }
    }

    public boolean receive(DataInputStream dataInputStream) throws IOException {
        long receiveAmount = dataInputStream.readLong();

        //System.out.println("receive amount: " + receiveAmount +", fileSize: " + tabledataOfferedFile.getFileSize() + ", currentPosition: " + currentSize);
        if(buffer.length != receiveAmount) {
            buffer = new byte[(int) receiveAmount];
        }

        dataInputStream.readFully(buffer);
        randomAccessFile.write(buffer);
        currentSize += receiveAmount;

        if(System.currentTimeMillis() - lastUpdateTime > Constants.GUI_UPDATE_INTERVAL) {
            tabledataQueueFile.setPercent(String.format("%.2f%%", 100 * ((double) currentSize / (double) tabledataOfferedFile.getFileSize())));
            lastUpdateTime = System.currentTimeMillis();
        }

        if(currentSize == randomAccessFile.length()) {
            System.out.println("File completely received");

            randomAccessFile.close();
            setDownloadStatus(DownloadStatus.CHECKING);

            if(FileUtility.Sha256(file).hash.equals(tabledataOfferedFile.getHash())) {

                String oldName = file.getAbsolutePath();
                File newName = findAvailableFileName(downloadDirectory.getAbsolutePath(), file.getName().substring(0, file.getName().length() - 3), null);

                if(!file.renameTo(newName)) {
                    System.err.println("Could not rename " + oldName + " to " + newName.getAbsolutePath());
                }

                setDownloadStatus(DownloadStatus.FINISHED);
                tabledataQueueFile.setPercent("100%");
                return false;

            } else {
                System.err.println("Hash incorrect, file transfer failed");
                setDownloadStatus(DownloadStatus.ERROR);
                tabledataQueueFile.setPercent("CRC");
                return false;
            }
        }

        return true;
    }

    private File findAvailableFileName(String path, String filename, String extension) {

        File localFile = new File(path + "/" + filename + (extension == null ? "" : "." + extension));

        if(localFile.exists() || localFile.isDirectory()) {
            int counter = 1;
            do {
                StringBuilder filenameBuilder = new StringBuilder(filename);
                filenameBuilder.insert(filenameBuilder.length() - 5, " (" + String.valueOf(counter) + ")");
                String ext = extension == null ? "" : "." + extension;

                localFile = new File(path + "/" + filenameBuilder + ext);
                counter++;
            } while(localFile.exists() || localFile.isDirectory());
        }

        return localFile;
    }

    public void setDownloadStatus(DownloadStatus newDownloadStatus) {
        switch (newDownloadStatus) {
            case WAITING:
                downloadStatus = DownloadStatus.WAITING;
                tabledataQueueFile.setStatus("Waiting");
                break;
            case PREPARING:
                downloadStatus = DownloadStatus.PREPARING;
                tabledataQueueFile.setStatus("Preparing");
                break;
            case DOWNLOADING:
                downloadStatus = DownloadStatus.DOWNLOADING;
                tabledataQueueFile.setStatus("Downloading");
                break;
            case PAUSED:
                downloadStatus = DownloadStatus.PAUSED;
                tabledataQueueFile.setStatus("Paused");
                break;
            case DELETING:
                downloadStatus = DownloadStatus.DELETING;
                tabledataQueueFile.setStatus("Deleting");
                break;
            case ERROR:
                downloadStatus = DownloadStatus.ERROR;
                tabledataQueueFile.setStatus("Error");
                break;
            case FINISHED:
                downloadStatus = DownloadStatus.FINISHED;
                tabledataQueueFile.setStatus("Finished");
                break;
        }
    }

    public long getFileSizeTotal() {
        return totalFileSize;
    }

    public long getFileSizeCurrent() {
        return currentSize;
    }
}

package main.network.data;

import main.Constants;
import main.file.FileUtility;
import main.gui.tabledata.TabledataOwnOfferedFile;
import main.gui.tabledata.TabledataQueueFile;
import main.network.BandwithLimitInformer;

import java.io.*;

public class DataUploadFileTask {

    public enum UploadStatus {
        WAITING,
        PREPARING,
        UPLOADING,
        PAUSED,
        ERROR,
        FINISHED
    }

    public TabledataOwnOfferedFile tabledataOwnOfferedFile;
    public TabledataQueueFile tabledataQueueFile;
    public UploadStatus uploadStatus = UploadStatus.WAITING;
    public BufferedInputStream fileInputStream;
    public DataOutputStream socketOutputStream;

    private byte[] buffer = new byte[1];
    private long fileSize;
    private long currentPosition = 0;
    private long lastUpdateTime;


    public DataUploadFileTask(TabledataOwnOfferedFile ownOfferedFile, TabledataQueueFile tabledataQueueFile) {
        this.tabledataOwnOfferedFile = ownOfferedFile;
        this.tabledataQueueFile = tabledataQueueFile;
        this.socketOutputStream = new DataOutputStream(tabledataQueueFile.getConnectedComputer().out);
        this.lastUpdateTime = System.currentTimeMillis();
        this.fileSize = tabledataOwnOfferedFile.getSize();
    }

    public void prepare() {
        try {
            fileInputStream = new BufferedInputStream(new FileInputStream(tabledataOwnOfferedFile.getFile()));

        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            setUploadStatus(UploadStatus.ERROR);

            // TODO Send the downloader that an error happened
        }
    }

    public boolean send(BandwithLimitInformer uploadLimitInformer, BandwithLimitInformer downloadLimitInformer) throws IOException {
        if(uploadLimitInformer.timer != downloadLimitInformer.timer)
            downloadLimitInformer.setTimer(uploadLimitInformer.timer);

        long sendAmount = uploadLimitInformer.unlimited ? 32768 : uploadLimitInformer.limit; // Own upload limit
        sendAmount = downloadLimitInformer.unlimited ? sendAmount : Math.min(sendAmount, downloadLimitInformer.limit); // Other side download limit
        sendAmount = Math.min(sendAmount, fileSize - currentPosition); // rest file size limit

        if(fileSize - currentPosition <= 0) {
            setUploadStatus(UploadStatus.FINISHED);
            tabledataQueueFile.setPercent("100%");
            return false;
        }

        if(buffer.length != sendAmount) {
            buffer = new byte[(int) sendAmount];
            System.out.println("new Buffer size: " + FileUtility.getPrintableSize(sendAmount));
        }

        fileInputStream.read(buffer);

        socketOutputStream.write(Constants.IDENTIFIER_DATA);
        socketOutputStream.writeLong(sendAmount);
        socketOutputStream.write(buffer);

        currentPosition += sendAmount;

        if(System.currentTimeMillis() - lastUpdateTime > Constants.GUI_UPDATE_INTERVAL) {
            tabledataQueueFile.setPercent(String.format("%.2f%%", 100 * ((double) currentPosition / (double) tabledataOwnOfferedFile.getSize())));
            lastUpdateTime = System.currentTimeMillis();
        }

        return true;
    }

    public void setUploadStatus(UploadStatus newUploadStatus) {
        switch (newUploadStatus) {
            case WAITING:
                uploadStatus = UploadStatus.WAITING;
                tabledataQueueFile.setStatus("Waiting");
                break;
            case PREPARING:
                uploadStatus = UploadStatus.PREPARING;
                tabledataQueueFile.setStatus("Preparing");
                break;
            case UPLOADING:
                uploadStatus = UploadStatus.UPLOADING;
                tabledataQueueFile.setStatus("Uploading");
                break;
            case PAUSED:
                uploadStatus = UploadStatus.PAUSED;
                tabledataQueueFile.setStatus("Paused");
                break;
            case ERROR:
                uploadStatus = UploadStatus.ERROR;
                tabledataQueueFile.setStatus("Error");
                break;
            case FINISHED:
                uploadStatus = UploadStatus.FINISHED;
                tabledataQueueFile.setStatus("Finished");
                break;
        }
    }

    public long getFileSizeTotal() {
        return fileSize;
    }

    public long getFileSizeCurrent() {
        return currentPosition;
    }
}

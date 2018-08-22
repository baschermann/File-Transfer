package main.gui.tabledata;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import main.network.data.DataConnectedComputer;
import main.network.data.DataDownloadFileTask;
import main.network.data.DataUploadFileTask;

public class TabledataQueueFile {

    public enum Type {
        DOWNLOAD,
        UPLOAD
    }

    private BooleanProperty selected;
    private StringProperty fileName;
    private StringProperty status;
    private StringProperty percent;
    private DataConnectedComputer connectedComputer;
    private Type type;
    private String hash;
    private DataDownloadFileTask downloadFileTask;
    private DataUploadFileTask uploadFileTask;

    public TabledataQueueFile(String fileName, String status, String percent, DataConnectedComputer connectedComputer, Type type, String hash) {
        this.selected = new SimpleBooleanProperty(false);
        this.fileName = new SimpleStringProperty(fileName);
        this.status = new SimpleStringProperty(status);
        this.percent = new SimpleStringProperty(percent);
        this.connectedComputer = connectedComputer;
        this.type = type;
        this.hash = hash;
    }

    public boolean getSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public String getFileName() {
        return fileName.get();
    }

    public StringProperty fileNameProperty() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public DataConnectedComputer getConnectedComputer() {
        return connectedComputer;
    }

    public void setConnectedComputer(DataConnectedComputer connectedComputer) {
        this.connectedComputer = connectedComputer;
    }

    public String getPercent() {
        return percent.get();
    }

    public StringProperty percentProperty() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent.set(percent);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public DataDownloadFileTask getDownloadFileTask() {
        return downloadFileTask;
    }

    public DataUploadFileTask getUploadFileTask() {
        return uploadFileTask;
    }

    public void setDownloadFileTask(DataDownloadFileTask downloadFileTask) {
        this.downloadFileTask = downloadFileTask;
    }

    public void setUploadFileTask(DataUploadFileTask uploadFileTask) {
        this.uploadFileTask = uploadFileTask;
    }
}

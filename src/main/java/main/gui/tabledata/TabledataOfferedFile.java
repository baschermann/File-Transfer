package main.gui.tabledata;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import main.file.FileUtility;
import main.network.data.DataConnectedComputer;

import java.math.BigInteger;

public class TabledataOfferedFile {

    private StringProperty fileName;
    private BooleanProperty selected;
    private StringProperty printSize;
    private StringProperty from;
    private String hash;
    private long fileSize;
    private DataConnectedComputer connectedComputer;

    public TabledataOfferedFile(String fileName, String hash, long fileSize, String from, DataConnectedComputer connectedComputer) {
        this.fileName = new SimpleStringProperty(fileName);
        this.selected = new SimpleBooleanProperty(false);
        this.printSize = new SimpleStringProperty(FileUtility.getPrintableSize(fileSize));
        this.from = new SimpleStringProperty(from);
        this.hash = hash;
        this.fileSize = fileSize;
        this.connectedComputer = connectedComputer;
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

    public String getPrintSize() {
        return printSize.get();
    }

    public StringProperty printSizeProperty() {
        return printSize;
    }

    public void setPrintSize(String printSize) {
        this.printSize.set(printSize);
    }

    public String getFrom() {
        return from.get();
    }

    public StringProperty fromProperty() {
        return from;
    }

    public void setFrom(String from) {
        this.from.set(from);
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public DataConnectedComputer getConnectedComputer() {
        return connectedComputer;
    }

    public void setConnectedComputer(DataConnectedComputer connectedComputer) {
        this.connectedComputer = connectedComputer;
    }
}

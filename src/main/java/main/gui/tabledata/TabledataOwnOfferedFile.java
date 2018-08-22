package main.gui.tabledata;

import javafx.beans.property.*;
import main.file.DigestInformation;
import main.file.FileUtility;

import java.io.File;
import java.math.BigInteger;

public class TabledataOwnOfferedFile {

    private StringProperty fileName;
    private BooleanProperty selected;
    private File file;
    private String hash;
    private long size;
    private StringProperty printSize;

    public TabledataOwnOfferedFile(File file) {
        DigestInformation digestInformation = FileUtility.Sha256(file);

        this.fileName = new SimpleStringProperty(file.getName());
        this.selected = new SimpleBooleanProperty(false);
        this.file = file;
        this.hash = digestInformation.hash;
        this.size = digestInformation.size;
        this.printSize = new SimpleStringProperty(FileUtility.getPrintableSize(size));
    }

    public String getFileName() {
        return fileName.get();
    }

    public StringProperty fileNameProperty() {
        return fileName;
    }

    public boolean getSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
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
}

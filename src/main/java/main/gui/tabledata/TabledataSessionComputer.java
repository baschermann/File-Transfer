package main.gui.tabledata;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TabledataSessionComputer {

    StringProperty name;
    StringProperty status;
    StringProperty ip;

    public TabledataSessionComputer(String name, String status, String ip) {
        this.name = new SimpleStringProperty(name);
        this.status = new SimpleStringProperty(status);
        this.ip = new SimpleStringProperty(ip);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
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

    public String getIp() {
        return ip.get();
    }

    public StringProperty ipProperty() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip.set(ip);
    }
}

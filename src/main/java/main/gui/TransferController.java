package main.gui;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import main.Constants;
import main.Main;
import main.file.FileUtility;
import main.gui.tabledata.TabledataQueueFile;
import main.gui.tabledata.TabledataOfferedFile;
import main.gui.tabledata.TabledataOwnOfferedFile;
import main.gui.tabledata.TabledataSessionComputer;
import main.network.BandwithLimitInformer;
import main.network.data.DataCommandTask;
import main.network.data.DataDownloadFileTask;
import main.network.data.DataUploadFileTask;
import main.network.threads.AcceptConnections;
import main.network.data.DataConnectedComputer;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class TransferController {

    public Label labelStatusUpper;
    public Label labelUploadUpper;
    public Label labelUploadLower;
    public Label labelDownloadUpper;
    public Label labelDownloadLower;
    public Label labelDownloadDirectory;
    public TableView<TabledataOwnOfferedFile> tableOwnFileOfferings;
    public TableView<TabledataOfferedFile> tableFileOfferings;
    public TableView<TabledataQueueFile> tableQueue;
    public TableView<TabledataSessionComputer> tableSessionComputer;
    public TableColumn<TabledataSessionComputer, String> tableSessionColName;
    public TableColumn<TabledataSessionComputer, String> tableSessionColStatus;
    public TableColumn tableSessionColKickban;
    public TableColumn<TabledataSessionComputer, String> tableSessionColIp;
    public TableColumn<TabledataOwnOfferedFile, Boolean> tableOwnColCheck;
    public TableColumn<TabledataOwnOfferedFile, String> tableOwnColFilename;
    public TableColumn<TabledataOwnOfferedFile, String> tableOwnColSize;
    public TableColumn<TabledataOfferedFile, Boolean> tableOfferColCheck;
    public TableColumn<TabledataOfferedFile, String> tableOfferColFilename;
    public TableColumn<TabledataOfferedFile, String> tableOfferColSize;
    public TableColumn<TabledataOfferedFile, String> tableOfferColFrom;
    public TableColumn<TabledataQueueFile, Boolean> tableQueueColCheck;
    public TableColumn<TabledataQueueFile, String> tableQueueColFilename;
    public TableColumn<TabledataQueueFile, String> tableQueueColStatus;
    public TableColumn<TabledataQueueFile, String> tableQueueColPercent;
    public TextField tfNickname;
    public TextField tfDownloadLimit;
    public TextField tfUploadLimit;
    public ProgressBar progressBarDownload;
    public ProgressBar progressBarUpload;
    private DoubleProperty progressBarDownloadProgressProperty = new SimpleDoubleProperty();
    private DoubleProperty progressBarUploadProgressProperty = new SimpleDoubleProperty();

    public File downloadDirectory;
    public final ObservableList<TabledataOwnOfferedFile> tabledataOwnOfferedFilesList = FXCollections.observableArrayList();
    public final ObservableList<TabledataOfferedFile> tabledataOfferedFilesList = FXCollections.observableArrayList();
    public final ObservableList<TabledataSessionComputer> tabledataConnectedComputerList = FXCollections.observableArrayList();
    public final ObservableList<TabledataQueueFile> tabledataQueueFileList = FXCollections.observableArrayList();
    public List<DataConnectedComputer> connections = new CopyOnWriteArrayList<>();
    public BandwithLimitInformer uploadBandwidthLimitInformer;
    public BandwithLimitInformer downloadBandwidthLimitInformer;
    public Timer progressBarsTimer = new Timer();



    @FXML
    public void initialize() {

        // Setting Download directory
        downloadDirectory = new File(System.getProperty("user.dir") + "\\downloads");
        if(!downloadDirectory.isDirectory()) {
            boolean mkdir = downloadDirectory.mkdir();
            if(mkdir) {
                System.out.println("Download wurde Ordner in " + downloadDirectory.getAbsolutePath() + " erstellt");
            }
        }
        labelDownloadDirectory.setText(downloadDirectory.getAbsolutePath());

        /*
        try {
            System.out.println("Get Absolute Path: " + downloadDirectory.getAbsolutePath());
            System.out.println("Get Path: " + downloadDirectory.getPath());
            System.out.println("Get Canonical Path: " + downloadDirectory.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        uploadBandwidthLimitInformer = new BandwithLimitInformer();
        downloadBandwidthLimitInformer = new BandwithLimitInformer();

        // ###### Bind Table Columns ######
        // Own Files Table
        tableOwnFileOfferings.setItems(tabledataOwnOfferedFilesList);
        tableOwnColCheck.setCellValueFactory(new PropertyValueFactory<>("selected"));
        tableOwnColCheck.setCellFactory(CheckBoxTableCell.forTableColumn(tableOwnColCheck));
        tableOwnColFilename.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        tableOwnColSize.setCellValueFactory(new PropertyValueFactory<>("printSize"));

        // Offered Files Table
        tableFileOfferings.setItems(tabledataOfferedFilesList);
        tableOfferColCheck.setCellValueFactory(new PropertyValueFactory<>("selected"));
        tableOfferColCheck.setCellFactory(CheckBoxTableCell.forTableColumn(tableOfferColCheck));
        tableOfferColFilename.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        tableOfferColSize.setCellValueFactory(new PropertyValueFactory<>("printSize"));
        tableOfferColFrom.setCellValueFactory(new PropertyValueFactory<>("from"));

        // Connected Computer in Session
        tableSessionComputer.setItems(tabledataConnectedComputerList);
        tableSessionColName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableSessionColStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tableSessionColIp.setCellValueFactory(new PropertyValueFactory<>("ip"));

        // Download/Upload Files
        tableQueue.setItems(tabledataQueueFileList);
        tableQueueColCheck.setCellValueFactory(new PropertyValueFactory<>("selected"));
        tableQueueColCheck.setCellFactory(CheckBoxTableCell.forTableColumn(tableQueueColCheck));
        tableQueueColFilename.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        tableQueueColStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tableQueueColPercent.setCellValueFactory(new PropertyValueFactory<>("percent"));


        // Setup Connections
        if(Main.isHost) {
            System.out.println("Hosting...");
            new AcceptConnections(Main.server, this);
        } else {
            System.out.println("Client(ing)...");

            // Add Host Information
            DataConnectedComputer hostComputer = Main.hostComputer;

            TabledataSessionComputer tabledataSessionComputer = new TabledataSessionComputer("Host", "Connected", Main.hostComputer.socket.getInetAddress().toString());
            hostComputer.tabledataSessionComputer = tabledataSessionComputer;
            hostComputer.setTransferController(this);

            tabledataConnectedComputerList.add(tabledataSessionComputer);
            connections.add(hostComputer);
        }

        // Set Status Labels
        if(Main.isHost) {
            labelStatusUpper.setText("You are hosting on Port " + Main.port);
        } else {
            labelStatusUpper.setText("You are connected to " + Main.connectedHostIp + " on Port " + Main.port);
        }

        // Setup Listeners
        tfNickname.textProperty().addListener((observable, oldValue, newValue) -> nicknameChanged());

        // Setup progress bars timer
        progressBarUpload.progressProperty().bind(progressBarUploadProgressProperty);
        progressBarDownload.progressProperty().bind(progressBarDownloadProgressProperty);

        progressBarsTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                int filesDownloadTotal = 0;
                int filesDownloadFinished = 0;
                long downloadTotal = 0;
                long downloadCurrent = 0;

                int filesUploadTotal = 0;
                int filesUploadFinished = 0;
                long uploadTotal = 0;
                long uploadCurrent = 0;

                for (TabledataQueueFile tabledataQueueFile : tabledataQueueFileList) {

                    // Download
                    if(tabledataQueueFile.getType() == TabledataQueueFile.Type.DOWNLOAD) {
                        downloadTotal += tabledataQueueFile.getDownloadFileTask().getFileSizeTotal();
                        downloadCurrent += tabledataQueueFile.getDownloadFileTask().getFileSizeCurrent();

                        ++filesDownloadTotal;
                        if (tabledataQueueFile.getDownloadFileTask().downloadStatus == DataDownloadFileTask.DownloadStatus.FINISHED)
                            ++filesDownloadFinished;
                    }

                    // Upload
                    if(tabledataQueueFile.getType() == TabledataQueueFile.Type.UPLOAD) {
                        uploadTotal += tabledataQueueFile.getUploadFileTask().getFileSizeTotal();
                        uploadCurrent += tabledataQueueFile.getUploadFileTask().getFileSizeCurrent();

                        ++filesUploadTotal;
                        if (tabledataQueueFile.getUploadFileTask().uploadStatus == DataUploadFileTask.UploadStatus.FINISHED)
                            ++filesUploadFinished;
                    }
                }

                // Download
                int finalFilesDownloadFinished = filesDownloadFinished;
                int finalFilesDownloadTotal = filesDownloadTotal;
                long finalDownloadCurrent = downloadCurrent;
                long finalDownloadTotal = downloadTotal;
                double downloadFinishedPercentage = (double)finalDownloadCurrent / (double)finalDownloadTotal;

                // Upload
                int finalFilesUploadFinished = filesUploadFinished;
                int finalFilesUploadTotal = filesUploadTotal;
                long finalUploadCurrent = uploadCurrent;
                long finalUploadTotal = uploadTotal;
                double uploadFinishedPercentage = (double)finalUploadCurrent / (double)finalUploadTotal;

                Platform.runLater( () -> {
                    // TODO Use observable properties and bind to progress bar

                    // Download
                    if(finalFilesDownloadTotal == 0) {
                        labelDownloadUpper.setText("No download files");
                        labelDownloadLower.setText("");
                        progressBarDownloadProgressProperty.set(0);
                    } else {
                        labelDownloadUpper.setText(String.valueOf(finalFilesDownloadFinished) + " of " + finalFilesDownloadTotal + " files downloaded " + "(" + String.format("%.0f%%", downloadFinishedPercentage * 100) + ")");
                        labelDownloadLower.setText(FileUtility.getPrintableSize(finalDownloadCurrent) + " of " + FileUtility.getPrintableSize(finalDownloadTotal));
                        progressBarDownloadProgressProperty.set(downloadFinishedPercentage);
                    }

                    // Upload
                    if(finalFilesUploadTotal == 0) {
                        labelUploadUpper.setText("No upload files");
                        labelUploadLower.setText("");
                        progressBarUploadProgressProperty.set(0);
                    } else {
                        labelUploadUpper.setText(String.valueOf(finalFilesUploadFinished) + " of " + finalFilesUploadTotal + " files uploaded " + "(" + String.format("%.0f%%", uploadFinishedPercentage * 100) + ")");
                        labelUploadLower.setText(FileUtility.getPrintableSize(finalUploadCurrent) + " of " + FileUtility.getPrintableSize(finalUploadTotal));
                        progressBarUploadProgressProperty.set(uploadFinishedPercentage);
                    }

                });

            }
        }, 0, Constants.GUI_UPDATE_INTERVAL);
    }

    public void btnAddOwnFiles(ActionEvent actionEvent) {
        // Standard choose directory (for easier testing)
        File chooseDirectory = new File("E:\\Musik\\Ori and the Blind Forest (Original Soundtrack)");
        if(!chooseDirectory.isDirectory()) chooseDirectory = new File(System.getProperty("user.dir"));

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Add Files To Offer");
        fileChooser.setInitialDirectory(chooseDirectory);
        List<File> files = fileChooser.showOpenMultipleDialog(Main.currentStage);

        if(files != null) {
            for (File file : files) {
                TabledataOwnOfferedFile tabledataOwnOfferedFile = new TabledataOwnOfferedFile(file);

                // Check for duplicate
                if(fileExistsInTabledataOwnOfferedFiles(tabledataOwnOfferedFile)) {
                    System.out.println("file is duplicate");
                    continue;
                }

                System.out.println("Added file: Filename: " + file.getName() + ", Size: " + FileUtility.getPrintableSize(tabledataOwnOfferedFile.getSize()) + ", Sha256 Hash: " + tabledataOwnOfferedFile.getHash());
                tabledataOwnOfferedFilesList.add(tabledataOwnOfferedFile);
                sendOwnOfferedFileAvailable(tabledataOwnOfferedFile);
            }
        }
    }

    private boolean fileExistsInTabledataOwnOfferedFiles(TabledataOwnOfferedFile fileToCheck) {
        //TODO use a hash set instead of iterating over the list
        for(TabledataOwnOfferedFile existingFile : tabledataOwnOfferedFilesList)
            if(existingFile.getHash().equals(fileToCheck.getHash()))
                return true;
        return false;
    }

    private void sendOwnOfferedFileAvailable(TabledataOwnOfferedFile ownOfferedFile) {
        DataCommandTask fileAvailableTask = DataCommandTask.getFileAvailableTask(ownOfferedFile.getFileName(), ownOfferedFile.getHash(), ownOfferedFile.getSize());

        for (DataConnectedComputer connection : connections) {
            connection.sendCommand(fileAvailableTask);
        }
    }

    public void btnRemoveOwnFiles(ActionEvent actionEvent) {
        List<TabledataOwnOfferedFile> filesToDelete = new ArrayList<>();

        for (TabledataOwnOfferedFile next : tabledataOwnOfferedFilesList) {
            if (next.getSelected()) {
                filesToDelete.add(next);

                for (DataConnectedComputer connection : connections) {
                    DataCommandTask removeFileTask = DataCommandTask.getFileRemoveTask(next.getHash());
                    connection.sendCommand(removeFileTask);
                }
            }
        }

        filesToDelete.forEach(tabledataOwnOfferedFilesList::remove);
    }

    public void btnCopyIpv4(ActionEvent actionEvent) {
        // TODO
        for (TabledataOwnOfferedFile ownFile : tabledataOwnOfferedFilesList) {
            System.out.println("Filename: " + ownFile.getFileName() + ", is selected: " + ownFile.getSelected());
        }
        System.out.println("-------------------------------------");
    }

    public void btnCopyIpv6(ActionEvent actionEvent) {
        // TODO
    }

    public void btnCopyPort(ActionEvent actionEvent) {
        // TODO
    }

    public void nicknameChanged() {
        for (DataConnectedComputer connection : connections) {
            connection.sendCommand(DataCommandTask.getNameChangedTask(tfNickname.getText()));
        }
    }

    public void btnDownloadSelected(ActionEvent actionEvent) {

        List<TabledataOfferedFile> deleteOfferedFiles = new ArrayList<>();

        for (TabledataOfferedFile offeredFile : tabledataOfferedFilesList) {
            if(offeredFile.getSelected()) {
                deleteOfferedFiles.add(offeredFile);
                TabledataQueueFile downloadFile = new TabledataQueueFile(offeredFile.getFileName(), "Waiting", "-", offeredFile.getConnectedComputer(), TabledataQueueFile.Type.DOWNLOAD, offeredFile.getHash());
                tabledataQueueFileList.add(downloadFile);

                // Send to Uploader that we want to download the file
                DataCommandTask fileDownloadAnnounceTask = DataCommandTask.getFileDownloadAnnounceTask(offeredFile.getHash());
                offeredFile.getConnectedComputer().sendCommand(fileDownloadAnnounceTask);

                // Add it to our own download task
                DataDownloadFileTask dataDownloadFileTask = new DataDownloadFileTask(offeredFile, downloadFile);
                offeredFile.getConnectedComputer().addDownload(dataDownloadFileTask);
                downloadFile.setDownloadFileTask(dataDownloadFileTask);
            }
        }

        tabledataOfferedFilesList.removeAll(deleteOfferedFiles);
    }

    public void btnChooseDownloadFolder(ActionEvent actionEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(downloadDirectory);
        File selectedDirectory = chooser.showDialog(Main.currentStage);

        if(selectedDirectory != null) {
            downloadDirectory = selectedDirectory;
            Platform.runLater(() -> labelDownloadDirectory.setText(downloadDirectory.getAbsolutePath()));
        }
    }

    public void tfDownloadLimitChanged(Event event) {
        try {
            long downloadLimit = Long.valueOf(tfDownloadLimit.getText());
            tfDownloadLimit.setStyle("-fx-control-inner-background: #FFFFFF");
            downloadBandwidthLimitInformer.setLimitPerSecond(downloadLimit * 1024);
            connections.forEach( connectedComputer -> connectedComputer.sendCommand(DataCommandTask.getTransferLimitTask(downloadLimit * 1024)));
        } catch (NumberFormatException e) {
            tfDownloadLimit.setStyle("-fx-control-inner-background: #FFB0B0");
        }
    }

    public void tfUploadLimitChanged(Event event) {
        try {
            long uploadLimit = Long.valueOf(tfUploadLimit.getText());
            tfUploadLimit.setStyle("-fx-control-inner-background: #FFFFFF");
            uploadBandwidthLimitInformer.setLimitPerSecond(uploadLimit * 1024);
        } catch (NumberFormatException e) {
            tfUploadLimit.setStyle("-fx-control-inner-background: #FFB0B0");
        }
    }

    public void btnDownloadPauseSelected(ActionEvent actionEvent) {
    }

    public void btnDownloadRemoveSelected(ActionEvent actionEvent) {
        Iterator<TabledataQueueFile> iterator = tabledataQueueFileList.iterator();

        while(iterator.hasNext()) {
            TabledataQueueFile queueFile = iterator.next();
            if(queueFile.getSelected()) {
                iterator.remove();

                DataCommandTask fileCancelTask = DataCommandTask.getFileDownloadCancelTask(queueFile.getHash());
                queueFile.getConnectedComputer().sendCommand(fileCancelTask);
            }
        }
    }

    public void btnMoreSettings(ActionEvent actionEvent) {
    }

    public void btnPauseAll(ActionEvent actionEvent) {
    }


    public void removeOfferedFile(DataConnectedComputer connectedComputer, String hash) {
        List<TabledataOfferedFile> removeFiles = new ArrayList<>();

        for (TabledataOfferedFile offeredFile : tabledataOfferedFilesList) {
            if(offeredFile.getConnectedComputer().equals(connectedComputer) && offeredFile.getHash().equals(hash)) {
                removeFiles.add(offeredFile);
            }
        }

        tabledataOfferedFilesList.removeAll(removeFiles);
    }

    public void removeAllOfferedFiles(DataConnectedComputer connectedComputer) {
        List<TabledataOfferedFile> removeFiles = new ArrayList<>();

        for (TabledataOfferedFile offeredFile : tabledataOfferedFilesList) {
            if(offeredFile.getConnectedComputer().equals(connectedComputer)) {
                removeFiles.add(offeredFile);
            }
        }

        tabledataOfferedFilesList.removeAll(removeFiles);
    }

    public void sendAllOfferedFiles(DataConnectedComputer connection) {
        for (TabledataOwnOfferedFile ownOfferedFile : tabledataOwnOfferedFilesList) {
            DataCommandTask fileAvailableTask = DataCommandTask.getFileAvailableTask(ownOfferedFile.getFileName(), ownOfferedFile.getHash(), ownOfferedFile.getSize());

            connection.sendCommand(fileAvailableTask);
        }
    }

    public void removeQueuedUploadFile(String hash) {

        /*
        Iterator<TabledataQueueFile> iterator = tabledataQueueFileList.iterator();

        while(iterator.hasNext()) {
            TabledataQueueFile file = iterator.next();
            if(file.getHash().equals(hash) && file.getType() == TabledataQueueFile.Type.UPLOAD) {
                iterator.remove();
            }
        }*/

        tabledataQueueFileList.removeIf(file -> file.getHash().equals(hash) && file.getType() == TabledataQueueFile.Type.UPLOAD);
    }
}

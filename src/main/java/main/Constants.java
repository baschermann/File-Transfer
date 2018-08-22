package main;


public class Constants {

    public static final byte IDENTIFIER_COMMAND = 0;
    public static final byte IDENTIFIER_DATA = 1;

    public static final byte COMMAND_PING = 0;
    public static final byte COMMAND_FILE_AVAILABLE = 1;
    public static final byte COMMAND_FILE_REMOVE = 2;
    public static final byte COMMAND_NAME_CHANGE = 3;
    public static final byte COMMAND_FILE_DOWNLOAD_ANNOUNCE = 4;
    public static final byte COMMAND_FILE_DOWNLOAD_ACCEPT = 5;
    public static final byte COMMAND_FILE_DOWNLOAD_PAUSE = 6;
    public static final byte COMMAND_FILE_DOWNLOAD_CANCEL = 7;
    public static final byte COMMAND_FILE_UPLOAD_START = 8;
    public static final byte COMMAND_FILE_UPLOAD_PAUSE = 9;
    public static final byte COMMAND_FILE_UPLOAD_CANCEL = 10;
    public static final byte COMMAND_TRANSFER_LIMIT = 11;

    /* Protocol
    1. Downloader - 'Announce' the want to download file
    2. Uploader - 'Start' the download, but don't send yet
    3. Downloader - 'Accept' the download, Uploader starts sending
    4. Downloader or Uploader can 'Pause'
    5. Downloader or Uploader can 'Cancel'
    */

    public static final long GUI_UPDATE_INTERVAL = 400; // in milliseconds
}

package main.file;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtility {

    private static MessageDigest md;

    static {
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static DigestInformation Sha256(File file) {
        String hash = null;
        long size = 0;

        if(file.isFile()) {

            try {
                FileInputStream fis = new FileInputStream(file);
                byte[] dataBytes = new byte[8192];

                int nread;
                while((nread = fis.read(dataBytes)) != -1) {
                    md.update(dataBytes, 0, nread);
                    size += nread;
                }
                fis.close();
                byte[] mdbytes = md.digest();


                StringBuffer sb = new StringBuffer();
                for(int i=0; i<mdbytes.length; i++) {
                    sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
                }

                hash = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("File is not a file " + file.getName());
        }

        return new DigestInformation(hash, size);
    }

    public static String getPrintableSize(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = ("kMGTPE").charAt(exp-1);
        return String.format("%.2f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
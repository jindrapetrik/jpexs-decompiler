package com.jpexs.proxy;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

/**
 * Replacement of URL with local file
 */
public class Replacement {

    /**
     * URL pattern, can contain * wild-cards
     */
    public String urlPattern;
    /**
     * Filename to replace content with
     */
    public String targetFile;
    /**
     * Date of last accesing this url
     */
    public Calendar lastAccess;

    /**
     * Constructor
     *
     * @param urlPattern URL pattern, can contain * wild-cards
     * @param targetFile Filename to replace content with
     */
    public Replacement(String urlPattern, String targetFile) {
        this.urlPattern = urlPattern;
        this.targetFile = targetFile;
    }

    /**
     * Returns true when urlPattern matches specified url
     *
     * @param url Url to test match
     * @return True when matches
     */
    public boolean matches(String url) {
        String pat = Pattern.quote(urlPattern);
        pat = pat.replace("*", "\\E.*\\Q");
        return Pattern.matches(pat, url);
    }

    private static String byteCountStr(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

        long size = new File(targetFile).length();
        String sizeS = byteCountStr(size, false);
        while (sizeS.length() < 12) {
            sizeS = " " + sizeS;
        }

        if (lastAccess == null) {
            return "        " + " | " + sizeS + " | " + urlPattern;
        } else {
            return format.format(lastAccess.getTime()) + " | " + sizeS + " | " + urlPattern;
        }
    }
}

package com.jpexs.proxy;

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

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

        if (lastAccess == null) {
            return "         " + urlPattern;
        } else {
            return format.format(lastAccess.getTime()) + " " + urlPattern;
        }
    }
}

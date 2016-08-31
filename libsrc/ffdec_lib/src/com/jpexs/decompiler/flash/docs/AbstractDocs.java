package com.jpexs.decompiler.flash.docs;

import com.jpexs.decompiler.flash.ApplicationInfo;
import static com.jpexs.decompiler.flash.docs.As3PCodeOtherDocs.NEWLINE;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AbstractDocs {

    protected static Cache<String, String> docsCache = Cache.getInstance(false, true, "abstractDocsCache");

    protected static String htmlFooter() {
        StringBuilder sb = new StringBuilder();

        sb.append("</html>");
        return sb.toString();
    }

    public static String getStyle() {
        String cached = docsCache.get("__style");
        if (cached != null) {
            return cached;
        }
        String style = "";
        InputStream is = As3PCodeDocs.class.getResourceAsStream("/com/jpexs/decompiler/flash/docs/docs.css");
        if (is == null) {
            Logger.getLogger(As3PCodeDocs.class.getName()).log(Level.SEVERE, "docs.css needed for documentation not found");
        } else {
            style = new String(Helper.readStream(is), Utf8Helper.charset);
        }

        docsCache.put("__style", style);
        return style;
    }

    protected static String meta(String name, String content) {
        return "\t\t<meta name=\"" + name + "\" content=\"" + content + "\">" + NEWLINE;
    }

    protected static String metaProp(String name, String content) {
        return "\t\t<meta property=\"" + name + "\" content=\"" + content + "\">" + NEWLINE;
    }

    protected static String meta(String name, Date content) {
        return "\t\t<meta name=\"" + name + "\" content=\"" + getISO8601StringForDate(content) + "\">" + NEWLINE;
    }

    protected static String getISO8601StringForDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

}

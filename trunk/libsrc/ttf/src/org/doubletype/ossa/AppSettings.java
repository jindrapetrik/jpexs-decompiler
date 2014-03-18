/*
 */
package org.doubletype.ossa;

import java.io.*;
import java.util.Properties;

/**
 * @author e.e
 */
public class AppSettings extends Object {
    private static String k_lastTypefaceFile = "lastTypefaceFile";
    
    private static File s_fileName = new File("./.properties");
    private static Properties s_properties;
    
    public static String getLastTypefaceDir() {
        loadPropertyFile();
        return s_properties.getProperty(k_lastTypefaceFile, "./");
    }
    
    public static void setLastTypefaceDir(String a_value) {
        loadPropertyFile();
        s_properties.setProperty(k_lastTypefaceFile, a_value);
        savePropertyFile();
    }
    
    private static void loadPropertyFile() {
        if (s_properties != null) {
            return;
        } // if
        
	    s_properties = new Properties();
	    if (s_fileName.exists()) {
	        try {
	            s_properties.load(new FileInputStream(s_fileName));
	        } catch (IOException e) {
	            e.printStackTrace();
	        } // try-catch
	    } // if
	}
    
    private static void savePropertyFile() {
        if (s_properties == null) {
            return;
        } // if
        
        try {
            s_properties.store(new FileOutputStream(s_fileName), 
                    "DoubleType AppSetting File");
        } catch (IOException e) {
            e.printStackTrace();
        } // try-catch
    }
}

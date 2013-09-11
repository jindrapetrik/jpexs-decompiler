package com.jpexs.browsers.cache.chrome;

import com.jpexs.browsers.cache.CacheEntry;
import com.jpexs.browsers.cache.CacheImplementation;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class ChromeCache implements CacheImplementation {

    private static ChromeCache instance;
    private File tempDir;
    private List<File> dataFiles;
    private File indexFile;

    private ChromeCache() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                free();
            }
        });
    }

    public static ChromeCache getInstance() {
        if (instance == null) {
            instance = new ChromeCache();
        }
        return instance;
    }
    private boolean loaded = false;
    private Index index;

    @Override
    public List<CacheEntry> getEntries() {
        if (!loaded) {
            refresh();
        }
        if (!loaded) {
            return null;
        }
        List<CacheEntry> ret = new ArrayList<>();
        try {
            List<EntryStore> entries = index.getEntries();
            for (EntryStore en : entries) {
                if (en.state == EntryStore.ENTRY_NORMAL) {
                    String key = en.getKey();
                    if (key != null && !key.trim().equals("")) {
                        ret.add(en);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ChromeCache.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return ret;
    }

    @Override
    public void refresh() {
        free();
        File cacheDir = null;
        try {
            cacheDir = getCacheDirectory();
        } catch (IOException ex) {
            Logger.getLogger(ChromeCache.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (cacheDir == null) {
            return;
        }
        File systemTempDir = new File(System.getProperty("java.io.tmpdir"));
        File originalIndexFile = new File(cacheDir + File.separator + "index");
        tempDir = new File(systemTempDir, "cacheView" + System.identityHashCode(this));
        tempDir.mkdir();
        indexFile = new File(tempDir, "index");
        try {
            Files.copy(originalIndexFile.toPath(), indexFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            Logger.getLogger(ChromeCache.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        File originalDataFile;
        dataFiles = new ArrayList<>();
        for (int i = 0; (originalDataFile = new File(cacheDir, "data_" + i)).exists(); i++) {
            File dataFile = new File(tempDir, "data_" + i);
            dataFiles.add(dataFile);
            try {
                Files.copy(originalDataFile.toPath(), dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                Logger.getLogger(ChromeCache.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
        }
        try {
            index = new Index(indexFile, cacheDir);
        } catch (IOException ex) {
            Logger.getLogger(ChromeCache.class.getName()).log(Level.SEVERE, null, ex);
        }
        loaded = true;
    }

    private enum OSId {

        WINDOWS, OSX, UNIX
    }

    private static OSId getOSId() {
        PrivilegedAction<String> doGetOSName = new PrivilegedAction<String>() {
            @Override
            public String run() {
                return System.getProperty("os.name");
            }
        };
        OSId id = OSId.UNIX;
        String osName = AccessController.doPrivileged(doGetOSName);
        if (osName != null) {
            if (osName.toLowerCase().startsWith("mac os x")) {
                id = OSId.OSX;
            } else if (osName.contains("Windows")) {
                id = OSId.WINDOWS;
            }
        }
        return id;
    }

    public static File getCacheDirectory() throws IOException {
        String userHome = null;
        File ret = null;
        try {
            userHome = System.getProperty("user.home");
        } catch (SecurityException ignore) {
        }
        if (userHome != null) {
            OSId osId = getOSId();
            if (osId == OSId.WINDOWS) {
                ret = new File(userHome + "\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Cache\\");
                if (!ret.exists()) {
                    ret = new File(userHome + "\\Local Settings\\Application Data\\Google\\Chrome\\User Data\\Default\\Cache");
                }
            } else if (osId == OSId.OSX) {
                ret = new File(userHome + "/Library/Caches/Google/Chrome/Default/Cache");
            } else {
                ret = new File(userHome + "/.config/google-chrome/Default/Application Cache/Cache/");
                if (!ret.exists()) {
                    ret = new File(userHome + "/.cache/chromium/Default/Cache");
                }
            }
        }
        if ((ret != null) && !ret.exists()) {
            return null;
        }
        return ret;

    }

    private void free() {
        if (loaded) {
            index.free();
            indexFile.delete();
            for (File d : dataFiles) {
                d.delete();
            }
            tempDir.delete();
        }
    }
}

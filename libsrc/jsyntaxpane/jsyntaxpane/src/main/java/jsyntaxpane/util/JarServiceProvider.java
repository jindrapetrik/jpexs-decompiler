/*
 * Copyright 2008 Ayman Al-Sairafi ayman.alsairafi@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License 
 *       at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 */
package jsyntaxpane.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author subwiz
 * @author Ayman Al-Sairafi
 */
public class JarServiceProvider {

    public static final String SERVICES_ROOT = "META-INF/services/";
    private static final Logger LOG = Logger.getLogger(JarServiceProvider.class.getName());

    /**
     * Prevent anyone from instantiating this class. Just use the static method
     */
    private JarServiceProvider() {
    }

    private static ClassLoader getClassLoader() {
        ClassLoader cl = JarServiceProvider.class.getClassLoader();
        return cl == null ? ClassLoader.getSystemClassLoader() : cl;
    }

    /**
     * Return an Object array from the file in META-INF/resources/{classname}
     *
     * @param cls
     * @return
     * @throws java.io.IOException
     */
    public static List<Object> getServiceProviders(Class cls) throws IOException {
        ArrayList<Object> l = new ArrayList<Object>();
        ClassLoader cl = getClassLoader();
        String serviceFile = SERVICES_ROOT + cls.getName();
        Enumeration<URL> e = cl.getResources(serviceFile);
        while (e.hasMoreElements()) {
            URL u = e.nextElement();
            InputStream is = u.openStream();
            BufferedReader br = null;
            try {
                br = new BufferedReader(
                        new InputStreamReader(is, Charset.forName("UTF-8")));
                String str = null;
                while ((str = br.readLine()) != null) {
                    int commentStartIdx = str.indexOf("#");
                    if (commentStartIdx != -1) {
                        str = str.substring(0, commentStartIdx);
                    }
                    str = str.trim();
                    if (str.length() == 0) {
                        continue;
                    }
                    try {
                        Object obj = cl.loadClass(str).newInstance();
                        l.add(obj);
                    } catch (Exception ex) {
                        LOG.warning("Could not load: " + str);
                        LOG.warning(ex.getMessage());
                    }
                }
            } finally {
                if (br != null) {
                    br.close();
                }
            }
        }
        return l;
    }

    /**
     * Read a file in the META-INF/services location. File name will be fully
     * qualified classname, in all lower-case, appended with ".properties" If no
     * file is found, then an empty Property instance will be returned
     *
     * @param clazz
     * @return Property file read.
     */
    public static Properties readProperties(Class clazz) {
        return readProperties(clazz.getName());
    }

    /**
     * Read a file in the META-INF/services named name appended with
     * ".properties"
     *
     * If no file is found, then an empty Property instance will be returned
     *
     * @param name name of file (use dots to separate subfolders).
     * @return Property file read.
     */
    public static Properties readProperties(String name) {
        Properties props = new Properties();       
        String serviceFile = name.toLowerCase();
        // JPEXS: Added locale support for menu actions
        String propSuffix = ".properties";
        if (serviceFile.endsWith(propSuffix)) {
            serviceFile = serviceFile.substring(0, serviceFile.length() - propSuffix.length());
        }
        Locale defaultLocale = Locale.getDefault();
        String langName = defaultLocale.getLanguage();
        InputStream is = null;
        is = findResource(serviceFile + "_" + langName + propSuffix);
        if (is == null) {
            is = findResource(serviceFile + propSuffix);
        }
        if (is != null) {
            try {
                props.load(is);
            } catch (IOException ex) {
                Logger.getLogger(JarServiceProvider.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return props;
    }

    /**
     * Read a file in the META-INF/services named name appended with
     * ".properties", and returns it as a
     * <code>Map<String, String></code> If no file is found, then an empty
     * Property instance will be returned
     *
     * @param name name of file (use dots to separate subfolders).
     * @return Map of keys and values
     */
    public static Map<String, String> readStringsMap(String name) {
        Properties props = readProperties(name);
        HashMap<String, String> map = new HashMap<String, String>();
        if (props != null) {
            for (Map.Entry e : props.entrySet()) {
                map.put(e.getKey().toString(), e.getValue().toString());
            }
        }
        return map;
    }

    /**
     * Read the given URL and returns a List of Strings for each input line Each
     * line will not have the line terminator.
     *
     * The resource is searched in /META-INF/services/url, then in url, then the
     * url is treated as a location in the current classpath and an attempt to
     * read it from that location is done.
     *
     * @param url location of file to read
     * @return List of Strings for each line read. or EMPTY_LIST if URL is not
     * found
     */
    @SuppressWarnings("unchecked")
    public static List<String> readLines(String url) {
        InputStream is = findResource(url);
        if (is == null) {
            return Collections.EMPTY_LIST;
        }
        List<String> lines = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                // Trim and unescape some control chars
                line = line.trim().replace("\\n", "\n").replace("\\t", "\t");
                lines.add(line);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            return lines;
        }

    }

    /**
     * Attempt to find a location url. The following locations are searched in
     * sequence: url, SERVICES_ROOT/url all classpath/url
     *
     * @param url
     * @param cl classloader
     * @return InputSTream at that location, or null if not found
     * @see JarServiceProvider#findResource(java.lang.String)
     */
    public static InputStream findResource(String url, ClassLoader cl) {
        InputStream is = null;

        URL loc = cl.getResource(url);
        if (loc == null) {
            loc = cl.getResource(url);
        }
        if (loc == null) {
            loc = cl.getResource(SERVICES_ROOT + url);
        }
        if (loc == null) {
            is = ClassLoader.getSystemResourceAsStream(url);
        } else {
            try {
                is = loc.openStream();
            } catch (IOException ex) {
                Logger.getLogger(JarServiceProvider.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return is;
    }

    /**
     * Attempt to find a location url. The following locations are searched in
     * sequence: url, SERVICES_ROOT/url all classpath/url The System ClassLoader
     * is used.
     *
     * @param url
     * @return InputSTream at that location, or null if not found
     * @see JarServiceProvider#findResource(java.lang.String,
     * java.lang.ClassLoader)
     */
    public static InputStream findResource(String url) {
        return findResource(url, getClassLoader());
    }
}

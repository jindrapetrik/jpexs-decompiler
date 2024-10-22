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

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper around the Properties class with support for Hierarchical configurations
 * and more functionality.
 *
 * Except for the getXXXX methods, all other Map Interface methods operate on the
 * current (non-parent) collection and do NOT touch the parent.
 * 
 * @author Ayman Al-Sairafi
 */
public class Configuration implements Map<String, String> {

    /**
     * Our parent
     */
    Configuration parent;
    /**
     * Our Class for the configuration
     */
    Class clazz;
    /**
     * The properties we have, excluding the parents defaults
     */
    Map<String, String> props;

    /**
     * Creates a new COnfiguration that uses parent as its parent
     * Configuration.
     *
     * @param theClass
     * @param parent
     */
    public Configuration(Class theClass, Configuration parent) {
        this(theClass);
        this.parent = parent;
    }

    /**
     * Creates an empty Configuration
     * @param theClass
     */
    public Configuration(Class theClass) {
        super();
        this.clazz = theClass;
    }

    /**
     * Get a string from this object or one of its parents.  If nothing
     * is found, null is returned.
     * If the Regex ${key} is found, then it is replaced by the value of that
     * key within this (or parent's) map.
     * Special COnstructs in ${}:
     * <li><code>class_path</code> will be replaced by the name of the
     * Configuration (usually ClassName) with "." replaced by "/", and then
     * converted to all lowercase</li>
     * <li><code>class_simpleName</code></li> is replaced by class.SimpleName
     * @param key
     * @return
     */
    public String getString(String key) {
        String value = null;
        if (props != null) {
            value = props.get(key);
        }
        for (Configuration p = parent; value == null && p != null; p = p.parent) {
            value = p.get(key);
        }
        // if we have a parent, then perform ${} replacements
        if (value != null) {
            Matcher m = PARENT_KEY.matcher(value);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String p_key = m.group(1);
                String p_value = getString(p_key);
                if (p_key.equals("class_path")) {
                    p_value = clazz.getName().replace(".", "/").toLowerCase();
                } else if (p_key.equals("class_simpleName")) {
                    p_value = clazz.getSimpleName();
                } else {
                    p_value = getString(p_key);
                    if (p_value == null) {
                        Logger.getLogger(this.getClass().getName()).warning(
                                "no value for ${" + p_key +
                                "} is defined");
                    }
                }
                m.appendReplacement(sb, p_value);
            }
            m.appendTail(sb);
            value = sb.toString();
        }
        return value;
    }

    /**
     * Returns a non-null value either by traversing the current
     * and parent(s) map, or returning the defaultValue
     * @param key
     * @param defaultValue
     * @throws NullPointerException if defaultValue is null
     * @return
     */
    public String getString(String key, String defaultValue) {
        if (defaultValue == null) {
            throw new NullPointerException("defaultValue cannot be null");
        }
        String value = getString(key);
        return (value == null) ? defaultValue : value;
    }

    /**
     * Gets an integer from the properties.  If number cannot be found
     * or if it cannot be decoded, the default is returned
     * The integer is decoded using {@link Integer.decode(String)}
     * @param key
     * @param Default
     * @return
     */
    public int getInteger(String key, int Default) {
        String v = getString(key);
        if (v == null) {
            return Default;
        }
        try {
            int i = Integer.decode(v);
            return i;
        } catch (NumberFormatException e) {
            LOG.log(Level.WARNING, null, e);
            return Default;
        }
    }

    /**
     * Returns a String[] of the comma separated items in the value.
     * 
     * Does NOT return null.  If the key is not found,
     * then an empty string array is returned.  So the return of this method
     * can always be used directly in a foreach loop
     * @param key
     * @return non-null String[] 
     */
    public String[] getPropertyList(String key) {
        String v = getString(key);
        if (v == null) {
            return EMPTY_LIST;
        } else {
            return COMMA_SEPARATOR.split(v);
        }
    }

    /**
     * Returns a boolean from the configuration
     * @param key
     * @param Default
     * @return
     */
    public boolean getBoolean(String key, boolean Default) {
        String b = getString(key);
        if (b == null) {
            return Default;
        }
        return Boolean.parseBoolean(b.trim());
    }

    /**
     * return the Color that has the given key or the Default
     * @param key
     * @param Default
     * @return
     */
    public Color getColor(String key, Color Default) {
        String c = getString(key);
        if (c == null) {
            return Default;
        } else {
            try {
                return Color.decode(c);
            } catch (NumberFormatException e) {
                return Default;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void putAll(Map config) {
        if (props == null) {
            props = new HashMap<String, String>();
        }
        props.putAll(config);
    }

    /**
     * Returns ALL property names from this Configuration's parents and
     * this Configuration.  As usual, parents are added first so they
     * are overridden by children.
     *
     * @return Set of all String keys in this and parents
     */
    public Set<String> stringPropertyNames() {
        Set<String> propNames = new HashSet<String>();
        if (parent != null) {
            propNames.addAll(parent.stringPropertyNames());
        }
        if (props != null) {
            for (Object k : props.keySet()) {
                propNames.add(k.toString());
            }
        }
        return propNames;
    }

    @Override
    public String put(String key, String value) {
        if (props == null) {
            props = new HashMap<String, String>();
        }
        Object old = props.put(key, value);
        return (old == null) ? null : old.toString();
    }

    @Override
    public int size() {
        return (props == null) ? 0 : props.size();
    }

    @Override
    public boolean isEmpty() {
        return (props == null) ? true : props.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return (props == null) ? false : props.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return (props == null) ? false : props.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return (props == null) ? null : props.get(key);
    }

    @Override
    public String remove(Object key) {
        if (props == null) {
            return null;
        }
        Object old = props.remove(key);
        return (old == null) ? null : old.toString();
    }

    @Override
    public void clear() {
        if (props != null) {
            props.clear();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> keySet() {
        if (props == null) {
            return Collections.EMPTY_SET;
        } else {
            return props.keySet();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<String> values() {
        if (props == null) {
            return Collections.EMPTY_SET;
        } else {
            return props.values();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Entry<String, String>> entrySet() {
        if (props == null) {
            return Collections.EMPTY_SET;
        } else {
            return props.entrySet();
        }
    }

    @Override
    public String toString() {
        return "Configuration " + clazz + " for " + parent;
    }

    /**
     * Utility class to hold data for {@link getKeys} method.
     */
    public static class StringKeyMatcher {

        private StringKeyMatcher(String key, Matcher matcher, String group1, String value) {
            this.key = key;
            this.matcher = matcher;
            this.group1 = group1;
            this.value = value;
        }
        /**
         * The full key matched
         */
        public final String key;
        /**
         * matcher instance for the key
         */
        public final Matcher matcher;
        /**
         * Matched group 1. Could be null if no Group 1 is found
         */
        public final String group1;
        /**
         * Value for key matched
         */
        public final String value;
    }

    /**
     * Obtain a set of all keys (and parent's keys) that match the given pattern.
     * If no keys match, then an empty set is returned.
     * Use this instead of the {@link stringPropertyNames}
     * @param pattern
     * @return
     */
    public Set<StringKeyMatcher> getKeys(Pattern pattern) {
        Set<StringKeyMatcher> matched = new HashSet<StringKeyMatcher>();
        Set<String> all = stringPropertyNames();
        for (String k : all) {
            Matcher m = pattern.matcher(k);
            if (m.matches()) {
                StringKeyMatcher skm = new StringKeyMatcher(k, m,
                        (m.groupCount() >= 1) ? m.group(1) : null,
                        getString(k));
                matched.add(skm);
            }
        }
        return matched;
    }
    public static final String[] EMPTY_LIST = new String[0];
    public static final Pattern COMMA_SEPARATOR = Pattern.compile("\\s*,\\s*");
    private static Pattern PARENT_KEY = Pattern.compile("\\$\\{(\\w+)\\}");
    private static final Logger LOG = Logger.getLogger(Configuration.class.getName());
}

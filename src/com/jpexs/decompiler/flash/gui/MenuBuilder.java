/*
 *  Copyright (C) 2010-2018 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Menu Builder. Creates menu.
 *
 * @author JPEXS
 */
public interface MenuBuilder {

    public static class HotKey {

        private static Map<Integer, String> keyCodesToNames = new HashMap<>();

        private static Map<String, Integer> keyNamesToCodes = new HashMap<>();

        {

            Field[] fields = KeyEvent.class.getFields();

            for (int i = 0; i < fields.length; i++) {

                String fieldName = fields[i].getName();

                // We only care about the field names corresponding to key codes
                if (fieldName.startsWith("VK")) {
                    try {
                        int keyCode = fields[i].getInt(null);
                        String keyName = fieldName.substring(3);
                        keyCodesToNames.put(keyCode, keyName);
                        keyNamesToCodes.put(keyName, keyCode);
                    } catch (Exception ex) {

                    }
                }
            }
        }

        public int key;

        public boolean shiftDown;

        public boolean ctrlDown;

        public boolean altDown;

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 41 * hash + this.key;
            hash = 41 * hash + (this.shiftDown ? 1 : 0);
            hash = 41 * hash + (this.ctrlDown ? 1 : 0);
            hash = 41 * hash + (this.altDown ? 1 : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final HotKey other = (HotKey) obj;
            if (this.key != other.key) {
                return false;
            }
            if (this.shiftDown != other.shiftDown) {
                return false;
            }
            if (this.ctrlDown != other.ctrlDown) {
                return false;
            }
            return (this.altDown == other.altDown);
        }

        public boolean matches(KeyEvent ev) {
            return ev.getKeyCode() == key && ev.isControlDown() == ctrlDown && ev.isShiftDown() == shiftDown && ev.isAltDown() == altDown;
        }

        public int getModifier() {
            return (shiftDown ? KeyEvent.SHIFT_MASK : 0) + (ctrlDown ? KeyEvent.CTRL_MASK : 0) + (altDown ? KeyEvent.ALT_MASK : 0);
        }

        public HotKey(String h) {
            String[] parts = h.contains("+") ? h.split("\\+") : new String[]{h};
            for (String s : parts) {
                switch (s) {
                    case "SHIFT":
                        shiftDown = true;
                        break;
                    case "CTRL":
                        ctrlDown = true;
                        break;
                    case "ALT":
                        altDown = true;
                        break;
                    default:
                        if (keyNamesToCodes.containsKey(s)) {
                            key = keyNamesToCodes.get(s);
                        } else {
                            throw new IllegalArgumentException("Key " + s + " not found!");
                        }
                }
            }
        }

        public HotKey(KeyEvent ev) {
            this(ev.getKeyCode(), ev.isShiftDown(), ev.isControlDown(), ev.isAltDown());
        }

        public HotKey(int key) {
            this(key, false, false, false);
        }

        public HotKey(int key, boolean shiftDown, boolean ctrlDown, boolean altDown) {
            this.key = key;
            this.shiftDown = shiftDown;
            this.ctrlDown = ctrlDown;
            this.altDown = altDown;
        }

        @Override
        public String toString() {
            String s = "";
            if (shiftDown) {
                s += "SHIFT";
            }
            if (ctrlDown) {
                if (!s.isEmpty()) {
                    s += "+";
                }
                s += "CTRL";
            }
            if (altDown) {
                if (!s.isEmpty()) {
                    s += "+";
                }
                s += "ALT";
            }
            if (!s.isEmpty()) {
                s += "+";
            }
            s += keyCodesToNames.get(key);

            return s;
        }
    }

    public static final int PRIORITY_LOW = 1;

    public static final int PRIORITY_MEDIUM = 2;

    public static final int PRIORITY_TOP = 3;

    /**
     * Initializes menuBuilder
     */
    public void initMenu();

    /**
     * Adds separator
     *
     * @param parentPath Parent menu path
     */
    public void addSeparator(String parentPath);

    /**
     * Adds menu item
     *
     * @param path Path
     * @param title Title
     * @param icon Icon - resource name
     * @param action Action for clicking
     * @param priority Priority
     * @param subloader Action which loads menu inside
     * @param isLeaf Has no subitems?
     * @param key
     * @param isOptional
     */
    public void addMenuItem(String path, String title, String icon, ActionListener action, int priority, ActionListener subloader, boolean isLeaf, HotKey key, boolean isOptional);

    /**
     * Adds toggle item (radio/checkbox)
     *
     * @param path Path
     * @param title Title
     * @param group Group for toggling. Null = checkbox
     * @param icon Icon - resource name
     * @param action Action for clicking
     * @param priority Priority
     * @param key
     */
    public void addToggleMenuItem(String path, String title, String group, String icon, ActionListener action, int priority, HotKey key);

    /**
     * Test menu checked (toggle)
     *
     * @param path Menu path
     * @return True when checked
     */
    public boolean isMenuChecked(String path);

    /**
     * Hotkey for menu
     *
     * @param path Menu path
     * @return
     */
    public HotKey getMenuHotkey(String path);

    /**
     * Sets menu checked (toggle)
     *
     * @param path Menu path
     * @param checked Checked?
     */
    public void setMenuChecked(String path, boolean checked);

    /**
     * Sets checked item from group
     *
     * @param group Group name
     * @param selected Selected path
     */
    public void setGroupSelection(String group, String selected);

    /**
     * Gets selected path from group
     *
     * @param group Group name
     * @return Path
     */
    public String getGroupSelection(String group);

    /**
     * Clears menu
     *
     * @param path Path of menu
     */
    public void clearMenu(String path);

    /**
     * Sets enabled property of menu
     *
     * @param path Menu path
     * @param enabled Enabled?
     */
    public void setMenuEnabled(String path, boolean enabled);

    /**
     * Finished creating menu
     *
     * @param path Menu path
     */
    public void finishMenu(String path);

    /**
     * Does this builder support actions for menus? (not menuitems)
     *
     * @return True if supports
     */
    public boolean supportsMenuAction();

    /**
     * Does this builder support application menu (Path "_/...")
     *
     * @return True if supports
     */
    public boolean supportsAppMenu();
}

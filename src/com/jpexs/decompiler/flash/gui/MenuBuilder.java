/*
 * Copyright (C) 2015 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import java.awt.event.ActionListener;

/**
 * Menu Builder. Creates menu.
 *
 * @author JPEXS
 */
public interface MenuBuilder {

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
     */
    public void addMenuItem(String path, String title, String icon, ActionListener action, int priority, ActionListener subloader, boolean isLeaf);

    /**
     * Adds toggle item (radio/checkbox)
     *
     * @param path Path
     * @param title Title
     * @param group Group for toggling. Null = checkbox
     * @param icon Icon - resource name
     * @param action Action for clicking
     * @param priority Priority
     */
    public void addToggleMenuItem(String path, String title, String group, String icon, ActionListener action, int priority);

    /**
     * Test menu checked (toggle)
     *
     * @param path Menu path
     * @return True when checked
     */
    public boolean isMenuChecked(String path);

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

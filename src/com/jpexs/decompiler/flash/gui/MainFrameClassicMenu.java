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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 *
 * @author JPEXS
 */
public class MainFrameClassicMenu extends MainFrameMenu {

    private final MainFrameClassic mainFrame;

    private final Map<String, MenuElement> menuElements = new HashMap<>();

    private final Map<String, Set<String>> menuGroups = new HashMap<>();

    private final Map<String, ButtonGroup> menuButtonGroups = new HashMap<>();

    public MainFrameClassicMenu(MainFrameClassic mainFrame, boolean externalFlashPlayerUnavailable) {
        super(mainFrame, externalFlashPlayerUnavailable);
        this.mainFrame = mainFrame;

    }

    private void addMenu(String path, String title, String icon, final ActionListener subLoader) {
        path = mapping(path);
        final String fpath = path;
        if (path.equals("_") || path.startsWith("_/")) {
            return;
        }
        String parentPath = "";
        if (path.contains("/")) {
            parentPath = path.substring(0, path.lastIndexOf('/'));
        }
        MenuElement parentMenu = menuElements.get(parentPath);
        if (parentMenu == null) {
            throw new IllegalArgumentException("Parent menu " + path + " does not exist");
        }
        JMenu menu = new JMenu(title);
        if (icon != null) {
            menu.setIcon(View.getIcon(icon, 16));
        }
        if (parentMenu instanceof JMenuBar) {
            ((JMenuBar) parentMenu).add(menu);
        }
        if (parentMenu instanceof JMenu) {
            ((JMenu) parentMenu).add(menu);
            if (subLoader != null) {
                ((JMenu) parentMenu).addMenuListener(new MenuListener() {

                    @Override
                    public void menuSelected(MenuEvent e) {
                        subLoader.actionPerformed(new ActionEvent(menu, 0, "load:" + fpath));
                    }

                    @Override
                    public void menuDeselected(MenuEvent e) {
                    }

                    @Override
                    public void menuCanceled(MenuEvent e) {
                    }
                });
            };
        }
        menuElements.put(path, menu);
    }

    @Override
    public void addMenuItem(String path, String title, String icon, ActionListener action, int priority, final ActionListener subLoader, boolean isLeaf, HotKey key, boolean isOptional) {
        path = mapping(path);

        menuHotkeys.put(path, key);
        menuActions.put(path, action);

        if (!isLeaf) {
            //action is ignored
            addMenu(path, title, icon, subLoader);
            return;
        }
        if (path.startsWith("_/")) {
            return;
        }
        String parentPath = "";
        if (path.contains("/")) {
            parentPath = path.substring(0, path.lastIndexOf('/'));
        }
        MenuElement parentMenu = menuElements.get(parentPath);
        if (parentMenu == null) {
            throw new IllegalArgumentException("Parent menu " + path + " does not exist");
        }
        JMenuItem menuItem = new JMenuItem(title);
        if (icon != null) {
            menuItem.setIcon(View.getIcon(icon, 16));
        }
        if (action != null) {
            menuItem.addActionListener(action);
        }
        if (key != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(key.key, key.getModifier()));
        }
        if (parentMenu instanceof JMenu) {
            ((JMenu) parentMenu).add(menuItem);
        } else {
            throw new IllegalArgumentException("Parent path " + path + " is not a menu");
        }
        menuElements.put(path, menuItem);
    }

    @Override
    public void addToggleMenuItem(String path, String title, String group, String icon, ActionListener action, int priority, HotKey key) {
        path = mapping(path);
        menuHotkeys.put(path, key);

        String parentPath = "";
        if (path.contains("/")) {
            parentPath = path.substring(0, path.lastIndexOf('/'));
        }
        MenuElement parentMenu = menuElements.get(parentPath);
        if (parentMenu == null) {
            throw new IllegalArgumentException("Parent menu " + path + " does not exist");
        }
        JMenuItem menuItem;
        if (group == null) {
            menuItem = new JCheckBoxMenuItem(title);
        } else {
            menuItem = new JRadioButtonMenuItem(title);
            if (!menuGroups.containsKey(group)) {
                menuGroups.put(group, new HashSet<>());
            }
            if (!menuButtonGroups.containsKey(group)) {
                menuButtonGroups.put(group, new ButtonGroup());
            }
            menuGroups.get(group).add(path);
            menuButtonGroups.get(group).add(menuItem);
        }
        if (icon != null) {
            menuItem.setIcon(View.getIcon(icon, 16));
        }
        if (action != null) {
            menuItem.addActionListener(action);
        }
        if (parentMenu instanceof JMenu) {
            ((JMenu) parentMenu).add(menuItem);
        } else {
            throw new IllegalArgumentException("Parent path " + path + " is not a menu");
        }
        menuElements.put(path, menuItem);
    }

    @Override
    public String getGroupSelection(String group) {
        if (!menuGroups.containsKey(group)) {
            return null;
        }
        for (String path : menuGroups.get(group)) {
            if (isMenuChecked(path)) {
                return path;
            }
        }
        return null;
    }

    @Override
    public void clearMenu(String path) {
        path = mapping(path);
        if (path.equals("_") || path.startsWith("_/")) {
            return;
        }
        MenuElement menu = menuElements.get(path);
        if (menu == null) {
            throw new IllegalArgumentException("Menu " + path + " does not exist");
        }
        if (menu instanceof JMenuBar) {
            ((JMenuBar) menu).removeAll();
        } else if (menu instanceof JMenu) {
            ((JMenu) menu).removeAll();
        } else {
            throw new IllegalArgumentException(path + " is not a menu");
        }

    }

    @Override
    public void setMenuEnabled(String path, boolean enabled) {
        path = mapping(path);
        if (path.equals("_") || path.startsWith("_/")) {
            return;
        }
        MenuElement menu = menuElements.get(path);
        if (menu == null) {
            throw new IllegalArgumentException("Menu " + path + " does not exist");
        }
        if (menu instanceof JMenuBar) {
            ((JMenuBar) menu).setEnabled(enabled);
        } else if (menu instanceof JMenu) {
            ((JMenu) menu).setEnabled(enabled);
        } else if (menu instanceof JMenuItem) {
            ((JMenuItem) menu).setEnabled(enabled);
        } else {
            throw new IllegalArgumentException(path + " is not a menu");
        }
    }

    @Override
    public boolean isMenuChecked(String path) {
        path = mapping(path);
        MenuElement menu = menuElements.get(path);
        if (menu == null) {
            throw new IllegalArgumentException("Menu " + path + " does not exist");
        }
        if (menu instanceof JCheckBoxMenuItem) {
            return ((JCheckBoxMenuItem) menu).isSelected();
        } else if (menu instanceof JRadioButtonMenuItem) {
            return ((JRadioButtonMenuItem) menu).isSelected();
        } else {
            throw new IllegalArgumentException(path + " is not selectable menu item");
        }
    }

    @Override
    public void setMenuChecked(String path, boolean checked) {
        path = mapping(path);
        MenuElement menu = menuElements.get(path);
        if (menu == null) {
            throw new IllegalArgumentException("Menu " + path + " does not exist");
        }
        if (menu instanceof JCheckBoxMenuItem) {
            ((JCheckBoxMenuItem) menu).setSelected(checked);
        } else if (menu instanceof JRadioButtonMenuItem) {
            ((JRadioButtonMenuItem) menu).setSelected(checked);
        } else {
            throw new IllegalArgumentException(path + " is not selectable menu item");
        }
    }

    @Override
    public void setGroupSelection(String group, String selected) {
        selected = mapping(selected);
        for (String path : menuGroups.get(group)) {
            setMenuChecked(path, path.equals(selected));
        }
    }

    @Override
    public void initMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuElements.put("", menuBar);
        mainFrame.setJMenuBar(menuBar);
    }

    @Override
    public void finishMenu(String path) {
        path = mapping(path);
        if (path.equals("_") || path.startsWith("_/")) {
            return;
        }
        if (!menuElements.containsKey(path)) {
            throw new IllegalArgumentException("Invalid menu: " + path);
        }
        if (path.startsWith("/file/recent")) {
            return;
        }
        MenuElement me = menuElements.get(path);
        if (me instanceof JMenu) {
            JMenu jm = (JMenu) me;
            if (jm.getMenuComponentCount() == 1) {
                String parentPath = path.contains("/") ? path.substring(0, path.lastIndexOf('/')) : "";
                MenuElement parMe = menuElements.get(parentPath);
                JMenuItem mi = (JMenuItem) jm.getMenuComponent(0);
                jm.remove(mi);
                if (parMe instanceof JMenu) {
                    JMenu parMenu = (JMenu) parMe;
                    parMenu.remove(jm);
                    parMenu.add(mi);
                } else if (parMe instanceof JMenuBar) {
                    JMenuBar parMenuBar = (JMenuBar) parMe;
                    parMenuBar.remove(jm);
                    parMenuBar.add(mi);
                }
            }
        }
    }

    @Override
    public void addSeparator(String parentPath) {
        parentPath = mapping(parentPath);
        if (parentPath.equals("_") || parentPath.startsWith("_/")) {
            return;
        }
        if (!menuElements.containsKey(parentPath)) {
            throw new IllegalArgumentException("Menu does not exist: " + parentPath);
        }
        MenuElement parent = menuElements.get(parentPath);
        if (parent instanceof JMenu) {
            ((JMenu) parent).addSeparator();
        } else {
            throw new IllegalArgumentException("Not a menu: " + parentPath);
        }
    }

    @Override
    public boolean supportsMenuAction() {
        return false;
    }

    @Override
    public boolean supportsAppMenu() {
        return false;
    }

    /**
     * Maps some menus to other location
     *
     * @param s Source
     * @return To
     */
    private String mapping(String s) {
        Map<String, String> map = new HashMap<>();
        map.put("/file/view", "/view");

        for (String k : map.keySet()) {
            String v = map.get(k);
            if (s.startsWith(k)) {
                s = v + s.substring(k.length());
                return s;
            }
        }
        return s;
    }

    @Override
    public void hilightPath(String path) {
        //TODO
    }

    @Override
    public void setPathVisible(String path, boolean val) {
        MenuElement me = menuElements.get(path);
        if (me instanceof JComponent) {
            ((JComponent) me).setVisible(val);
        }
    }
}

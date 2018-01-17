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

import com.jpexs.decompiler.flash.configuration.Configuration;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.CommandButtonDisplayState;
import org.pushingpixels.flamingo.api.common.CommandToggleButtonGroup;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButtonPanel;
import org.pushingpixels.flamingo.api.common.JCommandToggleButton;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.AbstractRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbonComponent;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenu;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryFooter;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;
import org.pushingpixels.flamingo.api.ribbon.RibbonContextualTaskGroup;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;
import org.pushingpixels.flamingo.api.ribbon.resize.BaseRibbonBandResizePolicy;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.IconRibbonBandResizePolicy;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;
import org.pushingpixels.flamingo.internal.ui.ribbon.AbstractBandControlPanel;

/**
 *
 * @author JPEXS
 */
public class MainFrameRibbonMenu extends MainFrameMenu {

    private final JRibbon ribbon;

    private final Map<String, Object> menuItems = new HashMap<>();

    private final Map<String, String> menuTitles = new HashMap<>();

    private final Map<String, Boolean> menuOptional = new HashMap<>();

    private final Map<String, String> menuIcons = new HashMap<>();

    private final Map<String, ActionListener> menuLoaders = new HashMap<>();

    private final Map<String, Integer> menuPriorities = new HashMap<>();

    private final Map<String, List<String>> menuSubs = new HashMap<>();

    private final Map<String, Integer> menuType = new HashMap<>();

    private final Map<String, String> menuGroup = new HashMap<>();

    private final Map<String, CommandToggleButtonGroup> menuToggleGroups = new HashMap<>();

    private final Map<String, CommandToggleButtonGroup> menuToToggleGroup = new HashMap<>();

    private static final int TYPE_MENU = 1;

    private static final int TYPE_MENUITEM = 2;

    private static final int TYPE_TOGGLEMENUITEM = 3;

    private final Map<String, RibbonContextualTaskGroup> optionalGroups = new HashMap<>();

    public MainFrameRibbonMenu(MainFrameRibbon mainFrame, JRibbon ribbon, boolean externalFlashPlayerUnavailable) {
        super(mainFrame, externalFlashPlayerUnavailable);
        this.ribbon = ribbon;
    }

    private String fixCommandTitle(String title) {
        if (title.length() > 2) {
            if (title.charAt(1) == ' ') {
                title = title.charAt(0) + "\u00A0" + title.substring(2);
            }
        }
        return title;
    }

    private RibbonBandResizePolicy titleResizePolicies(final JRibbonBand ribbonBand) {
        return new BaseRibbonBandResizePolicy<AbstractBandControlPanel>(ribbonBand.getControlPanel()) {
            @Override
            public int getPreferredWidth(int i, int i1) {
                return ribbonBand.getGraphics().getFontMetrics(ribbonBand.getFont()).stringWidth(ribbonBand.getTitle()) + 20;
            }

            @Override
            public void install(int i, int i1) {
            }
        };
    }

    private List<RibbonBandResizePolicy> getResizePolicies(JRibbonBand ribbonBand) {
        final List<RibbonBandResizePolicy> myResizePolicies = new ArrayList<>();
        myResizePolicies.add(new CoreRibbonResizePolicies.Mirror(ribbonBand.getControlPanel()));
        myResizePolicies.add(titleResizePolicies(ribbonBand));
        myResizePolicies.add(new IconRibbonBandResizePolicy(ribbonBand.getControlPanel()));

        List<RibbonBandResizePolicy> resizePolicies = new ArrayList<>();

        resizePolicies.add(new RibbonBandResizePolicy() {

            @Override
            public int getPreferredWidth(int i, int i1) {
                int pw = 0;
                for (RibbonBandResizePolicy p : myResizePolicies) {
                    int npw = p.getPreferredWidth(i, i1);
                    if (npw > pw) {
                        pw = npw;
                    }
                }
                return pw;
            }

            @Override
            public void install(int i, int i1) {
                for (RibbonBandResizePolicy p : myResizePolicies) {
                    p.install(i, i1);
                }
            }
        });
        return resizePolicies;
    }

    @Override
    protected void loadRecent(ActionEvent evt) {
        if (evt.getSource() instanceof JPanel) {
            JPanel targetPanel = (JPanel) evt.getSource();
            targetPanel.removeAll();
            JCommandButtonPanel openHistoryPanel = new JCommandButtonPanel(CommandButtonDisplayState.MEDIUM);
            String groupName = translate("menu.recentFiles");
            openHistoryPanel.addButtonGroup(groupName);
            List<String> recentFiles = Configuration.getRecentFiles();
            int j = 0;
            for (int i = recentFiles.size() - 1; i >= 0; i--) {
                String path = recentFiles.get(i);
                RecentFilesButton historyButton = new RecentFilesButton(j + "    " + path, null);
                historyButton.fileName = path;
                historyButton.addActionListener((ActionEvent ae) -> {
                    RecentFilesButton source = (RecentFilesButton) ae.getSource();
                    if (Main.openFile(source.fileName, null) == OpenFileResult.NOT_FOUND) {
                        if (View.showConfirmDialog(null, translate("message.confirm.recentFileNotFound"), translate("message.confirm"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_NO_OPTION) {
                            Configuration.removeRecentFile(source.fileName);
                        }
                    }
                });
                j++;
                historyButton.setHorizontalAlignment(SwingUtilities.LEFT);
                openHistoryPanel.addButtonToLastGroup(historyButton);
            }

            if (recentFiles.isEmpty()) {
                JCommandButton emptyLabel = new JCommandButton(translate("menu.recentFiles.empty"));
                emptyLabel.setHorizontalAlignment(SwingUtilities.LEFT);
                emptyLabel.setEnabled(false);
                openHistoryPanel.addButtonToLastGroup(emptyLabel);
            }

            openHistoryPanel.setMaxButtonColumns(1);
            targetPanel.setLayout(new BorderLayout());
            targetPanel.add(openHistoryPanel, BorderLayout.CENTER);
        }
    }

    @Override
    public void finishMenu(String path) {
        if (!menuTitles.containsKey(path)) {
            throw new IllegalArgumentException("Menu not started: " + path);
        }
        boolean isAppMenu = path.equals("_");
        String title = menuTitles.get(path);
        String icon = menuIcons.get(path);
        ActionListener action = menuActions.get(path);
        int priority = menuPriorities.get(path);
        int type = menuType.get(path);
        if (type != TYPE_MENU) {
            throw new IllegalArgumentException("Not a menu: " + path);
        }

        String[] parts = path.contains("/") ? path.split("\\/") : new String[]{""};
        List<String> subs = menuSubs.get(path);

        boolean onlyCheckboxes = true;
        for (String sub : subs) {
            if (sub.equals("-")) {
                continue;
            }
            int subType = menuType.get(sub);
            if (subType == TYPE_MENUITEM) {
                onlyCheckboxes = false;
                break;
            }
            String subIcon = menuIcons.get(sub);
            if (subIcon != null) {
                onlyCheckboxes = false;
                break;
            }
        }
        if (subs.isEmpty()) {
            onlyCheckboxes = false;
        }

        if (isAppMenu) {
            RibbonApplicationMenu mainMenu = new RibbonApplicationMenu();
            for (String sub : subs) {
                if (sub.equals("-")) {
                    mainMenu.addMenuSeparator();
                    continue;
                }

                String subTitle = menuTitles.get(sub);
                String subIcon = menuIcons.get(sub);
                int subType = menuType.get(sub);
                ActionListener subAction = menuActions.get(sub);
                final ActionListener subLoader = menuLoaders.get(sub);

                if (sub.startsWith("_/$")) //FooterMenu
                {
                    RibbonApplicationMenuEntryFooter footerMenu = new RibbonApplicationMenuEntryFooter(View.getResizableIcon(subIcon, 16), subTitle, subAction);
                    menuItems.put(sub, footerMenu);
                    mainMenu.addFooterEntry(footerMenu);
                } else {
                    RibbonApplicationMenuEntryPrimary menu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon(subIcon, 32), subTitle, subAction,
                            subType == TYPE_MENU ? JCommandButton.CommandButtonKind.ACTION_AND_POPUP_MAIN_ACTION : JCommandButton.CommandButtonKind.ACTION_ONLY);

                    if (subLoader != null) {
                        menu.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {
                            @Override
                            public void menuEntryActivated(JPanel targetPanel) {
                                subLoader.actionPerformed(new ActionEvent(targetPanel, 0, "load:" + sub));
                            }
                        });
                    }
                    menuItems.put(sub, menu);
                    mainMenu.addMenuEntry(menu);
                }

            }

            ribbon.setApplicationMenu(mainMenu);
            return;
        }

        for (String sub : subs) {
            if (sub.equals("-")) {
                continue;
            }
            int subType = menuType.get(sub);
            ActionListener subAction = menuActions.get(sub);
            String subTitle = menuTitles.get(sub);
            String subIcon = menuIcons.get(sub);
            String subGroup = menuGroup.get(sub);
            HotKey subKey = menuHotkeys.get(sub);
            if (subKey != null) {
                String keyStr = subKey.toString();
                if (keyStr.length() < 8) {
                    subTitle += " (" + keyStr + ")";
                }
            }
            int subPriority = menuPriorities.get(sub);
            final ActionListener subLoader = menuLoaders.get(sub);
            AbstractCommandButton but = null;
            if (subType == TYPE_MENUITEM || (subType == TYPE_MENU && subAction != null)) {
                JCommandButton cbut;
                if (subIcon != null) {
                    cbut = new JCommandButton(fixCommandTitle(subTitle), View.getResizableIcon(subIcon, subPriority == PRIORITY_TOP ? 32 : 16));
                } else {
                    cbut = new JCommandButton(fixCommandTitle(subTitle));
                }
                if (subKey != null) {
                    //cbut.setActionRichTooltip(new RichTooltip(subTitle, subKey.toString()));
                }
                if (subLoader != null) {
                    cbut.setCommandButtonKind(JCommandButton.CommandButtonKind.ACTION_AND_POPUP_MAIN_ACTION);
                    cbut.setPopupCallback(new PopupPanelCallback() {

                        @Override
                        public JPopupPanel getPopupPanel(JCommandButton jcb) {
                            JPopupPanel jp = new JPopupPanel() {
                            };

                            subLoader.actionPerformed(new ActionEvent(jp, 0, "load:" + sub));
                            return jp;
                        }
                    });
                }
                but = cbut;
            } else if (subType == TYPE_TOGGLEMENUITEM) {
                if (onlyCheckboxes) {
                    JCheckBox cb = new JCheckBox(subTitle);
                    if (subAction != null) {
                        cb.addActionListener(subAction);
                    }
                    menuItems.put(sub, cb);
                } else {
                    if (subIcon != null) {
                        but = new JCommandToggleButton(fixCommandTitle(subTitle), View.getResizableIcon(subIcon, subPriority == PRIORITY_TOP ? 32 : 16));
                    } else {
                        but = new JCommandToggleButton(fixCommandTitle(subTitle));
                    }
                    menuToToggleGroup.get(sub).add((JCommandToggleButton) but);
                }
            }
            if (but != null) {
                if (subAction != null) {
                    but.addActionListener(subAction);
                }
                menuItems.put(sub, but);
            }
        }

        //if (parts.length == 3)
        { //3rd level - it's a Band!
            JRibbonBand band = new JRibbonBand(title, icon != null ? View.getResizableIcon(icon, 16) : null, null);
            band.setResizePolicies(getResizePolicies(band));
            int cnt = 0;
            for (String sub : subs) {
                if (sub.equals("-")) {
                    continue;
                }

                Object o = menuItems.get(sub);
                int subPriority = menuPriorities.get(sub);
                int subType = menuType.get(sub);
                ActionListener subAction = menuActions.get(sub);
                if (subType != TYPE_MENU || (subAction != null)) {
                    if (o instanceof AbstractCommandButton) {
                        RibbonElementPriority ribbonPriority = RibbonElementPriority.MEDIUM;
                        switch (subPriority) {
                            case PRIORITY_LOW:
                                ribbonPriority = RibbonElementPriority.LOW;
                                break;
                            case PRIORITY_MEDIUM:
                                ribbonPriority = RibbonElementPriority.MEDIUM;
                                break;
                            case PRIORITY_TOP:
                                ribbonPriority = RibbonElementPriority.TOP;
                                break;
                        }

                        band.addCommandButton((AbstractCommandButton) o, ribbonPriority);
                        cnt++;
                    } else if (o instanceof JComponent) {
                        band.addRibbonComponent(new JRibbonComponent((JComponent) o));
                        cnt++;
                    }
                }
            }
            if (cnt > 0) {
                if (parts.length != 3) {
                    if (!menuSubs.containsKey(path)) {
                        menuSubs.put(path, new ArrayList<>());
                    }
                    if (!menuSubs.get(path).contains(path + "/_")) {
                        menuSubs.get(path).add(0, path + "/_");
                    }
                    menuItems.put(path + "/_", band);

                } else {
                    menuItems.put(path, band);
                }

            }

        }

        if (parts.length == 1) { //1st level - it's ribbon
            for (String sub : subs) {
                if (sub.equals("-")) {
                    continue;
                }
                if (menuItems.get(sub) instanceof RibbonTask) {
                    RibbonTask rt = (RibbonTask) menuItems.get(sub);
                    if (menuOptional.get(sub)) {
                        RibbonContextualTaskGroup rct = new RibbonContextualTaskGroup("", new Color(128, 0, 0), rt);
                        ribbon.addContextualTaskGroup(rct);
                        optionalGroups.put(sub, rct);
                        //ribbon.setVisible(rct, false);
                    } else {
                        ribbon.addTask(rt);
                    }
                }
            }
        } else if (parts.length == 2) { //2nd level - it's a Task!
            int bandCount = 0;
            for (String sub : subs) {
                if (sub.equals("-")) {
                    continue;
                }

                if (menuItems.get(sub) instanceof AbstractRibbonBand) {
                    bandCount++;
                }
            }
            AbstractRibbonBand[] bands = new AbstractRibbonBand[bandCount];
            int b = 0;
            for (String sub : subs) {
                if (sub.equals("-")) {
                    continue;
                }
                if (menuItems.get(sub) instanceof AbstractRibbonBand) {
                    bands[b++] = (AbstractRibbonBand) menuItems.get(sub);
                }
            }
            if (bands.length > 0) {
                RibbonTask task = new RibbonTask(title, bands);
                menuItems.put(path, task);
            }
        }
    }

    @Override
    public void addMenuItem(String path, String title, String icon, ActionListener action, int priority, ActionListener subLoader, boolean isLeaf, HotKey key, boolean isOptional) {
        String parentPath = path.contains("/") ? path.substring(0, path.lastIndexOf('/')) : "";
        if (!menuSubs.containsKey(parentPath)) {
            throw new IllegalArgumentException("No parent menu exists: " + parentPath);
        }
        menuOptional.put(path, isOptional);
        menuHotkeys.put(path, key);
        menuSubs.get(parentPath).add(path);
        if (!isLeaf) {
            menuSubs.put(path, new ArrayList<>());
        }
        menuLoaders.put(path, subLoader);
        menuTitles.put(path, title);
        menuIcons.put(path, icon);
        menuActions.put(path, action);
        menuPriorities.put(path, priority);
        menuType.put(path, isLeaf ? TYPE_MENUITEM : TYPE_MENU);
    }

    @Override
    public void addToggleMenuItem(String path, String title, String group, String icon, ActionListener action, int priority, HotKey key) {
        addMenuItem(path, title, icon, action, priority, action, true, key, false);
        menuType.put(path, TYPE_TOGGLEMENUITEM);
        menuGroup.put(path, group);
        if (group == null) {
            group = path;
        }
        if (!menuToggleGroups.containsKey(group)) {
            menuToggleGroups.put(group, new CommandToggleButtonGroup());
        }
        menuToToggleGroup.put(path, menuToggleGroups.get(group));
    }

    @Override
    public boolean isMenuChecked(String path) {
        Object o = menuItems.get(path);
        if (o instanceof JCommandToggleButton) {
            JCommandToggleButton t = (JCommandToggleButton) o;
            if (!menuToToggleGroup.containsKey(path)) {
                throw new IllegalArgumentException("No toggle group for " + path);
            }
            return menuToToggleGroup.get(path).getSelected() == t;
        }
        if (o instanceof JCheckBox) {
            return ((JCheckBox) o).isSelected();
        }
        throw new IllegalArgumentException("Not a toggle menu");
    }

    @Override
    public void setMenuChecked(String path, boolean checked) {
        Object o = menuItems.get(path);
        if (o instanceof JCommandToggleButton) {
            JCommandToggleButton t = (JCommandToggleButton) o;
            if (!menuToToggleGroup.containsKey(path)) {
                throw new IllegalArgumentException("No toggle group for " + path);
            }
            menuToToggleGroup.get(path).setSelected(t, checked);
        } else if (o instanceof JToggleButton) {
            ((JToggleButton) o).setSelected(checked);
        } else {
            throw new IllegalArgumentException("Not a toggle menu");
        }
    }

    @Override
    public void setGroupSelection(String group, String selected) {
        if (!menuToggleGroups.containsKey(group)) {
            throw new IllegalArgumentException("Group " + group + " does not exist");
        }
        menuToggleGroups.get(group).clearSelection();
        if (selected == null) {
            return;
        }
        if (!menuItems.containsKey(selected)) {
            throw new IllegalArgumentException("Selection " + selected + " not found");
        }
        JCommandToggleButton c = (JCommandToggleButton) menuItems.get(selected);
        menuToggleGroups.get(group).setSelected(c, true);
    }

    @Override
    public String getGroupSelection(String group) {
        if (!menuToggleGroups.containsKey(group)) {
            throw new IllegalArgumentException("Group " + group + " does not exist");
        }
        JCommandToggleButton c = menuToggleGroups.get(group).getSelected();
        for (String path : menuItems.keySet()) {
            if (menuItems.get(path) == c) {
                return path;
            }
        }
        return null;
    }

    @Override
    public void clearMenu(String path) {

    }

    @Override
    public void setMenuEnabled(String path, boolean enabled) {
        if (!menuItems.containsKey(path)) {
            throw new IllegalArgumentException("Menu not found: " + path);
        }
        Object o = menuItems.get(path);
        try {
            if (o instanceof JRibbonBand) {
                ((JRibbonBand) o).setEnabled(enabled);
            } else if (o instanceof AbstractCommandButton) {
                ((AbstractCommandButton) o).setEnabled(enabled);
            } else if (o instanceof RibbonApplicationMenuEntryPrimary) {
                ((RibbonApplicationMenuEntryPrimary) o).setEnabled(enabled);
            } else if (o instanceof RibbonApplicationMenuEntryFooter) {
                ((RibbonApplicationMenuEntryFooter) o).setEnabled(enabled);
            } else if (o instanceof JComponent) {
                ((JComponent) o).setEnabled(enabled);
            } else {
                throw new IllegalArgumentException("Cannot set enabled to: " + path);
            }
        } catch (Exception ex) {
            //some substance issues, ignore
        }
    }

    @Override
    public void initMenu() {
        menuSubs.put("", new ArrayList<>());
        menuPriorities.put("", 0);
        menuActions.put("", null);
        menuTitles.put("", null);
        menuIcons.put("", null);
        menuType.put("", TYPE_MENU);
    }

    @Override
    public void addSeparator(String parentPath) {
        if (!menuSubs.containsKey(parentPath)) {
            throw new IllegalArgumentException("Menu does not exist: " + parentPath);
        }
        menuSubs.get(parentPath).add("-");
    }

    @Override
    public boolean supportsMenuAction() {
        return true;
    }

    @Override
    public boolean supportsAppMenu() {
        return true;
    }

    @Override
    public void setPathVisible(String path, boolean val) {
        Object o = menuItems.get(path);
        if (o instanceof RibbonTask) {
            if (menuOptional.get(path)) {
                RibbonContextualTaskGroup rg = optionalGroups.get(path);

                if (ribbon.isVisible(rg) != val) {
                    View.execInEventDispatch(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                ribbon.setVisible(rg, val);
                            } catch (Exception ex) {

                            }
                        }
                    });

                }
            }
        }
    }

    @Override
    public void hilightPath(String path) {
        Object o = menuItems.get(path);
        if (o instanceof RibbonTask) {
            if (menuOptional.get(path)) {
                View.execInEventDispatch(new Runnable() {

                    @Override
                    public void run() {
                        if (!ribbon.isVisible(optionalGroups.get(path))) {
                            ribbon.setVisible(optionalGroups.get(path), true);
                        }
                        ribbon.setSelectedTask((RibbonTask) o);
                    }
                });
                return;
            }
            final RibbonTask rt = (RibbonTask) o;
            View.execInEventDispatch(new Runnable() {

                @Override
                public void run() {
                    ribbon.setSelectedTask(rt);
                }
            });

        }
    }
}

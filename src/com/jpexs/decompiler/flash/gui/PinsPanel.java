/*
 * Copyright (C) 2022 JPEXS
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.tagtree.TreeRoot;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;

/**
 *
 * @author JPEXS
 */
public class PinsPanel extends JPanel {

    private List<TreeItem> items = new ArrayList<>();
    private TreeItem current;
    private MainPanel mainPanel;
    private PinButton lastSelectedButton;
    private PinButton currentUnpinnedButton;
    private List<PinButton> buttons = new ArrayList<>();   
    private List<ChangeListener> changeListeners = new ArrayList<>();
    
    private List<String> missingTagTreePaths = new ArrayList<>();
    private List<String> missingTagListPaths = new ArrayList<>();
    
    
    private static final String PATHS_SEPARATOR = "{#sep#}";

    public PinsPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        setLayout(new WrapLayout(WrapLayout.LEFT, 5, 2));
        setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
    }

    public void clear() {
        for (TreeItem item : items) {
            String tagTreePath = mainPanel.tagTree.getItemPathString(item);
            if (tagTreePath == null) {
                tagTreePath = "";
            }

            String tagListPath = mainPanel.tagListTree.getItemPathString(item);
            if (tagListPath == null) {
                tagListPath = "";
            }

            missingTagTreePaths.add(tagTreePath);
            missingTagListPaths.add(tagListPath);
        }
        items.clear();
        rebuild();
        save();
    }

    public void setCurrent(TreeItem item) {
        if (lastSelectedButton != null) {
            lastSelectedButton.setSelected(false);
        }
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == item) {            
                this.current = item;        
                buttons.get(i).setSelected(true);
                lastSelectedButton = buttons.get(i);
                if (currentUnpinnedButton != null) {
                    remove(currentUnpinnedButton);
                    revalidate();
                    repaint();
                }
                return;
            }
        }
        if (this.current == item) {
            return;
        }
        
        this.current = item;
        
        rebuild();
    }

    private String getTreeItemPath(TreeItem item) {
        TreePath path = mainPanel.getCurrentTree().getModel().getTreePath(item);
        if (path == null) {
            return "";
        }
        StringBuilder pathString = new StringBuilder();
        for (int i = 1; i < path.getPathCount(); i++) {
            if (pathString.length() > 0) {
                pathString.append(" / ");
            }
            pathString.append(path.getPathComponent(i).toString());
        }
        return pathString.toString();
    }

    private void rebuild() {
        removeAll();
        buttons.clear();
        currentUnpinnedButton = null;
        boolean currentPinned = false;
        for (TreeItem item : items) {
            PinButton pinButton = new PinButton(item, true);            
            pinButton.setToolTipText(getTreeItemPath(item));
            pinButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        JPopupMenu pinMenu = new JPopupMenu();
                        JMenuItem unpinMenuItem = new JMenuItem(AppStrings.translate("contextmenu.unpin"));
                        unpinMenuItem.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                items.remove(item);
                                rebuild();
                                fireChange();
                            }                            
                        });
                        pinMenu.add(unpinMenuItem);
                        
                        JMenuItem unpinAllMenuItem = new JMenuItem(AppStrings.translate("contextmenu.unpin.all"));
                        unpinAllMenuItem.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                items.clear();
                                rebuild();
                                fireChange();
                            }                            
                        });
                        if (items.size() > 1) {
                            pinMenu.add(unpinAllMenuItem);
                        }
                        
                        JMenuItem unpinOthersMenuItem = new JMenuItem(AppStrings.translate("contextmenu.unpin.others"));
                        unpinOthersMenuItem.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                items.clear();
                                items.add(item);
                                rebuild();
                                fireChange();
                            }                            
                        });
                        if (items.size() > 1) {
                            pinMenu.add(unpinOthersMenuItem);
                        }
                        pinMenu.show(pinButton, e.getX(), e.getY());
                    }
                }
                
            });
            pinButton.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (!pinButton.isPinned()) {
                        items.remove(item);
                        rebuild();                        
                    }
                    save();
                    fireChange();
                }
            });
            pinButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    current = pinButton.getItem();  
                    if (lastSelectedButton != null) {
                        lastSelectedButton.setSelected(false);
                        if (!lastSelectedButton.isPinned()) {
                            PinsPanel.this.remove(lastSelectedButton);
                            revalidate();
                            repaint();
                        }
                    }
                    lastSelectedButton = pinButton;
                    pinButton.setSelected(true);
                    fireChange();
                }
            });
            add(pinButton);
            buttons.add(pinButton);
            if (item == current) {
                currentPinned = true;
                lastSelectedButton = pinButton;
                pinButton.setSelected(true);
            }
        }
        if (!currentPinned && current != null) {
            currentUnpinnedButton = new PinButton(current, false);
            lastSelectedButton = currentUnpinnedButton;
            add(currentUnpinnedButton);
            currentUnpinnedButton.setToolTipText(getTreeItemPath(current));
            currentUnpinnedButton.setSelected(true);
            currentUnpinnedButton.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    items.add(current);
                    rebuild();
                    save();
                    fireChange();
                }
            });
        }
        revalidate();
        repaint();
    }

    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    private void fireChange() {
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener listener : changeListeners) {
            listener.stateChanged(ev);
        }
    }

    public List<TreeItem> getPinnedItems() {
        return new ArrayList<>(items);
    }

    public TreeItem getCurrent() {
        return current;
    }

    public void setPinnedItems(List<TreeItem> items) {
        this.items = new ArrayList<>(items);
        rebuild();
    }
    
    public void save() {
        StringBuilder tagTreePathsBuilder = new StringBuilder();
        StringBuilder tagListPathsBuilder = new StringBuilder();
        boolean first = true;
        
        for (int i = 0; i < missingTagTreePaths.size(); i++) {
            if (!first) {                
                tagTreePathsBuilder.append(PATHS_SEPARATOR);
                tagListPathsBuilder.append(PATHS_SEPARATOR);
            }
            tagTreePathsBuilder.append(missingTagTreePaths.get(i));
            tagListPathsBuilder.append(missingTagListPaths.get(i));
            first = false;
        }
        
        for (TreeItem item : items) {
            String tagTreePath = mainPanel.tagTree.getItemPathString(item);
            if (tagTreePath == null) {
                tagTreePath = "";
            }
            
            String tagListPath = mainPanel.tagListTree.getItemPathString(item);
            if (tagListPath == null) {
                tagListPath = "";
            }
            if (!first) {                
                tagTreePathsBuilder.append(PATHS_SEPARATOR);
                tagListPathsBuilder.append(PATHS_SEPARATOR);
            }
            tagTreePathsBuilder.append(tagTreePath);
            tagListPathsBuilder.append(tagListPath);
            first = false;
        }
        
        Configuration.pinnedItemsTagTreePaths.set(tagTreePathsBuilder.toString());
        Configuration.pinnedItemsTagListPaths.set(tagListPathsBuilder.toString());
    }
    
    public void load() {
        
        final String PATHS_END = "{finish}";
                
        List<String> missingTagTreePaths = new ArrayList<>();
        List<String> missingTagListPaths = new ArrayList<>();
        String tagTreePathsCombined = Configuration.pinnedItemsTagTreePaths.get() + PATHS_SEPARATOR + PATHS_END;
        String tagListPathsCombined = Configuration.pinnedItemsTagListPaths.get() + PATHS_SEPARATOR + PATHS_END;        
        String[] tagTreePaths = tagTreePathsCombined.split(Pattern.quote(PATHS_SEPARATOR));
        String[] tagListPaths = tagListPathsCombined.split(Pattern.quote(PATHS_SEPARATOR));
        if (tagTreePaths.length != tagListPaths.length) {
            return;
        }
        List<TreeItem> items = new ArrayList<>();
        for (int i = 0; i < tagTreePaths.length - 1; i++) {
            String tagTreePath = tagTreePaths[i];
            String tagListPath = tagListPaths[i];            
            TreeItem item = mainPanel.tagTree.getTreeItemFromPathString(tagTreePath);
            if (item == null || (item instanceof TreeRoot)) {
                item = mainPanel.tagListTree.getTreeItemFromPathString(tagListPath);
            }
            if (item != null && !(item instanceof TreeRoot)) {
                items.add(item);
            } else {
                missingTagTreePaths.add(tagTreePath);
                missingTagListPaths.add(tagListPath);
            }
        }
        this.items = items;
        this.missingTagTreePaths = missingTagTreePaths;
        this.missingTagListPaths = missingTagListPaths;
        rebuild();
    }        
    
    public void removeSwf(SWF swf) {
        for (int i = 0; i < items.size(); i++) {
            TreeItem item = items.get(i);
            SWF itemSwf = item.getSwf();
            if (itemSwf == swf || itemSwf == null) {
                
                String tagTreePath = mainPanel.tagTree.getItemPathString(item);
                if (tagTreePath == null) {
                    tagTreePath = "";
                }

                String tagListPath = mainPanel.tagListTree.getItemPathString(item);
                if (tagListPath == null) {
                    tagListPath = "";
                }
                
                missingTagTreePaths.add(tagTreePath);
                missingTagListPaths.add(tagListPath);
                
                items.remove(i);
                i--;
            }
        }
        save();
    }
}

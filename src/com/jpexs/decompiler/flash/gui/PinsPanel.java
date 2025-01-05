/*
 *  Copyright (C) 2022-2024 JPEXS
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

import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.tagtree.TreeRoot;
import com.jpexs.decompiler.flash.timeline.TagScript;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
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
    private List<String> missingScrollPos = new ArrayList<>();

    private static final String PATHS_SEPARATOR = "{#sep#}";

    public PinsPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        setLayout(new WrapLayout(WrapLayout.LEFT, 5, 2));
        setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
    }

    /**
     * Totally destroy and save empty
     */
    public void destroy() {
        items.clear();
        missingScrollPos.clear();
        missingTagListPaths.clear();
        missingTagTreePaths.clear();
        rebuild();
        save();
    }

    /**
     * Removes all items reference, saves them as paths.
     */
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

            String scrollPos = mainPanel.scrollPosStorage.getSerializedScrollPos(item);

            missingTagTreePaths.add(tagTreePath);
            missingTagListPaths.add(tagListPath);
            missingScrollPos.add(scrollPos);
        }
        items.clear();
        rebuild();
        save();
    }

    public void setCurrent(TreeItem item) {
        if (lastSelectedButton != null) {
            lastSelectedButton.setSelected(false);
        }
        
        if (item == null) {
            current = null;
            rebuild();
            return;
        }
        
        TreeItem itemNoTs = item;
        if (item instanceof TagScript) {
            itemNoTs = ((TagScript) item).getTag();
        }

        for (int i = 0; i < items.size(); i++) {
            TreeItem item2 = items.get(i);
            TreeItem item2NoTs = item2;
            if (item2 instanceof TagScript) {
                item2NoTs = ((TagScript) item2).getTag();
            }
            if (item2NoTs == itemNoTs) {
                this.current = items.get(i);
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

        TreeItem currentNoTs = this.current;
        if (currentNoTs instanceof TagScript) {
            currentNoTs = ((TagScript) currentNoTs).getTag();
        }

        if (currentNoTs == itemNoTs) {
            if (currentUnpinnedButton != null) {
                currentUnpinnedButton.setSelected(true);
            }
            return;
        }

        this.current = item;

        rebuild();
    }

    private void rebuild() {
        removeAll();
        buttons.clear();
        currentUnpinnedButton = null;
        boolean currentPinned = false;
        for (TreeItem item : items) {
            PinButton pinButton = new PinButton(mainPanel, item, true);
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
                    if (mainPanel.checkEdited()) {
                        return;
                    }
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
            currentUnpinnedButton = new PinButton(mainPanel, current, false);
            lastSelectedButton = currentUnpinnedButton;
            add(currentUnpinnedButton);
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
        StringBuilder scrollPosesBuilder = new StringBuilder();
        boolean first = true;

        for (int i = 0; i < missingTagTreePaths.size(); i++) {
            if (!first) {
                tagTreePathsBuilder.append(PATHS_SEPARATOR);
                tagListPathsBuilder.append(PATHS_SEPARATOR);
                scrollPosesBuilder.append(PATHS_SEPARATOR);
            }
            tagTreePathsBuilder.append(missingTagTreePaths.get(i));
            tagListPathsBuilder.append(missingTagListPaths.get(i));
            scrollPosesBuilder.append(missingScrollPos.get(i));
            first = false;
        }

        for (TreeItem item : items) {
            String tagTreePath = mainPanel.tagTree.getItemPathString(item);
            if (tagTreePath == null) {
                tagTreePath = "";
            }

            TreeItem tagListItem = item;
            if (item instanceof TagScript) {
                tagListItem = ((TagScript) item).getTag();
            }

            String tagListPath = mainPanel.tagListTree.getItemPathString(tagListItem);
            if (tagListPath == null) {
                tagListPath = "";
            }

            String scrollPos = mainPanel.scrollPosStorage.getSerializedScrollPos(item);

            if (!first) {
                tagTreePathsBuilder.append(PATHS_SEPARATOR);
                tagListPathsBuilder.append(PATHS_SEPARATOR);
                scrollPosesBuilder.append(PATHS_SEPARATOR);
            }
            tagTreePathsBuilder.append(tagTreePath);
            tagListPathsBuilder.append(tagListPath);
            scrollPosesBuilder.append(scrollPos);
            first = false;
        }

        Configuration.pinnedItemsTagTreePaths.set(tagTreePathsBuilder.toString());
        Configuration.pinnedItemsTagListPaths.set(tagListPathsBuilder.toString());
        Configuration.pinnedItemsScrollPos.set(scrollPosesBuilder.toString());
    }

    public void load() {

        final String PATHS_END = "{finish}";

        List<String> missingTagTreePaths = new ArrayList<>();
        List<String> missingTagListPaths = new ArrayList<>();
        List<String> missingScrollPos = new ArrayList<>();
        String tagTreePathsCombined = Configuration.pinnedItemsTagTreePaths.get() + PATHS_SEPARATOR + PATHS_END;
        String tagListPathsCombined = Configuration.pinnedItemsTagListPaths.get() + PATHS_SEPARATOR + PATHS_END;
        String scrollPosCombined = Configuration.pinnedItemsScrollPos.get() + PATHS_SEPARATOR + PATHS_END;

        String[] tagTreePaths = tagTreePathsCombined.split(Pattern.quote(PATHS_SEPARATOR));
        String[] tagListPaths = tagListPathsCombined.split(Pattern.quote(PATHS_SEPARATOR));
        String[] scrollPoses = scrollPosCombined.split(Pattern.quote(PATHS_SEPARATOR));
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
            String scrollPosStr = "";
            if (!Configuration.pinnedItemsScrollPos.get().isEmpty()) {
                scrollPosStr = scrollPoses[i];
            }

            if (item != null && !(item instanceof TreeRoot)) {
                items.add(item);
                mainPanel.scrollPosStorage.setSerializedScrollPos(item, scrollPosStr);
            } else {
                missingTagTreePaths.add(tagTreePath);
                missingTagListPaths.add(tagListPath);
                missingScrollPos.add(scrollPosStr);
            }
        }
        this.items = items;
        this.missingTagTreePaths = missingTagTreePaths;
        this.missingTagListPaths = missingTagListPaths;
        this.missingScrollPos = missingScrollPos;
        rebuild();
    }

    public void removeOpenable(Openable openable) {
        for (int i = 0; i < items.size(); i++) {
            TreeItem item = items.get(i);
            Openable itemOpenable = item.getOpenable();
            if (itemOpenable == openable || itemOpenable == null) {

                String tagTreePath = mainPanel.tagTree.getItemPathString(item);
                if (tagTreePath == null) {
                    tagTreePath = "";
                }

                String tagListPath = mainPanel.tagListTree.getItemPathString(item);
                if (tagListPath == null) {
                    tagListPath = "";
                }

                String scrollPos = mainPanel.scrollPosStorage.getSerializedScrollPos(item);

                missingTagTreePaths.add(tagTreePath);
                missingTagListPaths.add(tagListPath);
                missingScrollPos.add(scrollPos);

                items.remove(i);
                i--;
            }
        }
        save();
    }

    public void replaceItem(TreeItem oldItem, TreeItem newItem) {

        TreeItem oldItemNoTs = oldItem;
        if (oldItem instanceof TagScript) {
            oldItemNoTs = ((TagScript) oldItem).getTag();
        }

        for (int i = 0; i < items.size(); i++) {
            TreeItem item2NoTs = items.get(i);
            if (item2NoTs instanceof TagScript) {
                item2NoTs = ((TagScript) item2NoTs).getTag();
            }
            if (item2NoTs == oldItemNoTs) {
                items.set(i, newItem);
                rebuild();
                break;
            }
        }
    }

    public void removeOthers(TreeItem item) {
        items.clear();
        items.add(item);
        rebuild();
        save();
    }

    public void pin(TreeItem item) {
        if (!isPinned(item)) {
            items.add(item);
            rebuild();
            save();
        }
    }

    public int getPinCount() {
        return items.size();
    }

    public boolean isPinned(TreeItem item) {
        if (item instanceof TagScript) {
            item = ((TagScript) item).getTag();
        }

        for (int i = 0; i < items.size(); i++) {
            TreeItem item2 = items.get(i);
            if (item2 instanceof TagScript) {
                item2 = ((TagScript) item2).getTag();
            }
            if (item2 == item) {
                return true;
            }
        }
        return false;
    }

    public void removeItem(TreeItem item) {
        if (item instanceof TagScript) {
            item = ((TagScript) item).getTag();
        }

        for (int i = 0; i < items.size(); i++) {
            TreeItem item2 = items.get(i);
            if (item2 instanceof TagScript) {
                item2 = ((TagScript) item2).getTag();
            }
            if (item2 == item) {
                items.remove(i);
                rebuild();
                break;
            }
        }
    }

    public void refresh() {
        for (PinButton button : buttons) {
            button.refresh();
        }
        if (currentUnpinnedButton != null) {
            currentUnpinnedButton.refresh();
        }
    }
    
    public void refreshScriptPacks() {
        for (int b = 0; b < buttons.size(); b++) {
            PinButton button = buttons.get(b);
            if (button.getItem() instanceof ScriptPack) {
                ScriptPack sp = (ScriptPack) button.getItem();                
                List<ScriptPack> packs = sp.abc.getScriptPacks(null, sp.allABCs);
                for (ScriptPack sp2 : packs) {
                    if (Objects.equals(sp.getClassPath(), sp2.getClassPath())) {
                        replaceItem(sp, sp2);
                        break;
                    }
                }
            }
        }
    }
}

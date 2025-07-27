/*
 *  Copyright (C) 2023-2025 JPEXS
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
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.timeline.AS2Package;
import com.jpexs.decompiler.flash.timeline.TagScript;
import com.jpexs.decompiler.flash.treeitems.AS3ClassTreeItem;
import com.jpexs.decompiler.flash.treeitems.FolderItem;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JScrollPane;

/**
 * Storage for remembering scroll/caret positions of scripts / preview folder.
 *
 * @author JPEXS
 */
public class ScrollPosStorage {

    private final MainPanel mainPanel;
    private final List<ScrollPosItem> storage = new ArrayList<>();

    private static final String PATHS_SEPARATOR = "{#sep#}";
    private static final String POS_SEPARATOR = ";";

    public ScrollPosStorage(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    private int indexOf(TreeItem item) {
        for (int i = 0; i < storage.size(); i++) {
            ScrollPosItem sitem = storage.get(i);
            TreeItem titem = sitem.getItem();
            if (titem == null) {
                storage.remove(i);
                i--;
                continue;
            }
            if (titem == item) {
                return i;
            }
        }
        return -1;
    }

    public void setSerializedScrollPos(TreeItem item, String serializedScrollPos) {
        if (serializedScrollPos.isEmpty()) {
            int index = indexOf(item);
            if (index > -1) {
                storage.remove(index);
            }
            return;
        }
        String[] parts = (serializedScrollPos + POS_SEPARATOR).split(POS_SEPARATOR);
        int actionScriptScrollHorizontal = Integer.parseInt(parts[0]);
        int actionScriptScrollVertical = Integer.parseInt(parts[1]);
        int actionScriptCaret = Integer.parseInt(parts[2]);
        int pcodeScrollHorizontal = Integer.parseInt(parts[3]);
        int pcodeScrollVertical = Integer.parseInt(parts[4]);
        int pcodeCaret = Integer.parseInt(parts[5]);
        int folderPreviewScrollVertical = Integer.parseInt(parts[6]);
        int folderListScrollVertical = Integer.parseInt(parts[7]);
        int index = indexOf(item);
        ScrollPosItem sitem = new ScrollPosItem(item, actionScriptScrollHorizontal, actionScriptScrollVertical, actionScriptCaret, pcodeScrollHorizontal, pcodeScrollVertical, pcodeCaret, folderPreviewScrollVertical, folderListScrollVertical);
        if (index > -1) {
            storage.set(index, sitem);
        } else {
            storage.add(sitem);
        }
    }

    public String getSerializedScrollPos(TreeItem item) {
        int index = indexOf(item);
        if (index == -1) {
            return "";
        }
        ScrollPosItem sitem = storage.get(index);
        return sitem.getActionScriptScrollHorizontal() + ";"
                + sitem.getActionScriptScrollVertical() + ";"
                + sitem.getActionScriptCaret() + ";"
                + sitem.getPcodeScrollHorizontal() + ";"
                + sitem.getPcodeScrollVertical() + ";"
                + sitem.getPcodeCaret() + ";"
                + sitem.getFolderPreviewScrollVertical() + ";"
                + sitem.getFolderListScrollVertical();
    }

    public void loadScrollPos(TreeItem item) {
        int index = indexOf(item);
        if (index == -1) {
            return;
        }
        ScrollPosItem sitem = storage.get(index);
        //move to bottom
        storage.remove(index);
        storage.add(sitem);
        
        TreeItem asmItem = item;
        if (asmItem instanceof TagScript) {
            asmItem = ((TagScript) item).getTag();
        }

        if (Configuration.rememberScriptsScrollPos.get()) {
            if (item instanceof ScriptPack) {
                mainPanel.getABCPanel().decompiledTextArea.runWhenLoaded(new Runnable() {
                    @Override
                    public void run() {
                        if (sitem.getActionScriptCaret() < mainPanel.getABCPanel().decompiledTextArea.getDocument().getLength()) {
                            mainPanel.getABCPanel().decompiledTextArea.setCaretPositionForCurrentScript(sitem.getActionScriptCaret(), (ScriptPack) item);
                        }

                        try {
                            mainPanel.getABCPanel().detailPanel.methodTraitPanel.methodCodePanel.getSourceTextArea().setCaretPosition(sitem.getPcodeCaret());
                        } catch (IllegalArgumentException iex) {
                            //ignored
                        }

                        Timer tim = new Timer();
                        tim.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                View.execInEventDispatch(new Runnable() {
                                    @Override
                                    public void run() {
                                        mainPanel.getABCPanel().decompiledScrollPane.getHorizontalScrollBar().setValue(sitem.getActionScriptScrollHorizontal());
                                        mainPanel.getABCPanel().decompiledScrollPane.getVerticalScrollBar().setValue(sitem.getActionScriptScrollVertical());
                                        mainPanel.getABCPanel().detailPanel.methodTraitPanel.methodCodePanel.getSourceScrollPane().getHorizontalScrollBar().setValue(sitem.getPcodeScrollHorizontal());
                                        mainPanel.getABCPanel().detailPanel.methodTraitPanel.methodCodePanel.getSourceScrollPane().getVerticalScrollBar().setValue(sitem.getPcodeScrollVertical());
                                    }
                                });
                            }
                        }, 100);

                    }
                });
            } else if (asmItem instanceof ASMSource) {
                mainPanel.getActionPanel().runWhenLoaded(new Runnable() {
                    @Override
                    public void run() {                                                
                        if (sitem.getActionScriptCaret() < mainPanel.getActionPanel().decompiledEditor.getDocument().getLength()) {
                            mainPanel.getActionPanel().decompiledEditor.setCaretPosition(sitem.getActionScriptCaret());
                        }

                        try {
                            mainPanel.getActionPanel().editor.setCaretPosition(sitem.getPcodeCaret());
                        } catch (IllegalArgumentException iex) {
                            //ignored
                        }

                        Timer tim = new Timer();
                        tim.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                View.execInEventDispatch(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((JScrollPane) mainPanel.getActionPanel().decompiledEditor.getParent().getParent()).getHorizontalScrollBar().setValue(sitem.getActionScriptScrollHorizontal());
                                        ((JScrollPane) mainPanel.getActionPanel().decompiledEditor.getParent().getParent()).getVerticalScrollBar().setValue(sitem.getActionScriptScrollVertical());
                                        ((JScrollPane) mainPanel.getActionPanel().editor.getParent().getParent()).getHorizontalScrollBar().setValue(sitem.getPcodeScrollHorizontal());
                                        ((JScrollPane) mainPanel.getActionPanel().editor.getParent().getParent()).getVerticalScrollBar().setValue(sitem.getPcodeScrollVertical());
                                    }
                                });
                            }
                        }, 100);

                    }
                });
            }
        }
        if (Configuration.rememberFoldersScrollPos.get()) {
            Timer tim = new Timer();
            tim.schedule(new TimerTask() {
                @Override
                public void run() {
                    View.execInEventDispatchLater(new Runnable() {
                        @Override
                        public void run() {
                            ((JScrollPane) mainPanel.folderPreviewPanel.getParent().getParent()).getVerticalScrollBar().setValue(sitem.getFolderPreviewScrollVertical());
                            ((JScrollPane) mainPanel.folderListPanel.getParent().getParent()).getVerticalScrollBar().setValue(sitem.getFolderListScrollVertical());
                        }
                    });
                }
            }, 10);
        }
    }

    public void saveScrollPos(TreeItem item) {

        boolean doSave = false;
        
        TreeItem asmItem = item;
        if (item instanceof TagScript) {
            asmItem = ((TagScript) item).getTag();
        }
        if (item instanceof ScriptPack) {
            doSave = true;
        }
        if (asmItem instanceof ASMSource) {
            doSave = true;
        }
        if (item instanceof FolderItem) {
            doSave = true;
        }
        if (item instanceof AS3ClassTreeItem) {
            doSave = true;
        }
        if (item instanceof AS2Package) {
            doSave = true;
        }

        if (!doSave) {
            return;
        }

        int actionScriptScrollHorizontal = 0;
        int actionScriptScrollVertical = 0;
        int actionScriptCaret = 0;
        int pcodeScrollHorizontal = 0;
        int pcodeScrollVertical = 0;
        int pcodeCaret = 0;
        if (item instanceof ScriptPack) {
            synchronized (mainPanel.getABCPanel().decompiledTextArea) {
                if (!mainPanel.getABCPanel().decompiledTextArea.isScriptLoaded()) {
                    return;
                }
                if (mainPanel.getABCPanel().decompiledTextArea.getScriptLeaf() != item) {
                    return;
                }
                actionScriptCaret = mainPanel.getABCPanel().decompiledTextArea.getCaretPosition();
                if (mainPanel.getABCPanel().decompiledTextArea.getSelectionStart() > 0) {
                    actionScriptCaret = mainPanel.getABCPanel().decompiledTextArea.getSelectionStart();
                }
                
                actionScriptScrollHorizontal = mainPanel.getABCPanel().decompiledScrollPane.getHorizontalScrollBar().getValue();
                actionScriptScrollVertical = mainPanel.getABCPanel().decompiledScrollPane.getVerticalScrollBar().getValue();
                pcodeScrollHorizontal = mainPanel.getABCPanel().detailPanel.methodTraitPanel.methodCodePanel.getSourceScrollPane().getHorizontalScrollBar().getValue();
                pcodeScrollVertical = mainPanel.getABCPanel().detailPanel.methodTraitPanel.methodCodePanel.getSourceScrollPane().getVerticalScrollBar().getValue();
                pcodeCaret = mainPanel.getABCPanel().detailPanel.methodTraitPanel.methodCodePanel.getSourceTextArea().getCaretPosition();
                if (mainPanel.getABCPanel().detailPanel.methodTraitPanel.methodCodePanel.getSourceTextArea().getSelectionStart() > 0) {
                    pcodeCaret = mainPanel.getABCPanel().detailPanel.methodTraitPanel.methodCodePanel.getSourceTextArea().getSelectionStart();
                }
            }
                                    
        } else if (asmItem instanceof ASMSource) {       
            
            synchronized (mainPanel.getActionPanel()) {
                if (!mainPanel.getActionPanel().isScriptLoaded()) {
                    return;
                }
                if (mainPanel.getActionPanel().getSrc() != asmItem) {
                    return;
                }
                actionScriptScrollHorizontal = ((JScrollPane) mainPanel.getActionPanel().decompiledEditor.getParent().getParent()).getHorizontalScrollBar().getValue();
                actionScriptScrollVertical = ((JScrollPane) mainPanel.getActionPanel().decompiledEditor.getParent().getParent()).getVerticalScrollBar().getValue();
                actionScriptCaret = mainPanel.getActionPanel().decompiledEditor.getCaretPosition();
                if (mainPanel.getActionPanel().decompiledEditor.getSelectionStart() > 0) {
                    actionScriptCaret = mainPanel.getActionPanel().decompiledEditor.getSelectionStart();
                }
                pcodeScrollHorizontal = ((JScrollPane) mainPanel.getActionPanel().editor.getParent().getParent()).getHorizontalScrollBar().getValue();
                pcodeScrollVertical = ((JScrollPane) mainPanel.getActionPanel().editor.getParent().getParent()).getVerticalScrollBar().getValue();
                pcodeCaret = mainPanel.getActionPanel().editor.getCaretPosition();
                if (mainPanel.getActionPanel().editor.getSelectionStart() > 0) {
                    pcodeCaret = mainPanel.getActionPanel().editor.getSelectionStart();
                }
            }            
        }
        int folderPreviewScrollVertical = 0;
        int folderListScrollVertical = 0;
        if (!(item instanceof ScriptPack) && !(item instanceof ASMSource)) {
            folderPreviewScrollVertical = ((JScrollPane) mainPanel.folderPreviewPanel.getParent().getParent()).getVerticalScrollBar().getValue();
        }
        if (!(item instanceof ScriptPack) && !(item instanceof ASMSource)) {
            folderListScrollVertical = ((JScrollPane) mainPanel.folderListPanel.getParent().getParent()).getVerticalScrollBar().getValue();
        }

        ScrollPosItem savedItem = new ScrollPosItem(item,
                actionScriptScrollHorizontal,
                actionScriptScrollVertical,
                actionScriptCaret,
                pcodeScrollHorizontal,
                pcodeScrollVertical,
                pcodeCaret,
                folderPreviewScrollVertical,
                folderListScrollVertical);

        int index = indexOf(item);

        if (savedItem.isEmpty()) {
            if (index > -1) {
                storage.remove(index);
            }
            return;
        }
        if (index > -1) {
            storage.set(index, savedItem);
        } else {
            storage.add(savedItem);
        }
        if (storage.size() > Configuration.maxRememberedScrollposItems.get()) {
            for (int i = 0; i < storage.size(); i++) {
                TreeItem it = storage.get(i).getItem();
                if (it != null && mainPanel.isPinned(item)) {
                    continue;
                }
                storage.remove(0);
                break;
            }
        }
    }

    class ScrollPosItem {

        private final WeakReference<TreeItem> item;
        private final int actionScriptScrollHorizontal;
        private final int actionScriptScrollVertical;
        private final int actionScriptCaret;
        private final int pcodeScrollHorizontal;
        private final int pcodeScrollVertical;
        private final int pcodeCaret;
        private final int folderPreviewScrollVertical;
        private final int folderListScrollVertical;

        public TreeItem getItem() {
            return item.get();
        }

        public int getActionScriptScrollHorizontal() {
            return actionScriptScrollHorizontal;
        }

        public int getActionScriptScrollVertical() {
            return actionScriptScrollVertical;
        }

        public int getActionScriptCaret() {
            return actionScriptCaret;
        }

        public int getPcodeScrollHorizontal() {
            return pcodeScrollHorizontal;
        }

        public int getPcodeScrollVertical() {
            return pcodeScrollVertical;
        }

        public int getPcodeCaret() {
            return pcodeCaret;
        }

        public int getFolderPreviewScrollVertical() {
            return folderPreviewScrollVertical;
        }

        public int getFolderListScrollVertical() {
            return folderListScrollVertical;
        }

        public ScrollPosItem(TreeItem item, int actionScriptScrollHorizontal, int actionScriptScrollVertical, int actionScriptCaret, int pcodeScrollHorizontal, int pcodeScrollVertical, int pcodeCaret, int folderPreviewScrollVertical, int folderListScrollVertical) {
            this.item = new WeakReference<>(item);
            this.actionScriptScrollHorizontal = actionScriptScrollHorizontal;
            this.actionScriptScrollVertical = actionScriptScrollVertical;
            this.actionScriptCaret = actionScriptCaret;
            this.pcodeScrollHorizontal = pcodeScrollHorizontal;
            this.pcodeScrollVertical = pcodeScrollVertical;
            this.pcodeCaret = pcodeCaret;
            this.folderPreviewScrollVertical = folderPreviewScrollVertical;
            this.folderListScrollVertical = folderListScrollVertical;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 79 * hash + Objects.hashCode(this.item.get());
            hash = 79 * hash + this.actionScriptScrollHorizontal;
            hash = 79 * hash + this.actionScriptScrollVertical;
            hash = 79 * hash + this.actionScriptCaret;
            hash = 79 * hash + this.pcodeScrollHorizontal;
            hash = 79 * hash + this.pcodeScrollVertical;
            hash = 79 * hash + this.pcodeCaret;
            hash = 79 * hash + this.folderPreviewScrollVertical;
            hash = 79 * hash + this.folderListScrollVertical;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ScrollPosItem other = (ScrollPosItem) obj;
            if (this.actionScriptScrollHorizontal != other.actionScriptScrollHorizontal) {
                return false;
            }
            if (this.actionScriptScrollVertical != other.actionScriptScrollVertical) {
                return false;
            }
            if (this.actionScriptCaret != other.actionScriptCaret) {
                return false;
            }
            if (this.pcodeScrollHorizontal != other.pcodeScrollHorizontal) {
                return false;
            }
            if (this.pcodeScrollVertical != other.pcodeScrollVertical) {
                return false;
            }
            if (this.pcodeCaret != other.pcodeCaret) {
                return false;
            }
            if (this.folderPreviewScrollVertical != other.folderPreviewScrollVertical) {
                return false;
            }
            if (this.folderListScrollVertical != other.folderListScrollVertical) {
                return false;
            }
            return this.item.get() == other.item.get();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ScrollPosItem{");
            sb.append("actionScriptScrollHorizontal=").append(actionScriptScrollHorizontal);
            sb.append(", actionScriptScrollVertical=").append(actionScriptScrollVertical);
            sb.append(", actionScriptCaret=").append(actionScriptCaret);
            sb.append(", pcodeScrollHorizontal=").append(pcodeScrollHorizontal);
            sb.append(", pcodeScrollVertical=").append(pcodeScrollVertical);
            sb.append(", pcodeCaret=").append(pcodeCaret);
            sb.append(", folderPreviewScrollVertical=").append(folderPreviewScrollVertical);
            sb.append(", folderListScrollVertical=").append(folderListScrollVertical);
            sb.append('}');
            return sb.toString();
        }

        public boolean isEmpty() {
            if (actionScriptScrollHorizontal > 0) {
                return false;
            }
            if (actionScriptScrollVertical > 0) {
                return false;
            }
            if (actionScriptCaret > 0) {
                return false;
            }
            if (pcodeScrollHorizontal > 0) {
                return false;
            }
            if (pcodeScrollVertical > 0) {
                return false;
            }
            if (pcodeCaret > 0) {
                return false;
            }
            if (folderPreviewScrollVertical > 0) {
                return false;
            }
            if (folderListScrollVertical > 0) {
                return false;
            }
            return true;
        }
    }
}

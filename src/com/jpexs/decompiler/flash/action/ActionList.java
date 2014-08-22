/*
 *  Copyright (C) 2010-2014 JPEXS
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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.DisassemblyListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.special.ActionNop;
import com.jpexs.decompiler.flash.action.special.ActionStore;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.EmptySWFDecompilerListener;
import com.jpexs.decompiler.flash.helpers.FileTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class ActionList extends ArrayList<Action> {

    public ActionList() {
    }

    public ActionList(Collection<Action> actions) {
        super(actions);
    } 

    public void setActions(List<Action> list) {
        clear();
        addAll(list);
    }
    
    public void removeAction(int index) {
        ActionListReader.removeAction(this, index, SWF.DEFAULT_VERSION, true);
    }
    
    public void removeAction(int index, int count) {
        if (size() <= index + count - 1) {
            // Can't remove count elements, only size - index is available
            count = size() - index;
        }
        
        for (int i = 0; i < count; i++) {
            ActionListReader.removeAction(this, index, SWF.DEFAULT_VERSION, true);
        }
    }
    
    public void addAction(int index, Action action) {
        ActionListReader.addAction(this, index, action, SWF.DEFAULT_VERSION, false, false);
    }
    
    public void fixActionList() {
        ActionListReader.fixActionList(this, null, SWF.DEFAULT_VERSION);
    }

    public Iterator<Action> getReferencesFor(final Action target) {
        final ActionList diz = this;
        return new Iterator<Action>() {

            private final Iterator<Action> iterator = diz.iterator();

            private Action action = getNext();
            
            @Override
            public boolean hasNext() {
                return action != null;
            }

            @Override
            public Action next() {
                Action a = action;
                action = getNext();
                return a;
            }
            
            private Action getNext() {
                while (iterator.hasNext()) {
                    Action a = iterator.next();
                    if (a instanceof ActionJump) {
                        ActionJump aJump = (ActionJump) a;
                        long ref = aJump.getAddress() + aJump.getTotalActionLength() + aJump.getJumpOffset();
                        if (target.getAddress() == ref) {
                            return aJump;
                        }
                    } else if (a instanceof ActionIf) {
                        ActionIf aIf = (ActionIf) a;
                        long ref = aIf.getAddress() + aIf.getTotalActionLength() + aIf.getJumpOffset();
                        if (target.getAddress() == ref) {
                            return aIf;
                        }
                    } else if (a instanceof ActionStore) {
                        ActionStore aStore = (ActionStore) a;
                        int storeSize = aStore.getStoreSize();
                        int idx = indexOf(a);
                        int idx2 = indexOf(target);
                        if (idx != -1 && idx2 == idx + storeSize) {
                            return a;
                        }
                    } else if (a instanceof GraphSourceItemContainer) {
                        GraphSourceItemContainer container = (GraphSourceItemContainer) a;
                        long ref = a.getAddress() + a.getTotalActionLength();
                        for (Long size : container.getContainerSizes()) {
                            ref += size;
                            if (target.getAddress() == ref) {
                                return a;
                            }
                        }
                    }
                }
                return null;
            }
        };
    }
    
    public void removeNops() {
        for (int i = 0; i < size(); i++) {
           if (get(i) instanceof ActionNop) {
               removeAction(i);
           }
        }
    }

    public Action getByAddress(long address) {
        int idx = getIndexByAddress(address);
        return idx == -1 ? null : get(idx);
    }

    public int getIndexByAddress(long address) {
        int min = 0;
        int max = size() - 1;

        while (max >= min) {
            int mid = (min + max) / 2;
            long midValue = get(mid).getAddress();
            if (midValue == address) {
                return mid;
            } else if (midValue < address) {
                min = mid + 1;
            } else {
                max = mid - 1;
            }
        }

        return -1;
    }

    public void saveToFile(String fileName) {
        File file = new File(fileName);
        try (FileTextWriter writer = new FileTextWriter(Configuration.getCodeFormatting(), new FileOutputStream(file))) {
            Action.actionsToString(new ArrayList<DisassemblyListener>(), 0, this, SWF.DEFAULT_VERSION, ScriptExportMode.PCODE, writer);
        } catch (IOException ex) {
            Logger.getLogger(EmptySWFDecompilerListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

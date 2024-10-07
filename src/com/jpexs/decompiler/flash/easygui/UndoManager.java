/*
 * Copyright (C) 2024 JPEXS
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
package com.jpexs.decompiler.flash.easygui;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class UndoManager {
    private int historyPos = 0;
    private final List<DoableOperation> history = new ArrayList<>();
    
    private final List<Runnable> changeListeners = new ArrayList<>();
    
    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }
    
    public void removeChangeListener(Runnable listener) {
        changeListeners.remove(listener);
    }
    
    private void fireChange() {
        for (Runnable listener : changeListeners) {
            listener.run();
        }
    }
    
    public void doOperation(DoableOperation doableOperation) {
        //drop redos
        while(history.size() > historyPos) {
            history.remove(historyPos);
        }
        
        history.add(doableOperation);
        historyPos++;
        doableOperation.doOperation();
        fireChange();
    }
    
    public boolean canUndo() {
        return historyPos > 0;
    }
    
    public String getUndoName() {
        if (!canUndo()) {
            return null;
        }
        return history.get(historyPos - 1).getDescription();
    }        
    
    public void undo() {
        if (historyPos == 0) {
            return;
        }
        historyPos--;
        history.get(historyPos).undoOperation();
        fireChange();
    }
    
    public void redo() {
        if (!canRedo()) {
            return;
        }
        history.get(historyPos).doOperation();
        historyPos++;
        fireChange();
    }
    
    public String getRedoName() {
        if (!canRedo()) {
            return null;
        }
        return history.get(historyPos).getDescription();
    }
    
    public boolean canRedo() {
        return history.size() > historyPos;
    }
    
    public void clear() {
        history.clear();
        historyPos = 0;
        fireChange();
    }
}

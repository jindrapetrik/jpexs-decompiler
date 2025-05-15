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

import com.jpexs.decompiler.flash.SWF;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 *
 * @author JPEXS
 */
public class UndoManager {

    private final Map<SWF, Integer> historyPosMap = new WeakHashMap<>();
    private final Map<SWF, List<DoableOperation>> historyMap = new WeakHashMap<>();

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

    public void doOperation(DoableOperation doableOperation, SWF swf) {
        if (!historyMap.containsKey(swf)) {
            historyMap.put(swf, new ArrayList<>());
        }
        if (!historyPosMap.containsKey(swf)) {
            historyPosMap.put(swf, 0);
        }

        int historyPos = historyPosMap.get(swf);

        List<DoableOperation> history = historyMap.get(swf);

        //drop redos
        while (history.size() > historyPos) {
            history.remove(historyPos);
        }

        historyMap.get(swf).add(doableOperation);
        historyPosMap.put(swf, historyPosMap.get(swf) + 1);
        doableOperation.doOperation();
        fireChange();
    }

    public boolean canUndo(SWF swf) {
        if (!historyPosMap.containsKey(swf)) {
            return false;
        }
        return historyPosMap.get(swf) > 0;
    }

    public String getUndoName(SWF swf) {
        if (!canUndo(swf)) {
            return null;
        }
        return historyMap.get(swf).get(historyPosMap.get(swf) - 1).getDescription();
    }

    public void undo(SWF swf) {
        if (!canUndo(swf)) {
            return;
        }
        historyPosMap.put(swf, historyPosMap.get(swf) - 1);
        historyMap.get(swf).get(historyPosMap.get(swf)).undoOperation();
        fireChange();
    }

    public void redo(SWF swf) {
        if (!canRedo(swf)) {
            return;
        }
        historyMap.get(swf).get(historyPosMap.get(swf)).doOperation();
        historyPosMap.put(swf, historyPosMap.get(swf) + 1);
        fireChange();
    }

    public String getRedoName(SWF swf) {
        if (!canRedo(swf)) {
            return null;
        }
        return historyMap.get(swf).get(historyPosMap.get(swf)).getDescription();
    }

    public boolean canRedo(SWF swf) {
        if (!historyMap.containsKey(swf)) {
            return false;
        }
        return historyMap.get(swf).size() > historyPosMap.get(swf);
    }

    public void clear() {
        historyMap.clear();
        historyPosMap.clear();
        fireChange();
    }

    public void clear(SWF swf) {
        if (!historyMap.containsKey(swf)) {
            return;
        }
        historyMap.get(swf).clear();
        historyPosMap.put(swf, 0);
        fireChange();
    }
}

/*
 *  Copyright (C) 2024-2026 JPEXS
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
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.timeline.Timelined;
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
    private final Map<SWF, Integer> savedPosMap = new WeakHashMap<>();

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
        rememberSavedPosition(swf);
        if (savedPosMap.get(swf) > historyPos) {
            // The saved state belongs to the redo branch which is being dropped.
            savedPosMap.put(swf, hasSwfChangesBetween(swf, historyPos, savedPosMap.get(swf)) ? -1 : historyPos);
        }

        List<DoableOperation> history = historyMap.get(swf);

        //drop redos
        while (history.size() > historyPos) {
            history.remove(historyPos);
        }

        historyMap.get(swf).add(doableOperation);
        historyPosMap.put(swf, historyPosMap.get(swf) + 1);
        doableOperation.doOperation();
        updateModifiedState(swf);
        fireChange();
    }

    private void rememberSavedPosition(SWF swf) {
        if (!swf.isModified()) {
            savedPosMap.put(swf, historyPosMap.get(swf));
        } else if (!savedPosMap.containsKey(swf)) {
            savedPosMap.put(swf, -1);
        }
    }

    private void updateModifiedState(SWF swf) {
        int saved = savedPosMap.get(swf);
        if (saved >= 0 && !hasSwfChangesBetween(swf, saved, historyPosMap.get(swf))) {
            clearModifiedFlags(swf);
            swf.setHeaderModified(false);
        } else {
            swf.setModified(true);
        }
    }

    private boolean hasSwfChangesBetween(SWF swf, int first, int second) {
        List<DoableOperation> history = historyMap.get(swf);
        for (int i = Math.min(first, second); i < Math.max(first, second); i++) {
            if (history.get(i).modifiesSwf()) {
                return true;
            }
        }
        return false;
    }

    private void clearModifiedFlags(Timelined timelined) {
        for (Tag tag : timelined.getTags()) {
            if (tag instanceof Timelined) {
                clearModifiedFlags((Timelined) tag);
            }
            tag.setModified(false);
        }
        timelined.setModified(false);
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
        rememberSavedPosition(swf);
        historyPosMap.put(swf, historyPosMap.get(swf) - 1);
        historyMap.get(swf).get(historyPosMap.get(swf)).undoOperation();
        updateModifiedState(swf);
        fireChange();
    }

    public void redo(SWF swf) {
        if (!canRedo(swf)) {
            return;
        }
        rememberSavedPosition(swf);
        historyMap.get(swf).get(historyPosMap.get(swf)).doOperation();
        historyPosMap.put(swf, historyPosMap.get(swf) + 1);
        updateModifiedState(swf);
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
        savedPosMap.clear();
        fireChange();
    }

    public void clear(SWF swf) {
        if (!historyMap.containsKey(swf)) {
            return;
        }
        historyMap.get(swf).clear();
        historyPosMap.put(swf, 0);
        savedPosMap.remove(swf);
        fireChange();
    }
}

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
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Edit history for the open movies, including edits that affect several movies.
 */
public class UndoManager {

    private static class History {

        final List<Entry> entries = new ArrayList<>();
        final SWF swf;
        int position;
        int saved = -1;

        History(SWF swf) {
            this.swf = swf;
        }

        boolean hasChangesBetween(int first, int second) {
            for (int i = Math.min(first, second); i < Math.max(first, second); i++) {
                if (entries.get(i).operation.modifiesSwf(swf)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class Entry {

        final DoableOperation operation;
        final List<SWF> movies;

        Entry(DoableOperation operation, Collection<SWF> movies) {
            this.operation = operation;
            this.movies = new ArrayList<>(movies);
        }
    }

    public static final class ReplayPlan {

        private final SWF movie;
        private final boolean redo;
        private final List<Entry> entries = new ArrayList<>();

        private ReplayPlan(SWF movie, boolean redo) {
            this.movie = movie;
            this.redo = redo;
        }

        public SWF getMovie() {
            return movie;
        }

        public int size() {
            return entries.size();
        }

        public DoableOperation getOperation(int index) {
            return entries.get(index).operation;
        }
    }

    private final Map<SWF, History> histories = new HashMap<>();
    private final List<Runnable> changeListeners = new ArrayList<>();

    public synchronized void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    public synchronized void removeChangeListener(Runnable listener) {
        changeListeners.remove(listener);
    }

    private void fireChange() {
        for (Runnable listener : new ArrayList<>(changeListeners)) {
            listener.run();
        }
    }

    public synchronized void doOperation(DoableOperation operation, SWF swf) {
        rememberSavedPosition(swf);
        operation.doOperation();
        recordOperation(operation, Collections.singleton(swf));
    }

    public synchronized void rememberSavedPosition(SWF swf) {
        if (!swf.isModified()) {
            History history = histories.computeIfAbsent(swf, History::new);
            history.saved = history.position;
        }
    }

    /**
     * Adds an operation that has already been performed.
     */
    public synchronized void recordOperation(DoableOperation operation, Collection<SWF> movies) {
        Entry entry = new Entry(operation, movies);
        for (SWF swf : entry.movies) {
            History history = histories.computeIfAbsent(swf, History::new);
            discardRedo(history, history.position);
        }
        for (SWF swf : entry.movies) {
            History history = histories.get(swf);
            history.entries.add(entry);
            history.position++;
            updateModifiedState(swf, history);
        }
        fireChange();
    }

    private void discardRedo(History history, int from) {
        while (history.entries.size() > from) {
            int last = history.entries.size() - 1;
            if (history.saved > last) {
                history.saved = history.hasChangesBetween(last, history.saved) ? -1 : last;
            }
            Entry removed = history.entries.remove(last);
            for (SWF swf : removed.movies) {
                History other = histories.get(swf);
                int index = other.entries.indexOf(removed);
                if (index >= 0) {
                    discardRedo(other, index);
                }
            }
        }
    }

    private void updateModifiedState(SWF swf, History history) {
        if (history.saved >= 0 && !history.hasChangesBetween(history.saved, history.position)) {
            clearModifiedFlags(swf);
            swf.setModified(false);
            swf.setHeaderModified(false);
        } else {
            swf.setModified(true);
        }
    }

    private void clearModifiedFlags(Timelined timelined) {
        for (Tag tag : timelined.getTags()) {
            if (tag.isModified()) {
                if (tag instanceof DefineSpriteTag) {
                    clearModifiedFlags((Timelined) tag);
                }
                tag.createOriginalData();
                tag.setModified(false);
            }
        }
    }

    private Entry entryAt(History history, boolean redo, Map<SWF, Integer> positions) {
        if (history == null) {
            return null;
        }
        int position = positions.getOrDefault(history.swf, history.position);
        int index = redo ? position : position - 1;
        return index >= 0 && index < history.entries.size() ? history.entries.get(index) : null;
    }

    private Entry availableEntry(SWF swf, boolean redo) {
        return availableEntry(swf, redo, Collections.emptyMap());
    }

    private Entry availableEntry(SWF swf, boolean redo, Map<SWF, Integer> positions) {
        Entry entry = entryAt(histories.get(swf), redo, positions);
        if (entry != null) {
            for (SWF movie : entry.movies) {
                if (entryAt(histories.get(movie), redo, positions) != entry) {
                    return null;
                }
            }
        }
        return entry;
    }

    /** Returns the steps in replay order, stopping at the first blocked entry. */
    public synchronized ReplayPlan planReplay(SWF swf, boolean redo, int maxSteps) {
        ReplayPlan plan = new ReplayPlan(swf, redo);
        Map<SWF, Integer> positions = new HashMap<>();
        for (int step = 0; step < maxSteps; step++) {
            Entry entry = availableEntry(swf, redo, positions);
            if (entry == null) {
                break;
            }
            plan.entries.add(entry);
            for (SWF movie : entry.movies) {
                positions.put(movie, positions.getOrDefault(movie, histories.get(movie).position) + (redo ? 1 : -1));
            }
        }
        return plan;
    }

    public synchronized boolean canReplay(ReplayPlan plan, int steps) {
        return steps > 0 && steps <= plan.size()
                && planReplay(plan.movie, plan.redo, steps).entries.equals(plan.entries.subList(0, steps));
    }

    public synchronized boolean replayStep(ReplayPlan plan, int index) {
        Entry entry = plan.entries.get(index);
        if (availableEntry(plan.movie, plan.redo) != entry) {
            return false;
        }
        replay(entry, plan.redo);
        return true;
    }

    public synchronized boolean canUndo(SWF swf) {
        return availableEntry(swf, false) != null;
    }

    public synchronized boolean canRedo(SWF swf) {
        return availableEntry(swf, true) != null;
    }

    public synchronized String getUndoName(SWF swf) {
        Entry entry = availableEntry(swf, false);
        return entry == null ? null : entry.operation.getDescription();
    }

    public synchronized String getRedoName(SWF swf) {
        Entry entry = availableEntry(swf, true);
        return entry == null ? null : entry.operation.getDescription();
    }

    public synchronized void undo(SWF swf) {
        replay(availableEntry(swf, false), false);
    }

    public synchronized void redo(SWF swf) {
        replay(availableEntry(swf, true), true);
    }

    private void replay(Entry entry, boolean redo) {
        if (entry == null) {
            return;
        }
        for (SWF movie : entry.movies) {
            rememberSavedPosition(movie);
        }
        if (redo) {
            entry.operation.doOperation();
        } else {
            entry.operation.undoOperation();
        }
        for (SWF movie : entry.movies) {
            History history = histories.get(movie);
            history.position += redo ? 1 : -1;
            updateModifiedState(movie, history);
        }
        fireChange();
    }

    public synchronized void clear() {
        histories.clear();
        fireChange();
    }

    public synchronized void clear(SWF swf) {
        History history = histories.get(swf);
        if (history == null) {
            return;
        }
        discardRedo(history, history.position);
        histories.remove(swf);
        for (Entry entry : history.entries) {
            for (SWF movie : entry.movies) {
                History other = histories.get(movie);
                if (other != null) {
                    // Once a participant closes, edits preceding the shared change
                    // cannot safely be replayed in the remaining movie.
                    int end = other.entries.indexOf(entry) + 1;
                    if (end > 0) {
                        forgetPrefix(other, end);
                    }
                }
            }
        }
        fireChange();
    }

    private void forgetPrefix(History history, int end) {
        List<Entry> removed = new ArrayList<>(history.entries.subList(0, end));
        history.entries.subList(0, end).clear();
        history.position -= end;
        history.saved = history.saved < end ? -1 : history.saved - end;
        for (Entry entry : removed) {
            for (SWF swf : entry.movies) {
                History other = histories.get(swf);
                if (other != null) {
                    int index = other.entries.indexOf(entry);
                    if (index >= 0) {
                        forgetPrefix(other, index + 1);
                    }
                }
            }
        }
    }
}

/*
 * Copyright 2008 Ayman Al-Sairafi ayman.alsairafi@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License
 *       at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jsyntaxpane;

import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * A revised UndoManager that groups undos based on positions. If the change is
 * relatively next to the previous change, like when continuous typing, then the
 * undoes are grouped together.
 *
 * This is customized from the
 *
 * http://www.camick.com/java/source/CompoundUndoMan.java
 *
 * from the blog:
 *
 * http://tips4java.wordpress.com/2008/10/27/compound-undo-manager/
 *
 * @author Ayman Al-Sairafi
 *
 *
 * JPEXS - updated for java 9+
 * https://github.com/nordfalk/jsyntaxpane/blob/master/jsyntaxpane/src/main/java/jsyntaxpane/CompoundUndoManager.java
 *
 */
public class CompoundUndoMan extends UndoManager {

    private final SyntaxDocument doc;

    private CompoundEdit compoundEdit;
    // This allows us to start combining operations.
    // it will be reset after the first change.
    private boolean startCombine = false;
    // This holds the start of the last line edited, if edits are on multiple
    // lines, then they will not be combined.
    private int lastLine = -1;

    public CompoundUndoMan(SyntaxDocument doc) {
        this.doc = doc;
        doc.addUndoableEditListener(this);
        lastLine = doc.getStartPosition().getOffset();
    }

    /**
     * Whenever an UndoableEdit happens the edit will either be absorbed by the
     * current compound edit or a new compound edit will be started
     */
    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        //  Start a new compound edit

        if (compoundEdit == null) {
            compoundEdit = startCompoundEdit(e.getEdit());
            startCombine = false;
            updateDirty();
            return;
        }
        if (e.getEdit() instanceof DefaultDocumentEvent) {
            // Java 6 to 8
            AbstractDocument.DefaultDocumentEvent docEvt = (DefaultDocumentEvent) e.getEdit();

            int editLine = doc.getLineNumberAt(docEvt.getOffset());

            //  Check for an incremental edit or backspace.
            //  The Change in Caret position and Document length should both be
            //  either 1 or -1.
            if ((startCombine || Math.abs(docEvt.getLength()) == 1) && editLine == lastLine) {
                compoundEdit.addEdit(e.getEdit());
                startCombine = false;
                updateDirty();
                return;
            }

            //  Not incremental edit, end previous edit and start a new one
            lastLine = editLine;

        } else // Java 9: It seems that all the edits are wrapped and we cannot get line number!
        // See https://github.com/netroby/jdk9-dev/blob/master/jdk/src/java.desktop/share/classes/javax/swing/text/AbstractDocument.java#L279
        // AbstractDocument.DefaultDocumentEventUndoableWrapper docEvt = e.getEdit();
        {
            if (startCombine && !e.getEdit().isSignificant()) {
                compoundEdit.addEdit(e.getEdit());
                startCombine = false;
                updateDirty();
                return;
            }
        }

        compoundEdit.end();
        compoundEdit = startCompoundEdit(e.getEdit());

        updateDirty();
    }

    private void updateDirty() {
        doc.setCanUndo(canUndo());
        doc.setCanRedo(canRedo());
    }

    @Override
    protected void undoTo(UndoableEdit edit) throws CannotUndoException {
        super.undoTo(edit);
        updateDirty();
    }

    @Override
    public synchronized void undo() throws CannotUndoException {
        super.undo();
        updateDirty();
    }

    @Override
    protected void redoTo(UndoableEdit edit) throws CannotRedoException {
        super.redoTo(edit);
        updateDirty();
    }

    @Override
    public synchronized void redo() throws CannotRedoException {
        super.redo();
        updateDirty();
    }

    @Override
    public synchronized void discardAllEdits() {
        super.discardAllEdits();
        updateDirty();
    }

    /*
                         **  Each CompoundEdit will store a group of related incremental edits
                         **  (ie. each character typed or backspaced is an incremental edit)
     */
    private CompoundEdit startCompoundEdit(UndoableEdit anEdit) {
        //  Track Caret and Document information of this compound edit
        // AbstractDocument.DefaultDocumentEvent docEvt = (DefaultDocumentEvent) anEdit;

        //  The compound edit is used to store incremental edits
        compoundEdit = new MyCompoundEdit();
        compoundEdit.addEdit(anEdit);

        //  The compound edit is added to the UndoManager. All incremental
        //  edits stored in the compound edit will be undone/redone at once
        addEdit(compoundEdit);

        return compoundEdit;
    }

    class MyCompoundEdit extends CompoundEdit {

        @Override
        public boolean isInProgress() {
            //  in order for the canUndo() and canRedo() methods to work
            //  assume that the compound edit is never in progress
            return false;
        }

        @Override
        public void undo() throws CannotUndoException {
            //  End the edit so future edits don't get absorbed by this edit

            if (compoundEdit != null) {
                compoundEdit.end();
            }

            super.undo();

            //  Always start a new compound edit after an undo
            compoundEdit = null;
        }
    }

    /**
     * Start to combine the next operations together. Only the next operation is
     * combined. The flag is then automatically reset.
     */
    public void startCombine() {
        startCombine = true;
    }
}

/*
 * www.javagl.de - JTreeTable
 *
 * Copyright (c) 2016 Marco Hutter - http://www.javagl.de
 * 
 * This library is based on the code from the article "Creating TreeTables"
 * by Sun Microsystems (now known as Oracle). 
 * 
 * The original copyright header:
 *  
 * Copyright 1998 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.javagl.treetable;

import java.util.EventObject;

import javax.swing.CellEditor;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;


/**
 * Abstract base implementation of a CellEditor for a {@link JTreeTable}.
 */
public class AbstractCellEditor implements CellEditor
{
    /**
     * The list of CellEditorListeners
     */
    private final EventListenerList listenerList = new EventListenerList();

    @Override
    public Object getCellEditorValue()
    {
        return null;
    }

    @Override
    public boolean isCellEditable(EventObject e)
    {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent)
    {
        return false;
    }

    @Override
    public boolean stopCellEditing()
    {
        return true;
    }

    @Override
    public void cancelCellEditing()
    {
        // Empty default implementation
    }

    @Override
    public final void addCellEditorListener(CellEditorListener l)
    {
        listenerList.add(CellEditorListener.class, l);
    }

    @Override
    public final void removeCellEditorListener(CellEditorListener l)
    {
        listenerList.remove(CellEditorListener.class, l);
    }

    /**
     * Notify all listeners that have registered interest for notification on
     * this event type.
     */
    protected final void fireEditingStopped()
    {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == CellEditorListener.class)
            {
                ((CellEditorListener) listeners[i + 1])
                    .editingStopped(new ChangeEvent(this));
            }
        }
    }

    /**
     * Notify all listeners that have registered interest for notification on
     * this event type.
     */
    protected final void fireEditingCanceled()
    {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == CellEditorListener.class)
            {
                ((CellEditorListener) listeners[i + 1])
                    .editingCanceled(new ChangeEvent(this));
            }
        }
    }
}

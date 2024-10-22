/*
 *  Copyright (C) 2010-2024 JPEXS
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
package com.jpexs.decompiler.flash.gui.editor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.Token;
import jsyntaxpane.actions.ActionUtils;

/**
 * @author JPEXS
 */
public class MyMarkers {

    /**
     * Removes only our private highlights This is public so that we can remove
     * the highlights when the editorKit is unregistered. SimpleMarker can be
     * null, in which case all instances of our Markers are removed.
     *
     * @param component the text component whose markers are to be removed
     * @param marker the SimpleMarker to remove
     */
    public static void removeMarkers(JTextComponent component, Highlighter.HighlightPainter marker) {
        Highlighter hilite = component.getHighlighter();
        Highlighter.Highlight[] hilites = hilite.getHighlights();

        for (int i = 0; i < hilites.length; i++) {
            Highlighter.HighlightPainter hMarker = hilites[i].getPainter();
            if (marker == null || hMarker.equals(marker)) {
                hilite.removeHighlight(hilites[i]);
            }
        }
    }

    /**
     * Remove all the markers from an JEditorPane
     *
     * @param editorPane Editor
     */
    public static void removeMarkers(JTextComponent editorPane) {
        removeMarkers(editorPane, null);
    }

    /**
     * add highlights for the given Token on the given pane
     *
     * @param pane Editor
     * @param token Token
     * @param marker Marker
     */
    public static void markToken(JTextComponent pane, Token token, Highlighter.HighlightPainter marker) {
        markText(pane, token.start, token.end(), marker);
    }

    /**
     * add highlights for the given region on the given pane
     *
     * @param pane Editor
     * @param start Start index
     * @param end End index
     * @param marker Marker
     */
    public static void markText(JTextComponent pane, int start, int end, Highlighter.HighlightPainter marker) {
        try {
            Highlighter hiliter = pane.getHighlighter();
            int selStart = pane.getSelectionStart();
            int selEnd = pane.getSelectionEnd();
            // if there is no selection or selection does not overlap
            if (selStart == selEnd || end < selStart || start > selStart) {
                hiliter.addHighlight(start, end, marker);
                return;
            }
            // selection starts within the highlight, highlight before selection
            if (selStart > start && selStart < end) {
                hiliter.addHighlight(start, selStart, marker);
            }
            // selection ends within the highlight, highlight remaining
            if (selEnd > start && selEnd < end) {
                hiliter.addHighlight(selEnd, end, marker);
            }

        } catch (BadLocationException ex) {
            //ignored
        }
    }

    /**
     * Mark all text in the document that matches the given pattern
     *
     * @param pane control to use
     * @param pattern pattern to match
     * @param marker marker to use for highlighting
     */
    public static void markAll(JTextComponent pane, Pattern pattern, Highlighter.HighlightPainter marker) {
        SyntaxDocument sDoc = ActionUtils.getSyntaxDocument(pane);
        if (sDoc == null || pattern == null) {
            return;
        }
        Matcher matcher = sDoc.getMatcher(pattern);
        // we may not have any matcher (due to undo or something, so don't do anything.
        if (matcher == null) {
            return;
        }
        while (matcher.find()) {
            markText(pane, matcher.start(), matcher.end(), marker);
        }
    }
}

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
package jsyntaxpane.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.JTextComponent;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.Token;
import jsyntaxpane.actions.gui.ComboCompletionDialog;
import jsyntaxpane.util.StringUtils;

/**
 * This action will try to complete the word at the cursor by
 * looking for a matching word in this document that starts with
 * the same letters.
 *
 * This makes use of the SyntaxDocument.getWordAt, which requires
 * a Regexp Pattern.  The Pattern should match any word regardless of
 * the Tokens.
 *
 * This Regexp is configurable with {@code ACTION_NAME.WordsRegexp}.
 * The default Regexp is \w+ (any word char)
 *
 * @author Ayman Al-Sairafi
 */
public class CompleteWordAction extends DefaultSyntaxAction {

    public CompleteWordAction() {
        super("COMPLETE_WORD");
    }

    @Override
    public void actionPerformed(JTextComponent target, SyntaxDocument sdoc,
            int dot, ActionEvent e) {
        Token current = sdoc.getWordAt(dot, wordsPattern);
        if (current == null) {
            return;
        }
        String cw = current.getString(sdoc);
        target.select(current.start, current.end());

        sdoc.readLock();
        List<String> matches = new ArrayList<String>();
        Matcher m = sdoc.getMatcher(wordsPattern, 0, current.start);
        addWords(m, cw, matches);
        m = sdoc.getMatcher(wordsPattern, current.end(), sdoc.getLength() - current.end());
        addWords(m, cw, matches);
        sdoc.readUnlock();
        if (matches.size() == 0) {
            return;
        }
        if (matches.size() == 1) {
            target.replaceSelection(matches.get(0));
            return;
        }
        if (dlg == null) {
            dlg = new ComboCompletionDialog(target);
        }
        dlg.displayFor(cw, matches);
    }

    public void setWordsRegexp(String value) {
        wordsPattern = Pattern.compile(value);

    }

    /**
     * Add words from the matcher m that match the word abbr to matches
     * List
     * @param m matcher instance, could be null, to iterate through
     * @param abbr abbriviated word
     * @param matches List of matches
     */
    private void addWords(Matcher m, String abbr, List<String> matches) {
        while (m != null && m.find()) {
            String word = m.group();
            if (StringUtils.camelCaseMatch(word, abbr)) {
                if (!matches.contains(word)) {
                    matches.add(word);
                }
            }
        }
    }
    private ComboCompletionDialog dlg;
    private Pattern wordsPattern = DEFAULT_WORDS_REGEXP;
    private static final Pattern DEFAULT_WORDS_REGEXP = Pattern.compile("\\w+");
}

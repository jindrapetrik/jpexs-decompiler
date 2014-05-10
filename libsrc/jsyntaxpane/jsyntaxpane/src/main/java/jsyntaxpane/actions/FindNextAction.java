package jsyntaxpane.actions;

import java.awt.event.ActionEvent;
import javax.swing.text.JTextComponent;
import jsyntaxpane.SyntaxDocument;

/**
 * This class performs a Find Next operation by using the current pattern
 */
public class FindNextAction extends DefaultSyntaxAction {

    public FindNextAction() {
        super("find-next");
    }

    @Override
    public void actionPerformed(JTextComponent target, SyntaxDocument sdoc,
            int dot, ActionEvent e) {
        DocumentSearchData dsd = DocumentSearchData.getFromEditor(target);
        if (dsd != null) {
            if(!dsd.doFindNext(target)) {
				dsd.msgNotFound(target);
			}
        }
    }
}
